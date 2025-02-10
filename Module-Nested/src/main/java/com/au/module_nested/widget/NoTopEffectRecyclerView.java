package com.au.module_nested.widget;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.EdgeEffect;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.RecyclerView;

/**
 * @author au
 * @date 2024/8/7 11:54
 * 研究许久，如果想要下拉刷新的嵌套RecyclerView的Layout，则禁用内部的RecyclerView的edge效果即可。
 * rcv1.3.2的库，额外处理了edge consume掉了nest的距离。因此，我们这里搞一个假的空Effect进去，也符合我们
 *  下拉刷新的本质.
 */
public class NoTopEffectRecyclerView extends RecyclerView {
    public NoTopEffectRecyclerView(@NonNull Context context) {
        super(context);
        init(context);
    }

    public NoTopEffectRecyclerView(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    public NoTopEffectRecyclerView(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context);
    }

    private void init(Context context) {
        setEdgeEffectFactory(new NoEdgeTopEffectFactory(context));
    }

    public static final class DefaultEdgeEffectFactory extends EdgeEffectFactory {
        @NonNull
        @Override
        public EdgeEffect createEdgeEffect(@NonNull RecyclerView view, int direction) {
            return super.createEdgeEffect(view, direction);
        }
    }

    public static final class NoEdgeTopEffectFactory extends EdgeEffectFactory {
        private final NoEdgeEffect effect;
        private final DefaultEdgeEffectFactory defaultFactory = new DefaultEdgeEffectFactory();

        public NoEdgeTopEffectFactory(Context context) {
            this.effect = new NoEdgeEffect(context);
        }

        @NonNull
        @Override
        protected EdgeEffect createEdgeEffect(@NonNull RecyclerView view, int direction) {
            if (direction == EdgeEffectFactory.DIRECTION_TOP) {
                return effect;
            }
            return defaultFactory.createEdgeEffect(view, direction);
        }
    }
}
