package com.au.module_android.ui.views

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.widget.RelativeLayout
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.au.module_android.R
import com.au.module_android.click.PaddingClickListener
import com.au.module_android.databinding.ToolbarActivityLayoutBinding
import com.au.module_android.utils.asOrNull
import com.au.module_android.widget.CustomToolbar

internal fun createToolbarLayout(context: Context, contentView:View, hasNavIcon: Boolean) : ToolbarActivityLayoutBinding {
    val vb = ToolbarActivityLayoutBinding.inflate(LayoutInflater.from(context))
    val rl = vb.root
    val toolbar = vb.toolbar
    rl.addView(contentView, RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.MATCH_PARENT, RelativeLayout.LayoutParams.MATCH_PARENT).also {
        it.addRule(RelativeLayout.BELOW, toolbar.id)
    })
    initToolBar(context, toolbar, hasNavIcon)
    return vb
}

fun initToolBar(context: Context, toolbar: CustomToolbar, hasNavIcon:Boolean) {
    if (context !is AppCompatActivity) throw RuntimeException()
    toolbar.apply {
        if (context.supportActionBar == null) {
            context.setSupportActionBar(this)
        }
        //设置标题居中
        isTitleCentered = true
        setBackgroundResource(R.color.color_toolbar_background)
        if (hasNavIcon) {
            val drawable = ContextCompat.getDrawable(context, com.au.module_androidcolor.R.drawable.toolbarLogo)
            navigationIcon = drawable
        }
        title = context.title
        popupTheme = com.au.module_androidcolor.R.style.toolbarPopupTheme
        setTitleTextAppearance(context, com.au.module_androidcolor.R.style.toolbarTitleTextAppearance)
        setNavigationOnClickListener(PaddingClickListener(null) {
            it.context.asOrNull<AppCompatActivity>()?.onBackPressedDispatcher?.onBackPressed()
        })
    }
}