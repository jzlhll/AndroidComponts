package com.allan.nested.widget;

import android.content.Context;
import android.graphics.Canvas;
import android.os.Build;
import android.util.AttributeSet;
import android.widget.EdgeEffect;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;

/**
 * @author au
 * @date :2023/12/26 14:42
 * @description: 因为RecyclerView内部做了边缘(edge)效果处理，
 * 在1.2.1和1.3.2对于边缘处理不同，进而导致parentView无法监听到Nest事件。
 * 研究许久，如果想要下拉刷新的嵌套RecyclerView的Layout，则禁用内部的RecyclerView的edge效果即可。
 */
public class NoEdgeEffect extends EdgeEffect {
    public NoEdgeEffect(Context context) {
        super(context);
    }

    @RequiresApi(api = Build.VERSION_CODES.S)
    public NoEdgeEffect(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    public void setSize(int width, int height) {}

    @Override
    public boolean isFinished() {
        return true;
    }

    @Override
    public void onPull(float deltaDistance) {}

    @Override
    public void onPull(float deltaDistance, float displacement) {}

    @Override
    public float onPullDistance(float deltaDistance, float displacement) {
        return 0f;
    }

    @Override
    public void onRelease() {}

    @Override
    public void onAbsorb(int velocity) {}

    @Override
    public boolean draw(Canvas canvas) {
        return false;
    }
}
