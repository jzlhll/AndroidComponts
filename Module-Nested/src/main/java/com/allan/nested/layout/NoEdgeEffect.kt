package com.allan.nested.layout

import android.content.Context
import android.graphics.Canvas
import android.os.Build
import android.util.AttributeSet
import android.widget.EdgeEffect
import androidx.annotation.RequiresApi

/**
 * @date :2023/12/26 14:42
 * @description: 因为RecyclerView内部做了边缘(edge)效果处理，
 * 在1.2.1和1.3.2对于边缘处理不同，进而导致parentView无法监听到Nest事件。
 * 研究许久，如果想要下拉刷新的嵌套RecyclerView的Layout，则禁用内部的RecyclerView的edge效果即可。
 */
class NoEdgeEffect : EdgeEffect {
    constructor(context: Context?) : super(context)

    @RequiresApi(api = Build.VERSION_CODES.S)
    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs)

    override fun setSize(width: Int, height: Int) {
        //do nothing.
    }

    override fun isFinished(): Boolean {
        return true
    }

    override fun onPull(deltaDistance: Float) {
        //do nothing.
    }

    override fun onPull(deltaDistance: Float, displacement: Float) {
        //do nothing.
    }

    override fun onPullDistance(deltaDistance: Float, displacement: Float): Float {
        //do nothing.
        return 0f
    }

    override fun onRelease() {
        //do nothing.
    }

    override fun onAbsorb(velocity: Int) {
        //do nothing.
    }

    override fun draw(canvas: Canvas?): Boolean {
        //do nothing.
        return false
    }
}
