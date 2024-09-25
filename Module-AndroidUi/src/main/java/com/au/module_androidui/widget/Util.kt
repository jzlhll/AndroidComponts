package com.au.module_androidui.widget

import android.widget.SeekBar
import androidx.core.content.res.ResourcesCompat

fun SeekBar.enableAndStyle() {
    isEnabled = true
    thumb = ResourcesCompat.getDrawable(context.resources, com.au.module_androidui.R.drawable.au_seekbar_thumb, null)
    progressDrawable = ResourcesCompat.getDrawable(context.resources, com.au.module_androidui.R.drawable.au_seekbar_progress_bg, null)
}

fun SeekBar.disableAndStyle() {
    isEnabled = false
    thumb = ResourcesCompat.getDrawable(context.resources, com.au.module_androidui.R.drawable.au_seekbar_thumb_disable, null)
    progressDrawable = ResourcesCompat.getDrawable(context.resources, com.au.module_androidui.R.drawable.au_seekbar_progress_bg_disable, null)
}