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
import android.text.TextPaint;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.VelocityTracker;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.animation.DecelerateInterpolator;

import androidx.annotation.NonNull;

import java.util.ArrayList;
import java.util.List;

/**
 * 参考：<a href="https://github.com/GITbiubiubiu/ScaleView">...</a>
 * 实现的温度刻度尺。5格一次。
 */
public class HorizontalScale3View extends View {

    public interface OnValueChangeListener {
        void onValueChanged(int value, String action);
    }

    private static final int SCALE_STEP = 5;  // 常量代替魔数

    private int mScaleWidth;//刻度线宽度
    private float mSpace;//刻度间距

    private int height; //View高度

    private int max;//最大刻度
    private int min;//最小刻度
    private float mBoundMinLeft;//滑动条能够走到的极限，左右边界值坐标。
    private float mHalfWidth; //中间的位置
    private float mBoundMinRight; //滑动到最右边的极限

    private float currentX;//最小刻度x坐标,从最小刻度开始画刻度

    private float lastX;

    private int currentValue;//当前刻度对应的值
    public int getCurrentValue() {
        return currentValue;
    }
    public void setCurrentValue(int currentValue) {
        this.currentValue = Math.max(min, Math.min(currentValue, max));
        currentX = getClearlyXByValue(currentValue);
        postInvalidate();
        mHandler.sendMessage(mHandler.obtainMessage(0, "change"));
    }

    private Paint paint;//画笔
    private Paint textPaint; //画下面数字的画笔
    private int mLineColor = Color.GRAY;
    private int mIndicatorColor = Color.RED;

    //画短线和长线的起点位置和终点位置
    private int mSmallLineStartY, mSmallLineEndY;
    private int mBigLineStartY, mBigLineEndY;

    public void setTextColor(int textColor) {
        textPaint.setColor(textColor);
    }
    public void setLineColor(int lineColor) {
        this.mLineColor = lineColor;
    }
    public void setIndicatorColor(int indicatorColor) {
        this.mIndicatorColor = indicatorColor;
    }

    private final Context context;

    private VelocityTracker velocityTracker;//速度监测

    private final List<Rect> cacheRectList = new ArrayList<>();
    private final Path mInvertedTrianglePath = new Path();
    private int mInvertedTriangleHeight; //倒三角形指示器的高度

