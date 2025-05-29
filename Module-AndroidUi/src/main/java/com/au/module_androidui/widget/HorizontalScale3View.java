package com.au.module_androidui.widget;

import android.animation.ValueAnimator;
import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Rect;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.VelocityTracker;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.animation.DecelerateInterpolator;

import androidx.annotation.NonNull;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 参考：<a href="https://github.com/GITbiubiubiu/ScaleView">...</a>
 * 实现的温度刻度尺。5格一次。
 */
public class HorizontalScale3View extends View {

    public interface OnValueChangeListener {
        void onValueChanged(int value, String action);
    }

    private int bigScaleW;//大刻度线宽度
    private int smallScaleW;//小刻度线宽度
    private float mSpace;//刻度间距
    private float mSpaceCloseBig; //大刻度左右的2格间距

    private int height; //View高度

    private int max;//最大刻度
    private int min;//最小刻度
    private float mBoundMinLeft;//滑动条能够走到的极限，左右边界值坐标。
    private float mHalfWidth; //中间的位置
    private float mBoundMinRight; //滑动到最右边的极限

    private float currentX;//最小刻度x坐标,从最小刻度开始画刻度

    private float lastX;

    private int currentValue;//当前刻度对应的值

    private Paint paint;//画笔
    private int mTextColor = Color.BLACK;
    private int mLineColor = Color.GRAY;
    private int mIndicatorColor = Color.RED;

    private int drawSmallStartY, drawSmallEndY;
    private int drawBigStartY, drawBigEndY;

    public void setTextColor(int textColor) {
        this.mTextColor = textColor;
    }
    public void setLineColor(int lineColor) {
        this.mLineColor = lineColor;
    }
    public void setIndicatorColor(int indicatorColor) {
        this.mIndicatorColor = indicatorColor;
    }

    private final Context context;

    private VelocityTracker velocityTracker;//速度监测

    private final Map<Integer, Float> mCacheAllOffsetXMap = new HashMap<>(); //缓存所有刻度偏移x值, key就是我们的刻度数值

    private final List<Rect> cacheRectList = new ArrayList<>();
    private final Path mInvertedTrianglePath = new Path();
    private int mInvertedTriangleHeight;

    private OnValueChangeListener mOnValueChangeListener;
    private final Handler mHandler = new Handler(Looper.getMainLooper()) {
        @Override
        public void handleMessage(@NonNull Message msg) {
            if (null != mOnValueChangeListener) {
                mOnValueChangeListener.onValueChanged(currentValue, msg.obj.toString());
            }
        }
    };

    public HorizontalScale3View(Context context) {
        super(context);
        this.context = context;
        init();
    }

    public HorizontalScale3View(Context context, AttributeSet attrs) {
        super(context, attrs);
        this.context = context;
        init();
    }

    public HorizontalScale3View(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        this.context = context;
        init();
    }

    private void init() {
        //初始化画笔
        paint = new Paint();
        paint.setAntiAlias(true);
        paint.setDither(true);
    }

    //设置刻度范围
    public void setRange(int min, int max, int current) {
        this.min = min;
        this.max = max;
        //初始刻度对应的值
        currentValue = current;

        cacheRectList.clear();
        var newRectList = new ArrayList<Rect>();
        for (int i = min; i <= max; i++) {
            newRectList.add(new Rect());
        }
        cacheRectList.addAll(newRectList);
    }

    //设置value变化监听
    public void setOnValueChangeListener(OnValueChangeListener mOnValueChangeListener) {
        this.mOnValueChangeListener = mOnValueChangeListener;
    }

