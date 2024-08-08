package com.au.module_androidex.toolbar

import android.view.LayoutInflater
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.core.content.ContextCompat
import com.au.module_android.widget.CustomToolBar

/**
 * @author allan
 * @date :2024/8/8 18:07
 * @description:
 */
class AbsToolbarCreator(private val inflater: LayoutInflater) {
    val toolBar:CustomToolBar = CustomToolBar(inflater.context)

    init {
        val ctx = inflater.context
        if (ctx is AppCompatActivity) {

            //设置标题居中
            isTitleCentered = true
            setBackgroundResource(if(isDarkMode) R.color.toolBarBackgroundDark else R.color.toolBarBackground)
            val drawable = ContextCompat.getDrawable(ctx, if(isDarkMode)R.drawable.toolbarLogoDark else R.drawable.toolbarLogo)
            popupTheme = if(isDarkMode) R.style.toolbarPopupThemeDark else R.style.toolbarPopupTheme
            navigationIcon = drawable
            setTitleTextAppearance(ctx, if(isDarkMode) R.style.toolbarTitleTextAppearanceDark else R.style.toolbarTitleTextAppearance)

        }
    }

    abstract fun setBackgroundResource(toolbar: Toolbar)
}