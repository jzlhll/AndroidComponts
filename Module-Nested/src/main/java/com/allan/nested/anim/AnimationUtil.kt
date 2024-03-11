package com.allan.nested.anim

import android.animation.Keyframe
import android.animation.ObjectAnimator
import android.animation.PropertyValuesHolder
import android.view.View

/**
 * @author au
 * @date :2023/11/6 14:43
 * @description:
 */
class AnimationUtil {
    fun bottomSwitchBtnAnim(view: View?): ObjectAnimator {
        val pvhScaleX = PropertyValuesHolder.ofKeyframe(
            View.SCALE_X,
            Keyframe.ofFloat(0f, 1f),
            Keyframe.ofFloat(.1f, 1.05f),
            Keyframe.ofFloat(.3f, 1.08f),
            Keyframe.ofFloat(.4f, 1.1f),
            Keyframe.ofFloat(.5f, 1.1f),
            Keyframe.ofFloat(.6f, 1.1f),
            Keyframe.ofFloat(.7f, 1.08f),
            Keyframe.ofFloat(.9f, 1.05f),
            Keyframe.ofFloat(1f, 1f)
        )
        val pvhScaleY = PropertyValuesHolder.ofKeyframe(
            View.SCALE_Y,
            Keyframe.ofFloat(0f, 1f),
            Keyframe.ofFloat(.1f, 1.05f),
            Keyframe.ofFloat(.3f, 1.08f),
            Keyframe.ofFloat(.4f, 1.1f),
            Keyframe.ofFloat(.5f, 1.1f),
            Keyframe.ofFloat(.6f, 1.1f),
            Keyframe.ofFloat(.7f, 1.08f),
            Keyframe.ofFloat(.9f, 1.05f),
            Keyframe.ofFloat(1f, 1f)
        )
        return ObjectAnimator.ofPropertyValuesHolder(view, pvhScaleX, pvhScaleY).setDuration(300)
    }
}