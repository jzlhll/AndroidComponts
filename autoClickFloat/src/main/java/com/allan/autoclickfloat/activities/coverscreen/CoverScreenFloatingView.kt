package com.allan.autoclickfloat.activities.coverscreen

import android.annotation.SuppressLint
import android.graphics.Color
import android.graphics.PixelFormat
import android.util.Log
import android.view.Gravity
import android.view.LayoutInflater
import android.view.Surface
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.widget.FrameLayout
import com.allan.autoclickfloat.R
import com.allan.autoclickfloat.consts.Const
import com.allan.autoclickfloat.floats.WindowMgr
import com.au.module_android.Globals
import com.au.module_android.click.onClick
import com.au.module_android.utils.gone
import com.au.module_android.utils.unsafeLazy
import com.au.module_android.utils.visible

class CoverScreenFloatingView private constructor() {
    companion object {
        private const val MODE_COVER = 0
        private const val MODE_BLACK = 1

        @SuppressLint("StaticFieldLeak")
        private var instance:CoverScreenFloatingView? = null
        fun getInstance() : CoverScreenFloatingView {
            if (instance == null) {
                instance = CoverScreenFloatingView()
            }
            return instance!!
        }

        fun getInstanceOrNull() = instance
    }

    private var _mParams: WindowManager.LayoutParams? = null
    protected val mParams : WindowManager.LayoutParams
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

                    width = FrameLayout.LayoutParams.MATCH_PARENT
                    height = FrameLayout.LayoutParams.MATCH_PARENT
                }
            }
            return _mParams!!
        }

    val mRoot: View = LayoutInflater.from(Globals.app).inflate(R.layout.view_floating_over_screen, null).also {
        it.tag = this
    }

    var isShown = false
        private set

    private val normalFlags = WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL or
            WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN or WindowManager.LayoutParams.FLAG_LAYOUT_INSET_DECOR or
            WindowManager.LayoutParams.FLAG_WATCH_OUTSIDE_TOUCH

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

        WindowMgr.addView(mRoot, mParams)

        onSelfAttached()

        isShown = true
    }

    fun remove() {
        if (mRoot.isAttachedToWindow) {
            WindowMgr.removeView(mRoot)
            onSelfDetached()
        }
        isShown = false
    }

    private val coverColor = Color.TRANSPARENT
    private val blackColor = Color.BLACK

    private var mMode = MODE_COVER

    private var mClickCount = 0

    init {
        mRoot.onClick {
            when (mMode) {
                MODE_COVER -> {
                    if (++mClickCount == 2) {
                        mMode = MODE_BLACK
                        mClickCount = 0
                        mRoot.setBackgroundColor(blackColor)
                    } else {
                        ctrlHost.visible()
                        hideLock()
                    }
                }
                MODE_BLACK -> {
                    if (++mClickCount == 10) {
                        mClickCount = 0
                        mMode = MODE_COVER
                        mRoot.setBackgroundColor(coverColor)
                        ctrlHost.visible()
                        hideLock()
                    }
                }
            }
        }
    }

    private val ctrlHost: ViewGroup by unsafeLazy {
        val closeBtn = mRoot.findViewById<View>(R.id.closeBtn)
        closeBtn.onClick {
            getInstanceOrNull()?.remove()
        }

        val smallenBtn = mRoot.findViewById<View>(R.id.smallenBtn)
        smallenBtn.onClick {
            getInstanceOrNull()?.remove()
            SmallenIconFloatingView.getInstance().loadShow()
        }

        mRoot.findViewById<ViewGroup>(R.id.controlsHost)
    }

    private val hideLockImageRun by unsafeLazy {
        Runnable {
            ctrlHost.gone()
        }
    }

    private fun hideLock() {
        Globals.mainHandler.removeCallbacks(hideLockImageRun)
        Globals.mainHandler.postDelayed(hideLockImageRun, 3000)
    }

    fun onSelfAttached() {
        ctrlHost.visible()
        hideLock()
    }

    fun onSelfUpdated() {
        ctrlHost.visible()
        hideLock()
    }

    fun onSelfDetached() {
        Globals.mainHandler.removeCallbacks(hideLockImageRun)
    }
}