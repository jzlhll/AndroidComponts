package com.au.module_android.ui.toolbar

import android.content.Context
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.au.module_android.R
import com.au.module_android.click.PaddingClickListener
import com.au.module_android.utils.asOrNull
import com.au.module_android.utils.dp
import com.au.module_android.widget.CustomToolbar

/**
 * 当需要拼接
 */
internal fun createToolbarLayout(context: Context, contentView:View) : Pair<CustomToolbar, LinearLayout> {
    val toolbar = createToolBar(context)
    val ll = LinearLayout(context).apply {
        orientation = LinearLayout.VERTICAL
        layoutParams = ViewGroup.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.MATCH_PARENT,
        )

        val toolbarHeight = Math.round(context.resources.getDimension(R.dimen.toolbar_height))
        val toolbarParam = LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, toolbarHeight).also {
            it.marginEnd = 8.dp
        }

        addView(toolbar, toolbarParam)

        //如果content没有被贴入parent则add一下。
        val lp = contentView.layoutParams ?: ViewGroup.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.MATCH_PARENT
        )
        addView(contentView, lp)
    }
    return toolbar to ll
}

private fun createToolBar(context: Context): CustomToolbar {
    if (context !is AppCompatActivity) throw RuntimeException()
    return CustomToolbar(context).apply {
        if (context.supportActionBar == null) {
            context.setSupportActionBar(this)
        }
        //设置标题居中
        isTitleCentered = true
        setBackgroundResource(R.color.color_toolbar_background)
        val drawable = ContextCompat.getDrawable(context, com.au.module_androidcolor.R.drawable.toolbarLogo)
        navigationIcon = drawable
        title = context.title
        popupTheme = com.au.module_androidcolor.R.style.toolbarPopupTheme
        setTitleTextAppearance(context, com.au.module_androidcolor.R.style.toolbarTitleTextAppearance)
        setNavigationOnClickListener(PaddingClickListener(null) {
            it.context.asOrNull<AppCompatActivity>()?.onBackPressedDispatcher?.onBackPressed()
        })
    }
}