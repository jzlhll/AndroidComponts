package com.au.module_android.widget

import android.content.Context
import android.util.AttributeSet
import android.view.View
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.au.module_android.click.PaddingClickListener
import com.au.module_android.fontutil.setFontFromAsset
import com.au.module_android.utils.asOrNull
import com.au.module_android.utils.forEachChild
import com.au.module_android.utils.logd
import com.google.android.material.appbar.MaterialToolbar

/**
 * @author allan
 * @date :2024/8/8 17:57
 * @description:
 */
class CustomToolBar @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null
) : MaterialToolbar(context, attrs) {
    var fontMode = FontMode.MID

    init {
        if (context is AppCompatActivity) {
            context.setSupportActionBar(this)
            this.title = context.title
            this.setNavigationOnClickListener(PaddingClickListener(paddingTime = null) { v ->
                val activity = v.context.asOrNull<AppCompatActivity>()
                activity?.onBackPressedDispatcher?.onBackPressed()
            })
        }
    }

    fun extraSetting(toolBar: CustomToolBar) {
        /**

        //设置标题居中
        isTitleCentered = true
        setBackgroundResource(if(isDarkMode) R.color.toolBarBackgroundDark else R.color.toolBarBackground)
        val drawable = ContextCompat.getDrawable(ctx, if(isDarkMode)R.drawable.toolbarLogoDark else R.drawable.toolbarLogo)
        popupTheme = if(isDarkMode) R.style.toolbarPopupThemeDark else R.style.toolbarPopupTheme
        navigationIcon = drawable
        setTitleTextAppearance(ctx, if(isDarkMode) R.style.toolbarTitleTextAppearanceDark else R.style.toolbarTitleTextAppearance)

         */
    }

    override fun addView(child: View?) {
        super.addView(child)
        logd { "addView....child $child" }
        child.forEachChild {
            if (it is TextView) {
                it.setFontFromAsset(context, fontMode, false)
            }
        }
    }
}