    private float calculateAllOffsetX(float halfWidth) {
        var allOffsetXList = new ArrayList<Float>();

        var total = 0f;
        //计算总共的长度；先每一根刻度的宽度，再加上后一个相邻的间距
        for (int i = min; i < max; i++) {
            allOffsetXList.add(total); //逐步将每个刻度offsetX存入

            var d = i % 5;
            if (d == 0) {
                total += bigScaleW;
                total += mSpaceCloseBig;
            } else if (d == 4) {
                total += smallScaleW;
                total += mSpaceCloseBig;
            } else {
                total += smallScaleW;
                total += mSpace;
            }
        }
        allOffsetXList.add(total);
        var lastScale = (max % 5 == 0) ? bigScaleW : smallScaleW;
        total += lastScale; //最后一根刻度
        var sz = allOffsetXList.size();

        mBoundMinLeft = halfWidth + lastScale / 2f - total;
        mBoundMinRight = halfWidth - lastScale / 2f;
        Log.d("alland", "AllX halfWidth " + halfWidth + " total " + total + " size " + sz + " boundLeft " + mBoundMinLeft + " boundRight " + mBoundMinRight);

        //倒序装入的才是正确的offsetX
        mCacheAllOffsetXMap.clear();
        for (int i = 0; i < sz; i++) { //todo 是否 《=
            var v = mBoundMinLeft + allOffsetXList.get(i);
            mCacheAllOffsetXMap.put(min + i, v);
            Log.d("alland", "mCacheAllOffsetXList k: " + (min + i) + " v: " + v);
        }
        return total;
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        //view宽度
        int width = getMeasuredWidth();
        height = getMeasuredHeight();

        float px = height / 46f;

        //按照公司的UI计算比例得出。直接用测量的高度来转换。其实更好的是传入。
        paint.setTextSize(10 * px);
        mInvertedTriangleHeight = (int) (5 * px);

        smallScaleW = (int) px;
        bigScaleW = (int) (px * 3 / 2);
        //大刻度长度
        int bigScaleH = (int) (px * 18);
        //小刻度长度
        int smallScaleH = (int) (px * 12);
        mSpace = (px * 8);
        mSpaceCloseBig = (mSpace + px - bigScaleW / 2f);

        var hw = width / 2f;
        mHalfWidth = hw;
        //整条ruler的长度
        calculateAllOffsetX(hw);

        currentX = mCacheAllOffsetXMap.get(currentValue);

        drawBigStartY = (int) (mInvertedTriangleHeight + px);
        drawBigEndY = drawSmallEndY = drawBigStartY + bigScaleH;
        drawSmallStartY = drawSmallEndY - smallScaleH;
    }

    @Override
    protected void onDraw(@NonNull Canvas canvas) {
        super.onDraw(canvas);
        var hfScaleWidthBig = bigScaleW / 2f;

        //画刻度线
        var drawX = currentX;
        for (int i = min; i <= max; i++) {
            var d = i % 5;
            var isBig = d == 0;
            var is4 = d == 4;

            paint.setColor(mLineColor);
            if (isBig) {
                //画大刻度线
                paint.setStrokeWidth(bigScaleW);
                canvas.drawLine(drawX, drawBigStartY, drawX, drawBigEndY, paint);

                //画刻度数字
                Rect rect = cacheRectList.get(i - min);
                String str = String.valueOf(i);
                paint.setColor(mTextColor);
                paint.getTextBounds(str, 0, str.length(), rect);
                int w = rect.width();
                int h = rect.height();
                canvas.drawText(str, drawX - w / 2f - hfScaleWidthBig, height - h, paint);

                drawX += bigScaleW;
            } else {
                //画小刻度线
                paint.setStrokeWidth(smallScaleW);
                canvas.drawLine(drawX, drawSmallStartY, drawX, drawSmallEndY, paint);
                drawX += smallScaleW;
            }
            drawX += (isBig || is4) ? mSpaceCloseBig : mSpace;
        }

        //画中刻度线
        paint.setColor(mIndicatorColor);
        paint.setStrokeWidth(bigScaleW);
        canvas.drawLine(mHalfWidth - bigScaleW/2f, drawBigStartY, mHalfWidth - bigScaleW/2f, drawBigEndY, paint);

        //画中间倒三角形指示器
        paint.setStrokeWidth(smallScaleW);
        Path invertedTrianglePath = mInvertedTrianglePath;
        invertedTrianglePath.reset();
        var triangleW = mInvertedTriangleHeight * 2 / 3;
        invertedTrianglePath.moveTo(mHalfWidth - triangleW, 0);
        invertedTrianglePath.lineTo(mHalfWidth + triangleW, 0);
        invertedTrianglePath.lineTo(mHalfWidth, mInvertedTriangleHeight);
        invertedTrianglePath.close();
        canvas.drawPath(invertedTrianglePath, paint);
    }

