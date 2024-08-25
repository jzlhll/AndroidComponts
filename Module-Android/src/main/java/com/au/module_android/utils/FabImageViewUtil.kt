package com.au.module_android.utils

import android.widget.ImageView

/**
 * @author au
 * @date :2024/3/11 10:04
 * @description:
 */
class FabImageViewUtil(private val imageView:ImageView) {
    fun show() {
        imageView.visible()
        val anim = imageView.animate()
        anim.cancel()
        anim.alpha(1f)
            .setDuration(300)
            .start()
    }

    fun hide() {
        imageView.visible()
        val anim = imageView.animate()
        anim.cancel()
        anim.alpha(0f)
            .setDuration(300)
            .withEndAction {
                imageView.invisible()
            }
            .start()
    }
    
    fun showDirectly() {
        imageView.animate().cancel()
        imageView.visible()
        imageView.alpha = 1f
    }

    fun hideDirectly() {
        imageView.animate().cancel()
        imageView.invisible()
        imageView.alpha = 0f
    }
}