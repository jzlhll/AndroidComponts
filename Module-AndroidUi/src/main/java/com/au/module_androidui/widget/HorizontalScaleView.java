package com.au.module_androidui.widget;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Rect;
import android.graphics.RectF;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.VelocityTracker;
import android.view.View;
import android.view.ViewConfiguration;

import androidx.annotation.NonNull;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;


/**
 * Copyright (c) 2017, Bongmi
 * All rights reserved
 * Author: xuyuanyi@bongmi.com
 */

public class HorizontalScaleView extends View {
    /**
     * Copyright (c) 2017, Bongmi
     * All rights reserved
     * Author: xuyuanyi@bongmi.com
     */

    public interface OnValueChangeListener {
        void onValueChanged(float value);
    }

    private final int SCALE_WIDTH_BIG = 4;//大刻度线宽度
    private final int SCALE_WIDTH_SMALL = 2;//小刻度线宽度
    private final int LINE_WIDTH = 6;//指针线宽度

    private int rectPadding = 40;//圆角矩形间距
    private int rectWidth;//圆角矩形宽
    private int rectHeight;//圆角矩形高

    private int maxScaleLength;//大刻度长度
    private int midScaleLength;//中刻度长度
    private int minScaleLength;//小刻度长度
    private int scaleSpace;//刻度间距
    private int scaleSpaceUnit;//每大格刻度间距
    private int height, width;//view高宽
    private int ruleHeight;//刻度尺高

    private int max;//最大刻度
    private int min;//最小刻度
    private int borderLeft, borderRight;//左右边界值坐标
    private float midX;//当前中心刻度x坐标
    private float originMidX;//初始中心刻度x坐标
    private float minX;//最小刻度x坐标,从最小刻度开始画刻度

    private float lastX;

    private float originValue;//初始刻度对应的值
    private float currentValue;//当前刻度对应的值

    private Paint paint;//画笔

    private Context context;

    private String descri = "体重";//描述
    private String unit = "kg";//刻度单位

    private VelocityTracker velocityTracker;//速度监测
    private float velocity;//当前滑动速度
    private float a = 1000000;//加速度
    private boolean continueScroll;//是否继续滑动

    private boolean isMeasured;

    private OnValueChangeListener onValueChangeListener;

    private final Handler mHandler = new Handler(Looper.getMainLooper()) {
        @Override
        public void handleMessage(@NonNull Message msg) {
            if (null != onValueChangeListener) {
                float v = (float) (Math.round(currentValue * 10)) / 10;//保留一位小数
                onValueChangeListener.onValueChanged(v);
            }
        }
    };

    public HorizontalScaleView(Context context) {
        super(context);
        this.context = context;
        init();
    }

    public HorizontalScaleView(Context context, AttributeSet attrs) {
        super(context, attrs);
        this.context = context;
        init();
    }

