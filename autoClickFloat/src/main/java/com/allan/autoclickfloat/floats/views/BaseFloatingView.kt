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

/**
 * @author allan
 * @date :2024/4/15 16:43
 * @description:
 */
open class BaseFloatingView(@LayoutRes private val layoutId:Int) {
    private var mParams: WindowManager.LayoutParams? = null
    val mRoot: View = LayoutInflater.from(Globals.app).inflate(layoutId, null).also { it.tag = this }

    var touchUpCallback:((pointX:Int, pointY:Int)->Unit)? = null
    var clickCallback:(()->Unit)? = null

    init {
        initListener()
    }

    private var dX:Int = 0
    private var dY:Int = 0

    @SuppressLint("ClickableViewAccessibility")
    private fun initListener() {
        mRoot.setOnTouchListener { v, event ->
            //获取相对屏幕的坐标，即以屏幕左上角为原点
            when (event.action) {
                MotionEvent.ACTION_DOWN -> {
                    //获取相对View的坐标，即以此View左上角为原点
                    dX = event.rawX.toInt()
                    dY = event.rawY.toInt()
                    Log.i("Floating", "startX $dX ====startY $dY")
                    mRoot.alpha = 1f
                }

                MotionEvent.ACTION_MOVE -> {
                    val nX = event.rawX.toInt()
                    val nY = event.rawY.toInt()
                    val cW = nX - dX
                    val cH = nY - dY
                    mParams?.let { mP->
                        mP.x += cW
                        mP.y += cH
                    }
                    Log.i("Floating", "moveX $cW ====moveY $cH")
                    updateViewPosition()
                }
                MotionEvent.ACTION_CANCEL,
                MotionEvent.ACTION_UP -> {
                    updateViewPosition()
                    touchUpCallback?.invoke(mParams?.x ?: 0, mParams?.y ?: 0)

                    mRoot.alpha = 0.3f
                }
            }
            false
        }

        mRoot.onClick {
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

    fun show() {
        mParams = WindowManager.LayoutParams()
        mParams?.apply {
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
        }
    }

    fun remove() {
        WindowMgr.removeView(mRoot)
    }
}