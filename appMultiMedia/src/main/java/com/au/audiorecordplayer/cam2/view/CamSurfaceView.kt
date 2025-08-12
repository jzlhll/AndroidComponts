package com.au.audiorecordplayer.cam2.view

import android.content.Context
import android.util.AttributeSet
import android.view.SurfaceHolder
import android.view.SurfaceView
import com.au.audiorecordplayer.util.MyLog

class CamSurfaceView : SurfaceView, SurfaceHolder.Callback {
    private var mCallback: IViewStatusChangeCallback? = null

    fun setCallback(mCallback: IViewStatusChangeCallback?) {
        this.mCallback = mCallback
    }

    constructor(context: Context?) : super(context) {
        init()
    }

    constructor(context: Context?, attrs: AttributeSet?) : super(context, attrs) {
        init()
    }

    constructor(context: Context?, attrs: AttributeSet?, defStyleAttr: Int) : super(context, attrs, defStyleAttr) {
        init()
    }

    private fun init() {
        holder.addCallback(this)
    }

    override fun surfaceCreated(holder: SurfaceHolder) {
        MyLog.d("SurfaceCreated")
        mCallback?.onSurfaceCreated(holder, null)
    }

    override fun surfaceChanged(holder: SurfaceHolder, format: Int, width: Int, height: Int) {
        MyLog.d("SurfaceChanged")
        mCallback?.onSurfaceChanged()
    }

    override fun surfaceDestroyed(holder: SurfaceHolder) {
        MyLog.d("surfaceDestroyed")
        mCallback?.onSurfaceDestroyed()
    }
}
