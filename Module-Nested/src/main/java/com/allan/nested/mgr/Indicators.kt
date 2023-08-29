package com.allan.nested.mgr

import android.view.View
import com.google.android.material.progressindicator.CircularProgressIndicator

internal abstract class IndicatorBase {
    val max = 100
    abstract var isIndeterminate:Boolean
    abstract var translationY:Float

    /**
     * 这个有实际的用途，用于判断是否能够加载。必须正确处理。
     */
    abstract var progress:Int

    open fun reset() {}
    open fun hide() {}
    open fun show() {}
}

internal class NoneIndicator : IndicatorBase() {
    override var isIndeterminate: Boolean = false
    override var translationY: Float
        get() = 0f
        set(value) {}
    /**
     * 不显示它；我们一直是0，就可以了。
     */
    override var progress: Int
        get() = 0
        set(value) {}
}

internal class RealIndicator(private val indicator: CircularProgressIndicator) : IndicatorBase() {
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