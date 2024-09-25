package com.au.module_android.ui.toolbar

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import androidx.annotation.DrawableRes
import androidx.annotation.IdRes
import androidx.annotation.LayoutRes
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.AppCompatImageView
import androidx.constraintlayout.widget.ConstraintLayout
import com.au.module_android.R
import com.au.module_android.click.onClick
import com.au.module_android.utils.asOrNull
import com.au.module_android.widget.CustomFontText

abstract class AbsFakeToolbar @JvmOverloads constructor(context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0) :
    ConstraintLayout(context, attrs, defStyleAttr) {

    @LayoutRes
    protected abstract fun layoutRId(): Int
    @IdRes
    protected abstract fun titleRId(): Int
    @IdRes
    protected abstract fun backRId(): Int
    @DrawableRes
    protected abstract fun defaultBackImage() : Int

    val title: CustomFontText
    val back: AppCompatImageView

    init {
        LayoutInflater.from(context).inflate(layoutRId(), this)
        title = findViewById(titleRId())
        back = findViewById(backRId())

        val attr = context.obtainStyledAttributes(attrs, R.styleable.FakeToolbar)
        attr.getString(R.styleable.FakeToolbar_fakeToolbarTitle)?.let {
            title.text = it
        }

        val image = attr.getResourceId(R.styleable.FakeToolbar_fakeToolbarBackImage, defaultBackImage())
        if (image > 0) {
            back.setImageResource(image)
        }
        back.onClick {
            val activity = it.context.asOrNull<AppCompatActivity>()
            activity?.onBackPressedDispatcher?.onBackPressed()
        }

        attr.recycle()
    }

}