    private OnValueChangeListener mOnValueChangeListener;
    private final Handler mHandler = new Handler(Looper.getMainLooper()) {
        @Override
        public void handleMessage(@NonNull Message msg) {
            if (mOnValueChangeListener != null) {
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

        textPaint = new TextPaint(paint);
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

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        //view宽度
        int width = getMeasuredWidth();
        height = getMeasuredHeight();

        float px = height / 46f;

        //按照公司的UI计算比例得出。直接用测量的高度来转换。其实更好的是传入。
        textPaint.setTextSize(10 * px);

        mInvertedTriangleHeight = (int) (5 * px); //倒三角形的高度

        mScaleWidth = (int) px;
        //大刻度长度
        int bigScaleH = (int) (px * 18);
        //小刻度长度
        int smallScaleH = (int) (px * 12);
        mSpace = px * 8;

        var hw = width >> 1;
        mHalfWidth = hw;
        // 计算总刻度数和总宽度
        int count = max - min + 1;
        float totalWidth = count * mScaleWidth + (count - 1) * mSpace;

        // 计算边界位置
        float halfScale = mScaleWidth / 2f;
        mBoundMinLeft = hw - totalWidth + halfScale;
        mBoundMinRight = hw - halfScale;

        currentX = getClearlyXByValue(currentValue);

        mBigLineStartY = (int) (mInvertedTriangleHeight + px);
        mBigLineEndY = mBigLineStartY + bigScaleH;
        mSmallLineEndY = mBigLineEndY;
        mSmallLineStartY = mSmallLineEndY - smallScaleH;
    }

    private float getClearlyXByValue(int value) {
        var i = max - value;
        return mBoundMinLeft + i * (mScaleWidth + mSpace);
    }

    @Override
    protected void onDraw(@NonNull Canvas canvas) {
        super.onDraw(canvas);
        var hfScaleWidth = mScaleWidth >> 1;
        int min = this.min;
        int max = this.max;
        int scaleWidth = this.mScaleWidth;
        int bigLineStartY = this.mBigLineStartY;
        int bigLineEndY = this.mBigLineEndY;
        int smallLineStartY = this.mSmallLineStartY;
        int smallLineEndY = this.mSmallLineEndY;
        int height = this.height;
        float space = mSpace;
        var halfWidth = mHalfWidth;

        //画刻度线
        var drawX = currentX;
        //Log.d("ScaleView", "on Draw0: " + drawX);
        paint.setColor(mLineColor);
        paint.setStrokeWidth(scaleWidth);

        for (int i = min; i <= max; i++) {
            if (i % SCALE_STEP == 0) {
                //画大刻度线
                canvas.drawLine(drawX, bigLineStartY, drawX, bigLineEndY, paint);

                //画刻度数字
                Rect rect = cacheRectList.get(i - min);
                String str = String.valueOf(i);
                textPaint.getTextBounds(str, 0, str.length(), rect);
                int w = rect.width();
                int h = rect.height();
                canvas.drawText(str, drawX - (w >> 1) - hfScaleWidth, height - h, textPaint);
            } else {
                canvas.drawLine(drawX, smallLineStartY, drawX, smallLineEndY, paint);
            }
            drawX += scaleWidth;
            drawX += space;
        }

        //画中刻度线
        var midLineWidth = mScaleWidth * 2;
        var halfMidLineWidth = midLineWidth >> 1;

        paint.setColor(mIndicatorColor);
        paint.setStrokeWidth(midLineWidth);
        canvas.drawLine(halfWidth - halfMidLineWidth, bigLineStartY, halfWidth - halfMidLineWidth, bigLineEndY, paint);

        //画中间倒三角形指示器
        paint.setStrokeWidth(midLineWidth);
        Path invertedTrianglePath = mInvertedTrianglePath;
        invertedTrianglePath.reset();
        var triangleW = mInvertedTriangleHeight * 2 / 3;
        invertedTrianglePath.moveTo(halfWidth - triangleW - halfMidLineWidth, 0);
        invertedTrianglePath.lineTo(halfWidth + triangleW - halfMidLineWidth, 0);
        invertedTrianglePath.lineTo(halfWidth - halfMidLineWidth, mInvertedTriangleHeight);
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
                    var clearlyX = getClearlyXByValue(clearlyValue); //精确x
                    var totalTime = Math.max(200, Math.min(time[0], 1500));
                    runScroll(currentX, clearlyX, totalTime);
                } else {
                    // 慢速滑动时使用直接偏移量
                    var clearlyTargetX = getClearlyXByValue(currentValue);
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

    /**
     * 使用二分查找在刻度坐标数组中查找最接近目标位置的值
     * @param targetX 目标X坐标位置
     * @return 最接近的刻度值
     */
    private int calculateClearlyValue(float targetX) {
        int l = min;
        int r = max;
        int m = currentValue;

        // 二分查找最佳匹配值
        while (l <= r) {
            m = (l + r) >> 1;
            float x = getClearlyXByValue(m);
            if (x < targetX) {
                r = m - 1;
            } else if (x > targetX) {
                l = m + 1;
            } else {
                // 精确匹配到坐标点
                return m;
            }
        }

        // 检查是否在合理误差范围内
        float matchedX = getClearlyXByValue(m);
        float deltaX = targetX - matchedX;
        float threshold = mSpace * 0.5f;

        if (Math.abs(deltaX) > threshold) {
            m += (deltaX < 0 ? 1 : -1);
        }

        // 确保结果在[min,max]范围内
        return Math.max(min, Math.min(m, max));
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