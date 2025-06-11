package com.au.logsystem.view;

import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.widget.RelativeLayout;

public class DragRelativeLayout extends RelativeLayout {
    private float lastX, lastY;
    private long mLastClickTs;

    public DragRelativeLayout(Context context) {
        super(context);
        setClickable(true); // 确保View可点击
    }

    public DragRelativeLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
        setClickable(true); // 确保View可点击
    }

    public DragRelativeLayout(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        setClickable(true); // 确保View可点击
    }

    @Override
    public boolean performClick() {
        return super.performClick();
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        float x = event.getRawX();
        float y = event.getRawY();

        switch(event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                lastX = x;
                lastY = y;
                mLastClickTs = System.currentTimeMillis();
                break;

            case MotionEvent.ACTION_MOVE:
                float deltaX = x - lastX;
                float deltaY = y - lastY;
                setTranslationX(getTranslationX() + deltaX);
                setTranslationY(getTranslationY() + deltaY);
                lastX = x;
                lastY = y;
                break;

            case MotionEvent.ACTION_UP:
                var newTs = System.currentTimeMillis();
                if (newTs - mLastClickTs < 100) {
                    performClick(); // 触发点击事件
                    mLastClickTs = newTs;
                }
                break;
        }
        return true;
    }
}

