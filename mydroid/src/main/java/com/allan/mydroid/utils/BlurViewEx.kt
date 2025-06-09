package com.allan.mydroid.utils

import android.graphics.Outline
import android.os.Build
import android.view.View
import android.view.ViewGroup
import android.view.ViewOutlineProvider
import com.au.module_android.utils.dp
import eightbitlab.com.blurview.BlurView
import eightbitlab.com.blurview.RenderEffectBlur

class BlurViewEx(private val blurView: BlurView,
                 private val blurViewCornerRadius: Int) {

    var legacyDrawable = com.allan.mydroid.R.drawable.legacy_blur_bg
    val overlayColor:Int
        get() {
            return blurView.context.getColor(com.allan.mydroid.R.color.blur_overlay)
        }

    val viewOutlineProvider = object : ViewOutlineProvider() {
        override fun getOutline(view: View, outline: Outline) {
            outline.setRoundRect(0, 0, view.width, view.height, blurViewCornerRadius.toFloat().dp)
        }
    }

    fun setBlur(root: ViewGroup) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) { //高版本使用模糊，低版本纯半透
            //View decorView = getWindow().getDecorView();
            // ViewGroup you want to start blur from. Choose root as close to BlurView in hierarchy as possible.
            //ViewGroup rootView = (ViewGroup) decorView.findViewById(android.R.id.content);
            // Optional:
            // Set drawable to draw in the beginning of each blurred frame.
            // Can be used in case your layout has a lot of transparent space and your content
            // gets a too low alpha value after blur is applied.
            //Drawable windowBackground = decorView.getBackground();
            blurView.setupWith(root, RenderEffectBlur()) // or RenderEffectBlur
                //.setFrameClearDrawable(windowBackground) // Optional
                .setBlurRadius(blurViewCornerRadius * 3f)

            blurView.setOverlayColor(overlayColor)

            blurView.outlineProvider = viewOutlineProvider
            blurView.clipToOutline = true
        } else {
            blurView.setBackgroundResource(legacyDrawable)
        }
    }

    fun setBlur(root: ViewGroup, blurRadius:Float) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) { //高版本使用模糊，低版本纯半透
            //View decorView = getWindow().getDecorView();
            // ViewGroup you want to start blur from. Choose root as close to BlurView in hierarchy as possible.
            //ViewGroup rootView = (ViewGroup) decorView.findViewById(android.R.id.content);
            // Optional:
            // Set drawable to draw in the beginning of each blurred frame.
            // Can be used in case your layout has a lot of transparent space and your content
            // gets a too low alpha value after blur is applied.
            //Drawable windowBackground = decorView.getBackground();
            blurView.setupWith(root, RenderEffectBlur()) // or RenderEffectBlur
                //.setFrameClearDrawable(windowBackground) // Optional
                .setBlurRadius(blurRadius)

            blurView.outlineProvider = viewOutlineProvider
            blurView.clipToOutline = true
        } else {
            blurView.setBackgroundResource(legacyDrawable)
        }
    }
}