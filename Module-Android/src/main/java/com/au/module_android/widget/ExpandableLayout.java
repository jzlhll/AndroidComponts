package com.au.module_android.widget;

import android.animation.Animator;
import android.animation.TimeInterpolator;
import android.animation.ValueAnimator;
import android.content.Context;
import android.content.res.Configuration;
import android.content.res.TypedArray;
import android.os.Bundle;
import android.os.Parcelable;
import android.util.AttributeSet;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.LinearLayout;

import androidx.interpolator.view.animation.FastOutLinearInInterpolator;

import com.au.module_android.R;


/**
 * 由于https://github.com/cachapa/ExpandableLayout 2.2k星的源码。
 * 不再维护，挪到本代码中，自行维护。
 *
 * Also supported are el_duration and el_expanded tags, for specifying the duration of the animation and
 * whether the layout should start expanded, respectively.
 * el_parallax can be set to a value between 0 and 1 to control how the child view is translated during the expansion.
 * To trigger the animation, simply grab a reference to the ExpandableLayout
 * from your Java code and and call either of expand(), collapse() or toggle().
 *
 * 修改：去除原本动画算法，使用系统的线性算法，更为流畅
 */
public class ExpandableLayout extends FrameLayout {
    public interface State {
        int COLLAPSED = 0;
        int COLLAPSING = 1;
        int EXPANDING = 2;
        int EXPANDED = 3;
    }

    public static final String KEY_SUPER_STATE = "super_state";
    public static final String KEY_EXPANSION = "expansion";

    public static final int HORIZONTAL = 0;
    public static final int VERTICAL = 1;

    private static final int DEFAULT_DURATION = 300;

    private int duration = DEFAULT_DURATION;
    private float parallax;
    private float expansion;
    private int orientation;
    private int state;

    private ValueAnimator animator;

    private OnExpansionUpdateListener listener;

    public ExpandableLayout(Context context) {
        this(context, null);
    }

    public ExpandableLayout(Context context, AttributeSet attrs) {
        super(context, attrs);

        if (attrs != null) {
            TypedArray a = getContext().obtainStyledAttributes(attrs, R.styleable.ExpandableLayout);
            duration = a.getInt(R.styleable.ExpandableLayout_el_duration, DEFAULT_DURATION);
            expansion = a.getBoolean(R.styleable.ExpandableLayout_el_expanded, false) ? 1 : 0;
            orientation = a.getInt(R.styleable.ExpandableLayout_android_orientation, VERTICAL);
            parallax = a.getFloat(R.styleable.ExpandableLayout_el_parallax, 1);
            a.recycle();

            state = expansion == 0 ? State.COLLAPSED : State.EXPANDED;
            setParallax(parallax);
        }
    }

    @Override
    protected Parcelable onSaveInstanceState() {
        Parcelable superState = super.onSaveInstanceState();
        Bundle bundle = new Bundle();

        expansion = isExpanded() ? 1 : 0;

        bundle.putFloat(KEY_EXPANSION, expansion);
        bundle.putParcelable(KEY_SUPER_STATE, superState);

        return bundle;
    }

    @Override
    protected void onRestoreInstanceState(Parcelable parcelable) {
        Bundle bundle = (Bundle) parcelable;
        expansion = bundle.getFloat(KEY_EXPANSION);
        state = expansion == 1 ? State.EXPANDED : State.COLLAPSED;
        Parcelable superState = bundle.getParcelable(KEY_SUPER_STATE);

        super.onRestoreInstanceState(superState);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);

        int width = getMeasuredWidth();
        int height = getMeasuredHeight();

        int size = orientation == LinearLayout.HORIZONTAL ? width : height;

        setVisibility(expansion == 0 && size == 0 ? GONE : VISIBLE);

        int expansionDelta = size - Math.round(size * expansion);
        if (parallax > 0) {
            float parallaxDelta = expansionDelta * parallax;
            for (int i = 0; i < getChildCount(); i++) {
                View child = getChildAt(i);
                if (orientation == HORIZONTAL) {
                    int direction = -1;
                    if (getLayoutDirection() == LAYOUT_DIRECTION_RTL) {
                        direction = 1;
                    }
                    child.setTranslationX(direction * parallaxDelta);
                } else {
                    child.setTranslationY(-parallaxDelta);
                }
            }
        }

