package com.au.audiorecordplayer.cam2.view

import android.content.Context
import android.util.AttributeSet
import android.view.Surface
import android.view.SurfaceView
import android.view.TextureView
import android.view.View
import android.widget.FrameLayout


class Cam2PreviewView : FrameLayout {
    companion object {
        /**
         * 暂时采用静态变量来标记；可以改成attr。懒得做了。
         */
        var isSurfaceView = true
        const val TAG = "Cam2PreviewView"
    }

    private var mIsInit = false
    private var mCallback: IViewStatusChangeCallback? = null
    private var mRealView: View? = null
    val realView: View?
        get() = mRealView

    fun setCallback(mCallback: IViewStatusChangeCallback?) {
        this.mCallback = mCallback
    }

    constructor(context: Context) : super(context)
    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs)
    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(context, attrs, defStyleAttr)

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
        if (mIsInit) return
        if (isInEditMode) return

        val lp = LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT)
        if (isSurfaceView) {
            addView(CamSurfaceView(context).also {
                mRealView = it
                it.layoutParams = lp
                it.setCallback(mCallback)
            })
        } else {
            addView(CamTextureView(context).also {
                mRealView = it
                it.layoutParams = lp
                it.setCallback(mCallback)
            })
        }
        mIsInit = true
    }

    private var mSurface: Surface? = null
    private var ratioWidth = 0
    private var ratioHeight = 0

    val surface: Surface
        get() {
            val surface = mSurface
            if (surface != null) {
                return surface
            }

            val v = mRealView
            val newSurface = if (v is TextureView) {
                Surface(v.surfaceTexture)
            } else {
                (v as SurfaceView).holder.surface
            }
            mSurface = newSurface
            return newSurface
        }

    /**
     * Sets the desired aspect ratio.  The value is `width / height`.
     */
    fun setAspectRatio(width: Int, height: Int) {
        ratioWidth = width
        ratioHeight = height
        requestLayout()
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        if (ratioWidth > 0 && ratioHeight > 0) {
            val width = MeasureSpec.getSize(widthMeasureSpec)
            val height = (width * ratioHeight.toFloat() / ratioWidth).toInt()
            setMeasuredDimension(width, height)
        }
    }
}