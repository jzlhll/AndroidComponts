package com.allan.autoclickfloat.floats.views

import android.annotation.SuppressLint
import android.graphics.PixelFormat
import android.util.Log
import android.view.Gravity
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.WindowManager
import android.widget.FrameLayout
import androidx.annotation.LayoutRes
import com.au.module_android.Globals
import com.au.module_android.click.onClick
import kotlin.math.abs

/**
 * @author allan
 * @date :2024/4/15 16:43
 * @description: 不要给任何的子View设置点击事件。通过直接监听这里面的clickCallback来处理
 */
open class BaseFloatingView(@LayoutRes private val layoutId:Int) {
    private var mParams: WindowManager.LayoutParams? = null
    val mRoot: View = LayoutInflater.from(Globals.app).inflate(layoutId, null).also { it.tag = this }

    var touchUpCallback:((pointX:Int, pointY:Int)->Unit)? = null

    var clickCallback:(()->Unit)? = null

    private var isShown = false

    var mNotAlpha = 0.4f

    init {
        initListener()
    }

    private val MOVE_REACH_PIXEL = 4

    private var lastTouchAction = 0

    private var mRawX:Float = 0f
    private var mRawY:Float = 0f

    @SuppressLint("ClickableViewAccessibility")
    private fun initListener() {
        mRoot.setOnTouchListener { v, event ->
            //获取相对屏幕的坐标，即以屏幕左上角为原点
            Log.d("allan", "onTouch: ${event.action} x(${event.x}) y(${event.y}), rawX(${event.rawX} rawY(${event.rawY}))")
            when (event.action) {
                MotionEvent.ACTION_DOWN -> {
                    //获取相对View的坐标，即以此View左上角为原点
                    lastTouchAction = MotionEvent.ACTION_DOWN //设置状态为按下
                    //判断是不第一次按下,不然的话每重新滑动都会回到起点
                    //0这个值应该跟随你的初始位置变化
                    mRawX = event.rawX
                    mRawY = event.rawY
                    mRoot.alpha = 1f
                }

                MotionEvent.ACTION_MOVE -> {
                    //当X轴或Y轴的滑动大于10时再,判定为滑动,正好点击事件也需要这个
                    if (abs(event.rawX - mRawX) > MOVE_REACH_PIXEL
                        || abs(event.rawY - mRawY) > MOVE_REACH_PIXEL) {
                        lastTouchAction = MotionEvent.ACTION_MOVE //设置状态为滑动
// getRawX是触摸位置相对于屏幕的坐标，getX是相对于按钮的坐标
                        val distanceX = (event.rawX - mRawX).toInt()
                        val distanceY = (event.rawY - mRawY).toInt()
                        mParams?.let {
                            it.x += distanceX
                            it.y += distanceY
                            // 刷新
                            updateViewPosition()
                        }
                        //记录下最新一个点的位置
                        mRawX = event.rawX
                        mRawY = event.rawY
                    }
                }
                MotionEvent.ACTION_CANCEL,
                MotionEvent.ACTION_UP -> {
                    touchUpCallback?.invoke(mParams?.x ?: 0, mParams?.y ?: 0)
                    mRoot.alpha = mNotAlpha
                }
            }
            false
        }

        mRoot.onClick {
            Log.d("allan", "on click")
            clickCallback?.invoke()
        }
    }

    private fun updateViewPosition() {
        //更新浮动窗口位置参数
        mParams?.apply {
            Log.d("allan", "WindowMgr updateView $x $y")
            WindowMgr.updateView(mRoot, this) //刷新显示
        }
    }

    fun show(x:Int?=null, y:Int? = null) {
        if (isShown) {
            return
        }

        mParams = WindowManager.LayoutParams()
        mParams?.apply {
            if (x != null) this.x = x
            if (y != null) this.y = y

            gravity = Gravity.CENTER
            //总是出现在应用程序窗口之上
            type =
                WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY
            //设置图片格式，效果为背景透明
            format = PixelFormat.RGBA_8888

            flags =
                WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL or
                        WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN or WindowManager.LayoutParams.FLAG_LAYOUT_INSET_DECOR or
                        WindowManager.LayoutParams.FLAG_WATCH_OUTSIDE_TOUCH

            width = FrameLayout.LayoutParams.WRAP_CONTENT
            height = FrameLayout.LayoutParams.WRAP_CONTENT
            if (mRoot.isAttachedToWindow) {
                WindowMgr.removeView(mRoot)
            }
            WindowMgr.addView(mRoot, this)
            isShown = true
        }
    }

    fun remove() {
        if (isShown) {
            WindowMgr.removeView(mRoot)
            isShown = false
        }
    }
}