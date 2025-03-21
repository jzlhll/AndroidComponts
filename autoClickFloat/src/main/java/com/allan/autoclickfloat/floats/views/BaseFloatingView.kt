package com.allan.autoclickfloat.floats.views

import android.annotation.SuppressLint
import android.graphics.PixelFormat
import android.util.Log
import android.view.Gravity
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.Surface
import android.view.View
import android.view.WindowManager
import android.widget.FrameLayout
import androidx.annotation.LayoutRes
import androidx.lifecycle.Observer
import com.allan.autoclickfloat.consts.Const
import com.allan.autoclickfloat.floats.WindowMgr
import com.au.module_android.Globals
import com.au.module_android.click.onClick
import kotlin.math.abs

/**
 * @author allan
 * @date :2024/4/15 16:43
 * @description: 不要给任何的子View设置点击事件。通过直接监听这里面的clickCallback来处理
 */
open class BaseFloatingView(@LayoutRes private val layoutId:Int) {
    val halfSize : Int
        get() = (mRoot.width shr 1)

    private var _mParams: WindowManager.LayoutParams? = null
    private val mParams : WindowManager.LayoutParams
        get() {
            if (_mParams == null) {
                _mParams = WindowManager.LayoutParams().apply {
                    gravity = Gravity.TOP or Gravity.START
                    //总是出现在应用程序窗口之上
                    type =
                        WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY
                    //设置图片格式，效果为背景透明
                    format = PixelFormat.RGBA_8888

                    flags = normalFlags

                    width = FrameLayout.LayoutParams.WRAP_CONTENT
                    height = FrameLayout.LayoutParams.WRAP_CONTENT
                }
            }
            return _mParams!!
        }

    val mRoot: View = LayoutInflater.from(Globals.app).inflate(layoutId, null).also {
        it.tag = this
    }

    var touchUpCallback:((pointX:Int, pointY:Int)->Unit)? = null

    var clickCallback:(()->Unit)? = null

    var isShown = false
        private set

    var mNotAlpha = 0.6f

    private val normalFlags = WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL or
            WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN or WindowManager.LayoutParams.FLAG_LAYOUT_INSET_DECOR or
            WindowManager.LayoutParams.FLAG_WATCH_OUTSIDE_TOUCH

    private val unTouchFlags = WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL or
            WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN or WindowManager.LayoutParams.FLAG_LAYOUT_INSET_DECOR or
            WindowManager.LayoutParams.FLAG_WATCH_OUTSIDE_TOUCH or WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE

    var disableTouch = false
        set(value) {
            if (value) {
                mParams.flags = unTouchFlags
            } else {
                mParams.flags = normalFlags
            }
            updateViewPosition()
            field = value
        }

    init {
        initListener()
    }

    private val MOVE_REACH_PIXEL = 4

    private var lastTouchAction = 0

    private var mRawX:Float = 0f
    private var mRawY:Float = 0f

    private var rotationObserver:Observer<Int?> ? = null

    @SuppressLint("ClickableViewAccessibility")
    private fun initListener() {
        mRoot.setOnTouchListener { v, event ->
            if (disableTouch) {
                false
            } else {
                //获取相对屏幕的坐标，即以屏幕左上角为原点
                //Log.d(Const.TAG, "onTouch: ${event.action} x(${event.x}) y(${event.y}), rawX(${event.rawX} rawY(${event.rawY}))")
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
                            mParams.x += distanceX
                            mParams.y += distanceY
                            updateViewPosition()
                            //记录下最新一个点的位置
                            mRawX = event.rawX
                            mRawY = event.rawY
                        }
                    }
                    MotionEvent.ACTION_CANCEL,
                    MotionEvent.ACTION_UP -> {
                        touchUpCallback?.invoke(mParams.x, mParams.y)
                        mRoot.alpha = mNotAlpha
                    }
                }
                false
            }
        }

        mRoot.onClick {
            if (!disableTouch) {
                clickCallback?.invoke()
            }
        }
    }

    fun updateViewPosition() {
        if (isShown) {
            //更新浮动窗口位置参数
            WindowMgr.updateView(mRoot, mParams) //刷新显示
            onSelfUpdated()
        }
    }

    var mLastOration = -1

    //传入的是保存后的结果。因此可能需要核对转换角度
    fun show(x:Int?, y:Int?, rotation:Int) {
        if (isShown) {
            return
        }
        val currentRotation = WindowMgr.mWindowManager.defaultDisplay.rotation
        var needFix = false
        if (currentRotation != rotation) {
            //转换不同角度的x,y
            when (rotation) {
                Surface.ROTATION_0 -> {
                    when (currentRotation) {
                        Surface.ROTATION_90 -> {
                            needFix = true
                        }
                        Surface.ROTATION_180 -> {

                        }
                        Surface.ROTATION_270 -> {
                            needFix = true
                        }
                    }
                }
                Surface.ROTATION_90 -> {
                    when (currentRotation) {
                        Surface.ROTATION_0 -> {
                            needFix = true
                        }
                        Surface.ROTATION_180 -> {
                            needFix = true
                        }
                        Surface.ROTATION_270 -> {
                        }
                    }
                }
                Surface.ROTATION_180 -> {
                    when (currentRotation) {
                        Surface.ROTATION_0 -> {

                        }
                        Surface.ROTATION_90 -> {
                            needFix = true
                        }
                        Surface.ROTATION_270 -> {
                            needFix = true
                        }
                    }
                }

                Surface.ROTATION_270 -> {
                    when (currentRotation) {
                        Surface.ROTATION_0 -> {
                            needFix = true
                        }
                        Surface.ROTATION_90 -> {
                        }
                        Surface.ROTATION_180 -> {
                            needFix = true
                        }
                    }
                }
            }
        }
        if (needFix) {
            Log.d(Const.TAG, ">>fixX-Y $x $y")
            if (y != null) mParams.x = y
            if (x != null) mParams.y = x
        } else {
            Log.d(Const.TAG, ">>no fix $x $y")
            if (x != null) mParams.x = x
            if (y != null) mParams.y = y
        }
        mLastOration = currentRotation

        if (mRoot.isAttachedToWindow) {
            WindowMgr.removeView(mRoot)
        }

        WindowMgr.addView(mRoot, mParams.also {
            addViewInitParam(mParams)
        })

        onSelfAttached()

        isShown = true

        //监听
        if (rotationObserver == null) {
            rotationObserver = Observer {
                val lastx = mParams.x
                val lasty = mParams.y
                val lastRot = mLastOration
                Log.d(Const.TAG, " rotationObserver! $it。>>lastRot $lastRot x,y $lastx $lasty")
                remove()
                show(lastx, lasty, lastRot)
            }
            Const.rotationLiveData.observeForeverUnStick(rotationObserver!!)
        }
    }

    fun remove() {
        if (mRoot.isAttachedToWindow) {
            WindowMgr.removeView(mRoot)
            onSelfDetached()
        }
        isShown = false
    }

    open fun onSelfAttached() {
    }

    open fun onSelfDetached() {
    }

    open fun onSelfUpdated() {
    }

    open fun addViewInitParam(mParams: WindowManager.LayoutParams) {
    }
}