        if (orientation == HORIZONTAL) {
            setMeasuredDimension(width - expansionDelta, height);
        } else {
            setMeasuredDimension(width, height - expansionDelta);
        }
    }

    @Override
    protected void onConfigurationChanged(Configuration newConfig) {
        if (animator != null) {
            animator.cancel();
        }
        super.onConfigurationChanged(newConfig);
    }

    /**
     * Get expansion state
     *
     * @return one of {@link State}
     */
    public int getState() {
        return state;
    }

    public boolean isExpanded() {
        return state == State.EXPANDING || state == State.EXPANDED;
    }

    public void toggle() {
        toggle(true);
    }

    public void toggle(boolean animate) {
        if (isExpanded()) {
            collapse(animate);
        } else {
            expand(animate);
        }
    }

    public void expand() {
        expand(true);
    }

    public void expand(boolean animate) {
        setExpanded(true, animate);
    }

    public void collapse() {
        collapse(true);
    }

    public void collapse(boolean animate) {
        setExpanded(false, animate);
    }

    /**
     * Convenience method - same as calling setExpanded(expanded, true)
     */
    public void setExpanded(boolean expand) {
        setExpanded(expand, true);
    }

    public void setExpanded(boolean expand, boolean animate) {
        if (expand == isExpanded()) {
            return;
        }

        int targetExpansion = expand ? 1 : 0;
        if (animate) {
            animateSize(targetExpansion);
        } else {
            setExpansion(targetExpansion);
        }
    }

    public int getDuration() {
        return duration;
    }

    public void setDuration(int duration) {
        this.duration = duration;
    }

    public float getExpansion() {
        return expansion;
    }

    public void setExpansion(float expansion) {
        if (this.expansion == expansion) {
            return;
        }

        // Infer state from previous value
        float delta = expansion - this.expansion;
        if (expansion == 0) {
            state = State.COLLAPSED;
        } else if (expansion == 1) {
            state = State.EXPANDED;
        } else if (delta < 0) {
            state = State.COLLAPSING;
        } else if (delta > 0) {
            state = State.EXPANDING;
        }

        setVisibility(state == State.COLLAPSED ? GONE : VISIBLE);
        this.expansion = expansion;
        requestLayout();

        if (listener != null) {
            listener.onExpansionUpdate(expansion, state);
        }
    }

    public float getParallax() {
        return parallax;
    }

    public void setParallax(float parallax) {
        // Make sure parallax is between 0 and 1
        parallax = Math.min(1, Math.max(0, parallax));
        this.parallax = parallax;
    }

    public int getOrientation() {
        return orientation;
    }

    public void setOrientation(int orientation) {
        if (orientation < 0 || orientation > 1) {
            throw new IllegalArgumentException("Orientation must be either 0 (horizontal) or 1 (vertical)");
        }
        this.orientation = orientation;
    }

    public void setOnExpansionUpdateListener(OnExpansionUpdateListener listener) {
        this.listener = listener;
    }

    private void animateSize(int targetExpansion) {
        if (animator != null) {
            animator.cancel();
            animator = null;
        }

        animator = ValueAnimator.ofFloat(expansion, targetExpansion);
        if (mCreateInterpolator != null) {
            animator.setInterpolator(mCreateInterpolator.createInterpolator());
        } else {
            animator.setInterpolator(new FastOutLinearInInterpolator());
        }
        animator.setDuration(duration);

        animator.addUpdateListener(valueAnimator -> setExpansion((float) valueAnimator.getAnimatedValue()));

        animator.addListener(new ExpansionListener(targetExpansion));

        animator.start();
    }

    public void setCreateInterpolator(ICreateInterpolator creator) {
        mCreateInterpolator = creator;
    }

    private ICreateInterpolator mCreateInterpolator;

    public interface ICreateInterpolator {
        TimeInterpolator createInterpolator();
    }

    public interface OnExpansionUpdateListener {
        /**
         * Callback for expansion updates
         *
         * @param expansionFraction Value between 0 (collapsed) and 1 (expanded) representing the the expansion progress
         * @param state             One of {@link State} repesenting the current expansion state
         */
        void onExpansionUpdate(float expansionFraction, int state);
    }

    private class ExpansionListener implements Animator.AnimatorListener {
        private int targetExpansion;
        private boolean canceled;

        public ExpansionListener(int targetExpansion) {
            this.targetExpansion = targetExpansion;
        }

        @Override
        public void onAnimationStart(Animator animation) {
            state = targetExpansion == 0 ? State.COLLAPSING : State.EXPANDING;
        }

        @Override
        public void onAnimationEnd(Animator animation) {
            if (!canceled) {
                state = targetExpansion == 0 ? State.COLLAPSED : State.EXPANDED;
                setExpansion(targetExpansion);
            }
        }

        @Override
        public void onAnimationCancel(Animator animation) {
            canceled = true;
        }

        @Override
        public void onAnimationRepeat(Animator animation) {
        }
    }
}
