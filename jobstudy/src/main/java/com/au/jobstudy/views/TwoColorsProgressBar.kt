package com.au.jobstudy.views

import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.util.AttributeSet
import android.util.Log
import android.widget.ProgressBar
import android.widget.TextView

class TwoColorsProgressBar @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null
) : ProgressBar(context, attrs) {
    private var firstColor:Int = -1
    private var secondColor:Int = -1

    private var secondStartProgress = -1

    private val paint = Paint().also { it.style = Paint.Style.FILL}

    private var curTimeTextView:TextView? = null
    private var textCallback:((cur:Int, max:Int)->Unit)? = null

    fun attachCurrentTimeTextView(tv:TextView, textCallback:(cur:Int, max:Int)->Unit) {
        curTimeTextView = tv
        this.textCallback = textCallback
    }

    fun initProgress(max:Int, firstComplete:Int) {
        setMax(max)
        secondStartProgress = if (firstComplete < max) {
            firstComplete + 1
        } else {
            -1
        }
    }

    fun initColors(firstColor:Int, secondColor:Int) {
        this.firstColor = firstColor
        this.secondColor = secondColor
    }

    override fun onLayout(changed: Boolean, left: Int, top: Int, right: Int, bottom: Int) {
        super.onLayout(changed, left, top, right, bottom)
        Log.d("TwoColors", "onLayout: $left, $right")
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        val width = width.toFloat()
        val height = height.toFloat()
        val progress = progress
        val max = max

        val onlyDrawFirst = if (secondStartProgress == -1) {
            //没有第二段。则当做第一段。
            true
        } else {
            //如果已经超过了第二段，则开始绘制第二次，第一段直接画满
            progress < secondStartProgress
        }

        val curProgressWidth = progress.toFloat() / max * width

        if (onlyDrawFirst) {
            paint.color = firstColor
            canvas.drawRect(0f,0f, curProgressWidth, height, paint)
        } else {
            val secondWidth = (max - secondStartProgress + 1).toFloat() / max * width
            val firstMaxWidth = width - secondWidth
            paint.color = firstColor
            canvas.drawRect(0f,0f, firstMaxWidth, height, paint)

            paint.color = secondColor
            canvas.drawRect(firstMaxWidth, 0f, width*(progress - secondStartProgress + 1) / max, height, paint)
        }

        curTimeTextView?.let {
            val attachTvWidth = it.width
            val maxTransX = width - attachTvWidth
            if (maxTransX < curProgressWidth) {
                it.translationX = curProgressWidth
            } else {
                it.translationX = maxTransX
            }

            textCallback?.invoke(progress, max)
        }
    }
}