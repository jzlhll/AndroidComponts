package com.au.module_androidui.widget;

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

import androidx.annotation.NonNull;

import java.util.ArrayList;
import java.util.List;

/**
 * 参考：<a href="https://github.com/GITbiubiubiu/ScaleView">...</a>
 * 实现的温度刻度尺。5格一次。
 */
public class HorizontalScale2View extends View {

    public interface OnValueChangeListener {
        void onValueChanged(int value);
    }

    private int bigScaleW;//大刻度线宽度
    private int smallScaleW;//小刻度线宽度
    private float mSpace;//刻度间距
    private float mSpaceCloseBig; //大刻度左右的2格间距

    private int height; //View高度

    private int max;//最大刻度
    private int min;//最小刻度
    private int boundMinLeft, boundMinRight;//滑动条能够走到的极限，左右边界值坐标。
    private float minX;//最小刻度x坐标,从最小刻度开始画刻度

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
    private float velocity;//当前滑动速度

    private final List<Float> cacheAllOffsetXList = new ArrayList<>(); //缓存所有刻度偏移x值

    private final List<Rect> cacheRectList = new ArrayList<>();
    private final Path mInvertedTrianglePath = new Path();
    private int mInvertedTriangleHeight;

    private OnValueChangeListener mOnValueChangeListener;
    private final Handler mHandler = new Handler(Looper.getMainLooper()) {
        @Override
        public void handleMessage(@NonNull Message msg) {
            if (null != mOnValueChangeListener) {
                mOnValueChangeListener.onValueChanged(currentValue);
            }
        }
    };

    public HorizontalScale2View(Context context) {
        super(context);
        this.context = context;
        init();
    }

    public HorizontalScale2View(Context context, AttributeSet attrs) {
        super(context, attrs);
        this.context = context;
        init();
    }

    public HorizontalScale2View(Context context, AttributeSet attrs, int defStyleAttr) {
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

    private float calculateAllOffsetX() {
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
        allOffsetXList.add(total); //逐步将每个刻度offsetX存入
        var d = max % 5;
        total += (d == 0) ? bigScaleW : smallScaleW;

        //倒序装入的才是正确的offsetX
        cacheAllOffsetXList.clear();
        for (int i = allOffsetXList.size() - 1; i >= 0; i--) {
            cacheAllOffsetXList.add(allOffsetXList.get(i));
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

        //整条ruler的长度
        float mTotalWidth = calculateAllOffsetX();

        var hw = width / 2;
        boundMinLeft = (int) (-mTotalWidth + hw);
        boundMinRight = hw - (max % 5 == 0 ? bigScaleW : smallScaleW);
        minX = boundMinLeft;

        drawBigStartY = (int) (mInvertedTriangleHeight + px);
        drawBigEndY = drawSmallEndY = drawBigStartY + bigScaleH;
        drawSmallStartY = drawSmallEndY - smallScaleH;
    }

    @Override
    protected void onDraw(@NonNull Canvas canvas) {
        super.onDraw(canvas);
        var hfScaleWidthBig = bigScaleW / 2f;

        //画刻度线
        var drawX = minX;
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
        canvas.drawLine(boundMinRight, drawBigStartY, boundMinRight, drawBigEndY, paint);

        //画中间倒三角形指示器
        paint.setStrokeWidth(smallScaleW);
        Path invertedTrianglePath = mInvertedTrianglePath;
        invertedTrianglePath.reset();
        var triangleW = mInvertedTriangleHeight * 2 / 3;
        invertedTrianglePath.moveTo(boundMinRight -triangleW, 0);
        invertedTrianglePath.lineTo(boundMinRight + triangleW, 0);
        invertedTrianglePath.lineTo(boundMinRight, mInvertedTriangleHeight);
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
                lastX = x;
                continueScroll = false;
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
                confirmBoarderWhenMoving(offsetX);
                calculateCurrentScale();
                invalidate();
                lastX = x;
                break;
            case MotionEvent.ACTION_UP:
                confirmBorder();
                //当前滑动速度
                velocityTracker.computeCurrentVelocity(1000);
                velocity = velocityTracker.getXVelocity();
                float minVelocity = ViewConfiguration.get(context).getScaledMinimumFlingVelocity();
                if (Math.abs(velocity) > minVelocity) {
                    continueScroll = true;
                    continueScroll();
                } else {
                    velocityTracker.recycle();
                    velocityTracker = null;
                }
                break;
            case MotionEvent.ACTION_CANCEL:
                velocityTracker.recycle();
                velocityTracker = null;
                break;
        }
        return true;
    }

    //计算当前刻度
    private boolean calculateCurrentScale() {
        float offsetTotal = minX - boundMinLeft; //距离左极限的偏移，其实就是整个View的滑动距离

        //二分法：在cacheAllOffsetXList中，找到偏移为offsetTotal的刻度
        int l = 0;
        int r = cacheAllOffsetXList.size() - 1;
        int m = 0;
        var isFullMatch = false;
        while (l <= r) {
            m = (l + r) / 2;
            if (cacheAllOffsetXList.get(m) < offsetTotal) {
                r = m - 1;
            } else if (cacheAllOffsetXList.get(m) > offsetTotal) {
                l = m + 1;
            } else {
                //刚刚好
                isFullMatch = true;
                break;
            }
        }
        var matchedOffsetX = cacheAllOffsetXList.get(m);
        if (isFullMatch) {
            currentValue = min + m;
        } else {
            var deltaX = offsetTotal - matchedOffsetX;

            if ((Math.abs(deltaX) + 1e-6f) < mSpaceCloseBig / 2f) {
                currentValue = min + m;
            } else {
                currentValue = min + m + (deltaX < 0 ? 1 : -1);//根据边界值自然推出来。
            }
        }
        mHandler.sendEmptyMessage(0);

        return isFullMatch;
    }

    private void confirmBoarderWhenMoving(int offsetX) {
        var newMinX = minX - offsetX;

        if (newMinX < boundMinLeft) {
            minX = boundMinLeft;
        } else if (newMinX > boundMinRight) {
            minX = boundMinRight;
        } else {
            minX = newMinX;
        }
    }

    //指针线超出范围时 重置回边界处
    private void confirmBorder() {
        confirmBoarderWhenMoving(0);
        postInvalidate();
    }

    private boolean continueScroll;//是否继续滑动
    private void ifContinueScroll(float velocityAbs) {
        if (continueScroll && velocityAbs > 0) {
            post(mContinueScrollRunnable);
        } else {
            continueScroll = false;
        }
    }

    private final Runnable mContinueScrollRunnable = new Runnable() {
        @Override
        public void run() {
            float velocityAbs = 0;//速度绝对值
            //加速度
            float a = 1000000;
            if (velocity > 0 && continueScroll) {
                velocity -= 50;
                minX += velocity * velocity / a;
                velocityAbs = velocity;
            } else if (velocity < 0 && continueScroll) {
                velocity += 50;
                minX -= velocity * velocity / a;
                velocityAbs = -velocity;
            }
            calculateCurrentScale();
            confirmBorder();
            postInvalidate();
            ifContinueScroll(velocityAbs);
        }
    };

    //手指抬起后继续惯性滑动
    private void continueScroll() {
        mContinueScrollRunnable.run();
    }
}