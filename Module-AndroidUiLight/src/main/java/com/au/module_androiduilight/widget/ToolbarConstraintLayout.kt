package com.au.module_androiduilight.widget

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.widget.LinearLayout
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContextCompat
import com.au.module_android.widget.CustomToolBar

/**
 * @author allan
 * @date :2024/8/8 17:53
 * @description:
 */
class ToolbarConstraintLayout : ConstraintLayout {
    private lateinit var toolbar: Toolbar

    constructor(context: Context) : super(context) {
        initWithToolbar()
    }

    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs) {
        initWithToolbar()
    }

    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(context, attrs, defStyleAttr) {
        initWithToolbar()
    }

    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int, defStyleRes: Int) : super(context, attrs, defStyleAttr, defStyleRes) {
        initWithToolbar()
    }

    private fun initWithToolbar() {
        val toolbarParam = if (isDarkMode) {
            LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, dp48).also {
                it.marginStart = 9.dp
                it.marginEnd = 12.dp
            }
        } else {
            LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, dp48).also {
                it.marginEnd = 8.dp
            }
        }
        addView(createToolBar(LayoutInflater.from(context)), lp)
    }

    private fun createToolBar(inflater: LayoutInflater): Toolbar {
        val ctx = inflater.context
        return CustomToolBar(ctx).apply {
            toolbar = this
            if (ctx is AppCompatActivity) {
                if (ctx.supportActionBar == null) {
                    ctx.setSupportActionBar(this)
                }
                //设置标题居中
                isTitleCentered = true
                setBackgroundResource(if(isDarkMode) R.color.toolBarBackgroundDark else R.color.toolBarBackground)
                val drawable = ContextCompat.getDrawable(ctx, if(isDarkMode)R.drawable.toolbarLogoDark else R.drawable.toolbarLogo)
                navigationIcon = drawable
                title = ctx.title
                popupTheme = if(isDarkMode) R.style.toolbarPopupThemeDark else R.style.toolbarPopupTheme
                setTitleTextAppearance(ctx, if(isDarkMode) R.style.toolbarTitleTextAppearanceDark else R.style.toolbarTitleTextAppearance)
                setNavigationOnClickListener(object : IOnSingleClickListener {
                    override fun onSingleClick(view: View) {
                        super.onSingleClick(view)
                        ctx.onBackPressed()
                    }
                })
            }
        }
    }
}