    @SuppressLint("ClickableViewAccessibility")
    @Override
    public boolean onTouchEvent(MotionEvent event) {
        // 阻止父布局拦截事件
        getParent().requestDisallowInterceptTouchEvent(event.getAction() != MotionEvent.ACTION_UP && event.getAction() != MotionEvent.ACTION_CANCEL); // 允许父布局拦截事件

        float x = event.getX();
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                stopScroll();
                lastX = x;
                //初始化速度追踪
                if (velocityTracker == null) {
                    velocityTracker = VelocityTracker.obtain();
                } else {
                    velocityTracker.clear();
                }

                break;
            case MotionEvent.ACTION_MOVE:
                velocityTracker.addMovement(event);
                int offsetX = (int) (lastX - x);
                changeMinX(confirmBoarderWhenMoving(currentX, offsetX), "move");
                invalidate();
                lastX = x;
                break;
            case MotionEvent.ACTION_UP:
                velocityTracker.computeCurrentVelocity(1000);
                float velocityX = velocityTracker.getXVelocity();
                float minVelocity = ViewConfiguration.get(context).getScaledMinimumFlingVelocity();

                if (Math.abs(velocityX) > minVelocity) {
                    //计算得到滑动目标点
                    var time = new int[1];
                    var targetX = confirmBoarder(calculateTargetX(velocityX, time));
                    var clearlyValue = calculateClearlyValue(targetX);
                    var clearlyX = mCacheAllOffsetXMap.get(clearlyValue); //精确x
                    var totalTime = Math.max(200, Math.min(time[0], 1500));
                    runScroll(currentX, clearlyX, totalTime);
                } else {
                    // 慢速滑动时使用直接偏移量
                    var clearlyTargetX = mCacheAllOffsetXMap.get(currentValue);
                    runScroll(currentX, clearlyTargetX, 200);
                }

                velocityTracker.recycle();
                velocityTracker = null;
                break;
            case MotionEvent.ACTION_CANCEL:
                stopScroll();

                changeMinX(confirmBoarderWhenMoving(currentX, 0), "up");

                velocityTracker.recycle();
                velocityTracker = null;
                invalidate();
                break;
        }
        return true;
    }

    private void changeMinX(float currentX, String move) {
        this.currentX = currentX;
        currentValue = calculateClearlyValue(currentX);
        Log.d("alland", String.format(move + ": chagne currentX " + currentX + ", currentValue=" + currentValue));
        mHandler.sendMessage(mHandler.obtainMessage(0, move));
    }

    private float calculateTargetX(float velocity, int[] time) {
        //根据速度计算targetX
        var targetX = currentX;
        var v = velocity;
        time[0] = 200;
        //加速度：数值越大，时间越长。
        float a = 2_00_000;//1_000_000;
        //差值：数值越大，停的越快
        float d = 150; //50

        do {
            float velocityAbs = 0;
            if (v > 0) {
                v -= d;
                targetX += v * v / a;
                velocityAbs = v;
            } else if (v < 0) {
                v += d;
                targetX -= v * v / a;
                velocityAbs = -v;
            }
            time[0] += 10;
            if (velocityAbs <= 0) {
                break;
            }
        } while(true);
        //再计算刻度精确x
        return targetX;
    }

    private int calculateClearlyValue(float targetX) {
        //在cacheAllOffsetXList中，找到偏移
        //二分法：在cacheAllOffsetXList中，找到偏移为offsetTotal的刻度
        int l = min;
        int r = max;
        int m = currentValue;
        var isFullMatch = false;
        while (l <= r) {
            m = (l + r) / 2;
            var x = mCacheAllOffsetXMap.get(m);
            if (x > targetX) {
                r = m - 1;
            } else if (x < targetX) {
                l = m + 1;
            } else {
                //刚刚好
                isFullMatch = true;
                break;
            }
        }
        var matchedX = mCacheAllOffsetXMap.get(m);
        int targetValue;
        if (isFullMatch) {
            targetValue = m;
        } else {
            var deltaX = targetX - matchedX;

            if ((Math.abs(deltaX) + 1e-6f) < mSpaceCloseBig / 2f) {
                targetValue = m;
            } else {
                targetValue = m + (deltaX < 0 ? 1 : -1);//根据边界值自然推出来。
            }
        }

        targetValue = Math.max(min, Math.min(targetValue, max));
        Log.d("alland", "cal ClearlyValue targetX " + targetX + " targetValue: " + targetValue);
        return targetValue;
    }

    private float confirmBoarderWhenMoving(float currentX, int offsetX) {
        var newMinX = currentX - offsetX;
        return confirmBoarder(newMinX);
    }

    private float confirmBoarder(float targetX) {
        return Math.max(mBoundMinLeft, Math.min(targetX, mBoundMinRight));
    }

    private ValueAnimator mAnimator;
    private void stopScroll() {
        if(mAnimator != null) mAnimator.cancel();
        mAnimator = null;
    }

    private void runScroll(float startX, float targetX, int duration) {
        stopScroll();

        mAnimator = ValueAnimator.ofFloat(startX, targetX);
        mAnimator.setInterpolator(new DecelerateInterpolator());
        mAnimator.setDuration(duration);
        mAnimator.addUpdateListener(animation -> {
            changeMinX((float) animation.getAnimatedValue(), "up");
            postInvalidate();
        });
        mAnimator.start();
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        stopScroll();
    }
}