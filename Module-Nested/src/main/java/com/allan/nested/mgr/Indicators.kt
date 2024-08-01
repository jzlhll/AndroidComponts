package com.allan.nested.mgr

import android.view.View
import androidx.core.view.marginBottom
import com.au.module_android.utils.dp
import com.google.android.material.progressindicator.CircularProgressIndicator

internal abstract class IndicatorBase {
    /**
     * 可以额外设置，修正的偏差值。
     */
    var holdTranslateDeltaY = 0f

    val max = 100
    abstract var isIndeterminate:Boolean
    abstract var translationY:Float

    /**
     * 这是如果加载中，把进度条hold住，等待加载成功后消失的时候的高度
     */
    abstract val holdTranslateY:Float

    /**
     * 这个有实际的用途，用于判断是否能够加载。必须正确处理。
     */
    abstract var progress:Int

    open fun reset() {}
    open fun hide() {}
    open fun show() {}
}

internal class NoneIndicator : IndicatorBase() {
    private var mProgress = 0

    override val holdTranslateY: Float
        get() = 0f

    override var isIndeterminate: Boolean = false
    override var translationY: Float
        get() = 0f
        set(value) {}
    /**
     * 不显示它；我们一直是0，就可以了。
     */
    override var progress: Int
        get() = mProgress
        set(value) {mProgress = value}
}

internal class RealIndicator(private val indicator: CircularProgressIndicator) : IndicatorBase() {
    private val _holdTranslateY:Float = indicator.indicatorSize + indicator.trackThickness * 2 + indicator.marginBottom + 0.5f.dp

    override val holdTranslateY: Float
        get() = _holdTranslateY + holdTranslateDeltaY

    override var isIndeterminate: Boolean
        get() = indicator.isIndeterminate
        set(value) {
            indicator.isIndeterminate = value
        }

    override var translationY: Float
        get() = indicator.translationY
        set(value) {indicator.translationY = value}
    override var progress: Int
        get() = indicator.progress
        set(value) {
            indicator.progress = value
        }

    override fun reset() {
        indicator.visibility = View.GONE
        indicator.progress = 0
        indicator.isIndeterminate = false
    }

    override fun hide() {
        if (indicator.visibility != View.GONE) {
            indicator.visibility = View.GONE
        }
    }

    override fun show() {
        if (indicator.visibility != View.VISIBLE) {
            indicator.visibility = View.VISIBLE
        }
    }
}