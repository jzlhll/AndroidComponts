package com.allan.nongyaofloat.floats

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.content.res.ColorStateList
import android.graphics.Color
import android.graphics.PixelFormat
import android.os.Build
import android.view.Gravity
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.WindowManager
import android.widget.FrameLayout
import androidx.appcompat.widget.AppCompatImageView
import com.allan.nongyaofloat.R

class FloatingClickView(private val mContext: Context) : FrameLayout(mContext) {
 
    private lateinit var mWindowManager: FloatingManager
    private var mParams: WindowManager.LayoutParams? = null
 
    private lateinit var mView: View
 
    //按下坐标
    private var mTouchStartX = -1f
    private var mTouchStartY = -1f
 
    val STATE_CLICKING = "state_clicking"
    val STATE_NORMAL = "state_normal"
    private var mCurrentState = STATE_NORMAL
 
    private var ivIcon: AppCompatImageView? = null

    private val colorAlphaTint = ColorStateList.valueOf(Color.parseColor("#8f212121"))
    private val colorNormalTint = ColorStateList.valueOf(Color.parseColor("#212121"))
 
    init {
        initView()
    }
 
    private fun initView() {
        mView = LayoutInflater.from(context).inflate(R.layout.view_floating_click, null)
        ivIcon = mView.findViewById(R.id.iv_icon)
        mWindowManager = FloatingManager.getInstance(mContext)
        initListener()
    }
 
    @SuppressLint("ClickableViewAccessibility")
    private fun initListener() {
        mView.setOnTouchListener { v, event ->
            when (event.action) {
                MotionEvent.ACTION_DOWN -> {
                    mTouchStartX = event.rawX
                    mTouchStartY = event.rawY
                }
 
                MotionEvent.ACTION_MOVE -> {
                    mParams?.let {
                        it.x += (event.rawX - mTouchStartX).toInt()
                        it.y += (event.rawY - mTouchStartY).toInt()
                        mWindowManager.updateView(mView, it)
                    }
                    mTouchStartX = event.rawX
                    mTouchStartY = event.rawY
                }
            }
            false
        }
 
        mView.setOnClickListener {
            val location = IntArray(2)
            it.getLocationOnScreen(location)

            val intent = Intent(FloatConst.BROADCAST_ACTION_AUTO_CLICK)
            if (mCurrentState == STATE_NORMAL) {
                mCurrentState = STATE_CLICKING
                intent.putExtra(FloatConst.KEY_ACTION, NongYaoAutoClickService.ACTION_PLAY)
                intent.putExtra(FloatConst.KEY_POINT_X, (location[0] - 1).toFloat())
                intent.putExtra(FloatConst.KEY_POINT_Y, (location[1] - 1).toFloat())
                ivIcon?.imageTintList = colorNormalTint
            } else {
                mCurrentState = STATE_NORMAL
                intent.putExtra(FloatConst.KEY_ACTION, NongYaoAutoClickService.ACTION_STOP)
                ivIcon?.imageTintList = colorAlphaTint
            }

            context.sendBroadcast(intent)
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
 
            width = LayoutParams.WRAP_CONTENT
            height = LayoutParams.WRAP_CONTENT
            if (mView.isAttachedToWindow) {
                mWindowManager.removeView(mView)
            }
            mWindowManager.addView(mView, this)
        }
    }
 
    fun remove() {
        mCurrentState = STATE_NORMAL
        mWindowManager.removeView(mView)
    }
 
}