    public HorizontalScaleView(Context context, AttributeSet attrs, int defStyleAttr) {
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
    public void setRange(int min, int max) {
        this.min = min;
        this.max = max;
        originValue = (max + min) / 2f;
        currentValue = originValue;

        cacheRectList.clear();
        var newRectList = new ArrayList<Rect>();
        for (int i = min; i <= max; i++) {
            newRectList.add(new Rect());
        }
        cacheRectList.addAll(newRectList);
    }

    //设置刻度单位
    public void setUnit(String unit) {
        this.unit = unit;
    }

    //设置刻度描述
    public void setDescri(String descri) {
        this.descri = descri;
    }

    //设置value变化监听
    public void setOnValueChangeListener(OnValueChangeListener onValueChangeListener) {
        this.onValueChangeListener = onValueChangeListener;
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        if (!isMeasured) {
            width = getMeasuredWidth();
            height = getMeasuredHeight();
            ruleHeight = height * 2 / 3;
            maxScaleLength = height / 10;
            midScaleLength = height / 12;
            minScaleLength = maxScaleLength / 2;
            scaleSpace = Math.max(height / 60, 8);
            scaleSpaceUnit = scaleSpace * 10 + SCALE_WIDTH_BIG + SCALE_WIDTH_SMALL * 9;
            rectWidth = scaleSpaceUnit;
            rectHeight = scaleSpaceUnit / 2;

            var l = ((min + max) / 2 - min) * scaleSpaceUnit;
            var r = ((min + max) / 2 - min) * scaleSpaceUnit;
            var hw = width / 2;
            borderLeft = hw - l;
            borderRight = hw + r;
            midX = (borderLeft + borderRight) / 2f;
            originMidX = midX;
            minX = borderLeft;
            isMeasured = true;

            for (int j = 1; j < 10; j++) {
                if (j == 5) {
                    continue;
                }
                scaleWidthIndexJ[j] = (SCALE_WIDTH_SMALL + scaleSpace) * j;
            }
        }
    }

    private final int[] scaleWidthIndexJ = new int[10];

    private final List<Rect> cacheRectList = new ArrayList<>();
    private final RectF rRect = new RectF();
    private final Rect mRect1 = new Rect();
    private final Rect mRect2 = new Rect();
    private final Path mSmallTripPath = new Path();

    @Override
    protected void onDraw(@NonNull Canvas canvas) {
        super.onDraw(canvas);
        //画刻度线
        var hfScSpaceUnit = scaleSpaceUnit / 2f;
        var hfScaleWidthBig = SCALE_WIDTH_BIG / 2f;
        var halfWidth = width / 2f;

        for (int i = min; i <= max; i++) {
            var startX = minX + (i - min) * scaleSpaceUnit;

            //画刻度数字
            Rect rect = cacheRectList.get(i - min);
            String str = String.valueOf(i);
            paint.setColor(Color.BLACK);
            paint.setTextSize(40);
            paint.getTextBounds(str, 0, str.length(), rect);
            int w = rect.width();
            int h = rect.height();
            canvas.drawText(str, startX - w / 2f - hfScaleWidthBig, ruleHeight - maxScaleLength - h - minScaleLength / 2f, paint);
            //画大刻度线
            paint.setStrokeWidth(SCALE_WIDTH_BIG);
            canvas.drawLine(startX, ruleHeight - maxScaleLength, startX, ruleHeight, paint);

            if (i == max) {
                continue;//最后一条不画中小刻度线
            }
            //画中刻度线
            paint.setStrokeWidth(SCALE_WIDTH_SMALL);
            canvas.drawLine(startX + hfScSpaceUnit, ruleHeight, startX + hfScSpaceUnit, ruleHeight - midScaleLength, paint);
            //画小刻度线
            for (int j = 1; j < 10; j++) {
                if (j == 5) {
                    continue;
                }
                var smallX = startX + scaleWidthIndexJ[j];
                canvas.drawLine(smallX, ruleHeight, smallX, ruleHeight - minScaleLength, paint);
            }
        }

        //画竖线
        paint.setStrokeWidth(LINE_WIDTH);
        paint.setColor(Color.GRAY);
        canvas.drawLine(minX + hfScaleWidthBig, ruleHeight + LINE_WIDTH / 2f, minX + (max - min) * scaleSpaceUnit - hfScaleWidthBig, ruleHeight + LINE_WIDTH / 2f, paint);
        //画指针线
        paint.setColor(Color.RED);
        canvas.drawLine(halfWidth, 0, halfWidth, ruleHeight, paint);
        //画圆角矩形
        paint.setStyle(Paint.Style.FILL);
        var ruleHeightPadding = ruleHeight + rectPadding;

        RectF r = rRect;
        r.left = halfWidth - rectWidth / 2f;
        r.top = ruleHeightPadding;
        r.right = halfWidth + rectWidth / 2f;
        r.bottom = ruleHeightPadding + rectHeight;
        canvas.drawRoundRect(r, 10, 10, paint);
        //画小三角形指针
        Path path = mSmallTripPath;
        path.moveTo(halfWidth - scaleSpace * 2, ruleHeightPadding);
        path.lineTo(halfWidth, ruleHeightPadding - 10);
        path.lineTo(halfWidth + scaleSpace * 2, ruleHeightPadding);
        path.close();
        canvas.drawPath(path, paint);
        //绘制文字
        paint.setColor(Color.BLACK);
        Rect rect1 = mRect1;
        paint.getTextBounds(descri, 0, descri.length(), rect1);
        int w1 = rect1.width();
        int h1 = rect1.height();
        canvas.drawText(descri, halfWidth - w1 / 2f, ruleHeightPadding + rectHeight + h1 + 10, paint);
        //绘制当前刻度值数字
        paint.setColor(Color.WHITE);
        float v = (float) (Math.round(currentValue * 10)) / 10;//保留一位小数
        String value = String.valueOf(v) + unit;
        Rect rect2 = mRect2;
        paint.getTextBounds(value, 0, value.length(), rect2);
        int w2 = rect2.width();
        int h2 = rect2.height();
        canvas.drawText(value, halfWidth - w2 / 2f, ruleHeightPadding + rectHeight / 2f + h2 / 2f, paint);
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
    private void calculateCurrentScale() {
        float offsetTotal = midX - originMidX;
        int offsetBig = (int) (offsetTotal / scaleSpaceUnit);//移动的大刻度数
        float offsetS = offsetTotal % scaleSpaceUnit;
        int offsetSmall = (new BigDecimal(offsetS / (scaleSpace + SCALE_WIDTH_SMALL)).setScale(0, RoundingMode.HALF_UP)).intValue();//移动的小刻度数 四舍五入取整
        float offset = offsetBig + offsetSmall * 0.1f;
        if (originValue - offset > max) {
            currentValue = max;
        } else if (originValue - offset < min) {
            currentValue = min;
        } else {
            currentValue = originValue - offset;
        }
        mHandler.sendEmptyMessage(0);
    }

    private void confirmBoarderWhenMoving(int offsetX) {
        var newMinX = minX - offsetX;
        var newMidX = midX - offsetX;

        if (newMidX < borderLeft) {
            newMidX = borderLeft;
            newMinX = borderLeft - (borderRight - borderLeft) / 2f;
        } else if (newMidX > borderRight) {
            newMidX = borderRight;
            newMinX = borderLeft + (borderRight - borderLeft) / 2f;
        }

        minX = newMinX;
        midX = newMidX;
    }

    //指针线超出范围时 重置回边界处
    private void confirmBorder() {
        if (midX < borderLeft) {
            midX = borderLeft;
            minX = borderLeft - (borderRight - borderLeft) / 2f;
            postInvalidate();
        } else if (midX > borderRight) {
            midX = borderRight;
            minX = borderLeft + (borderRight - borderLeft) / 2f;
            postInvalidate();
        }
    }

    //手指抬起后继续惯性滑动
    private void continueScroll() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                float velocityAbs = 0;//速度绝对值
                if (velocity > 0 && continueScroll) {
                    velocity -= 50;
                    minX += velocity * velocity / a;
                    midX += velocity * velocity / a;
                    velocityAbs = velocity;
                } else if (velocity < 0 && continueScroll) {
                    velocity += 50;
                    minX -= velocity * velocity / a;
                    midX -= velocity * velocity / a;
                    velocityAbs = -velocity;
                }
                calculateCurrentScale();
                confirmBorder();
                postInvalidate();
                if (continueScroll && velocityAbs > 0) {
                    post(this);
                } else {
                    continueScroll = false;
                }
            }
        }).start();
    }
}