package com.allan.androidlearning.activities

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.os.Bundle
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.allan.classnameanno.EntryFrgName
import com.au.module_android.ui.views.ViewFragment
import kotlin.math.abs
import kotlin.math.max

@EntryFrgName
class CanvasFragment : ViewFragment() {
    private lateinit var drawLinesView: DrawLinesView
    companion object {
        val pointsY = listOf(
            10f, 11f, 12f, 13f, 14f, 15.1f, 15.2f, 15.0f, 15.0f, 15.1f, 15.2f, 15.0f, 15.0f,15.1f, 15.2f, 15.0f, 15.0f, 15.0f, 15.1f, 15.2f, 15.0f, 15.0f, 15.0f, 15.1f, 15.2f, 15.0f, 15.0f, 15.0f, 15.1f, 15.2f, 15.0f, 15.0f,
                    16f, 17f, 17f, 18f, 20f, 30f, 35f, 40f,
            10f, 11f, 12f, 13f, 14f, 15.1f, 15.2f, 15.0f, 15.0f, 15.1f, 15.2f, 15.0f, 15.0f,15.1f, 15.2f, 15.0f, 15.0f, 15.1f, 15.2f, 15.0f, 15.0f, 15.0f, 15.1f, 15.2f, 15.0f, 15.0f, 15.0f, 15.1f, 15.2f, 15.0f, 15.0f,
                    17f, 17f, 18f, 20f, 30f, 35f,35f,  40f,
            10f, 11f, 12f, 13f, 14f, 15.1f, 15.2f, 15.0f, 15.0f,15.1f, 15.2f, 15.0f, 15.0f, 15.1f, 15.2f, 15.0f, 15.0f,15.1f, 15.2f, 15.0f, 15.0f, 15.0f, 15.1f, 15.2f, 15.0f, 15.0f, 15.0f, 15.1f, 15.2f, 15.0f, 15.0f,
                17f, 17f, 18f, 20f, 30f, 35f,35f, 40f,
        )

        //中值滤波算法 #start
        val sideCount = 2 //加权左侧数量
        val filterY = 0.4f //滤波最大偏离值
        val filterXRatio = 1f //左右幅度
        //中值滤波算法 #end
        fun alg() : List<Float> {
            if(false) return pointsY

            val sz = pointsY.size
            val list = ArrayList<Float>(sz)
            //前Count直接添加
            if (pointsY.size > sideCount) {
                for (i in 0 until sideCount) {
                    list.add(pointsY[i])
                }
            }

            var totalY:Float
            var avY:Float

            if (sz > sideCount * 2) { //中间的点进行滤波算法
                for (i in sideCount until pointsY.size - sideCount) {
                    totalY = 0f
                    for (j in -sideCount .. sideCount) {
                        totalY += pointsY[i + j]
                    }
                    avY = totalY / sideCount
                    if (abs(avY - pointsY[i]) <= filterY) { //目前采用替代均值。也可以采用众数等滤波方式
                        list.add(avY)
                    } else {
                        list.add(pointsY[i])
                    }
                }
            }

            //后Count直接添加
            if (pointsY.size > sideCount) { //举例 数组是4个，left3个，那么从第0~2是left。3是right
                                            //数组sz是6个，left3个，那么从3~5就是right
                                            //数组sz是7个，left3个，那么从4~7就是right
                val endStart = max(sz - sideCount, sideCount)
                for (i in endStart until sz) {
                    list.add(pointsY[i])
                }
            }
            return list
        }
    }

    override fun onUiCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        return DrawLinesView(requireContext()).also {
            drawLinesView = it
            it.setBackgroundColor(Color.parseColor("#f0f0f0"))
        }
    }

    override fun onResume() {
        super.onResume()
        drawLinesView.postDelayed({
            drawLinesView.myDrawStart()
        }, 10)
    }
}

class DrawLinesView @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {
    private val mPaint: Paint = Paint()
    private val mBoardPaint: Paint = Paint()
    init {
        mPaint.setColor(Color.BLUE)
        mPaint.isAntiAlias = true
        mPaint.strokeWidth = 2f

        mBoardPaint.setColor(Color.DKGRAY)
        mBoardPaint.isAntiAlias = true
        mBoardPaint.strokeWidth = 4f
    }

    private var startX: Float = 0f
    private var endX: Float = 0f
    private var startY: Float = 0f
    private var endY: Float = 0f

    private fun drawSkeleton(canvas: Canvas) {
        canvas.drawLine(startX, startY, endX, startY, mBoardPaint)
        canvas.drawLine(startX, startY, startX, endY, mBoardPaint)
    }

    private fun initOnDraw() {
        val height = height
        val width = width
        startX = width.toFloat() / 50
        startY = height.toFloat() / 80

        endX = width.toFloat() * 48 / 50
        endY = height.toFloat() * 78 / 80
    }

    fun myDrawStart() {
        initOnDraw()
        invalidate()
    }

    fun drawLines(canvas: Canvas) {
        val yRatio = 12f
        val xRatio = 7f

        var x = startX

        val cvtYs = CanvasFragment.alg()

        val size = cvtYs.size

        var i = 0
        while(i < size - 1) {
            val curY = cvtYs[i] * yRatio
            val nextY = cvtYs[i + 1] * yRatio
            canvas.drawLine(
                x, curY + startY,
                x + xRatio, nextY + startY,
                mPaint)
            x += xRatio
            i++
        }
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        drawSkeleton(canvas)
        drawLines(canvas)
    }
}