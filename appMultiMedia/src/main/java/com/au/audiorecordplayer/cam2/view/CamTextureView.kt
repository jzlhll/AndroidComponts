package com.au.audiorecordplayer.cam2.view

import android.content.Context
import android.graphics.SurfaceTexture
import android.util.AttributeSet
import android.view.TextureView
import android.view.TextureView.SurfaceTextureListener
import com.au.audiorecordplayer.util.CamLog

class CamTextureView : TextureView, SurfaceTextureListener {
    private var mCallback: IViewStatusChangeCallback? = null

    fun setCallback(mCallback: IViewStatusChangeCallback?) {
        this.mCallback = mCallback
    }

    constructor(context: Context) : super(context) {
        init()
    }

    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs) {
        init()
    }

    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(context, attrs, defStyleAttr) {
        init()
    }

    private fun init() {
        surfaceTextureListener = this
    }

    override fun onSurfaceTextureAvailable(surface: SurfaceTexture, width: Int, height: Int) {
        CamLog.d("SurfaceCreated")
        mCallback?.onSurfaceCreated()
    }

    override fun onSurfaceTextureSizeChanged(surface: SurfaceTexture, width: Int, height: Int) {
        CamLog.d("SurfaceChanged")
        mCallback?.onSurfaceChanged()
    }

    override fun onSurfaceTextureDestroyed(surface: SurfaceTexture): Boolean {
        CamLog.d("surfaceDestroyed")
        mCallback?.onSurfaceDestroyed()
        return false
    }

    override fun onSurfaceTextureUpdated(surface: SurfaceTexture) {
    }
}
