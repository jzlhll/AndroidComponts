package com.au.audiorecordplayer.cam2.view

import android.content.Context
import android.util.AttributeSet
import android.util.Log
import android.view.Surface
import android.view.SurfaceView
import android.view.TextureView
import android.view.View
import android.widget.FrameLayout
import kotlin.math.abs

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

//    fun setPreviewSize(width: Int, height: Int) {
//        CamLog.d("Size: setPreviewSize previewSize $width*$height")
//        CamLog.d("Size: setPreviewSizeInit camView " + mRealView?.width + "*" + mRealView?.height)
//        mRealView?.post { //Camera2介绍的知识都比较少，介绍surfaceView+cam2就更少。
//            // http://book2s.com/java/src/package/android/hardware/camera2/cts/testcases/camera2surfaceviewtestcase.html#ee5c9b91de5483feb8b8f4ecb4f0691b
//            //找了很久，才从国外网站找到这个api，注意它的描述，可能需要换到主线程
//            //mViewSurface.getHolder().setFixedSize(width, height);
//            //一般地，推荐使用TextureView
//            //从上述来看，并不需要设置fixSize，只需要搞正确view的大小即可
//           //setAspectRatio(view.height * height / width, view.height)
//        }
//    }

    /**
     * Sets the desired aspect ratio.  The value is `width / height`.
     */
    fun setAspectRatio(aspectRatio: Double) {
        require(!(aspectRatio < 0))
        Log.d(TAG, "Setting aspect ratio to $aspectRatio (was $mTargetAspect)")
        if (mTargetAspect != aspectRatio) {
            mTargetAspect = aspectRatio
            requestLayout()
        }
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        var widthMeasureSpec = widthMeasureSpec
        var heightMeasureSpec = heightMeasureSpec
        Log.d(
            TAG, "onMeasure target=" + mTargetAspect +
                    " width=[" + MeasureSpec.toString(widthMeasureSpec) +
                    "] height=[" + MeasureSpec.toString(heightMeasureSpec) + "]"
        )

        // Target aspect ratio will be < 0 if it hasn't been set yet.  In that case,
        // we just use whatever we've been handed.
        if (mTargetAspect > 0) {
            var initialWidth = MeasureSpec.getSize(widthMeasureSpec)
            var initialHeight = MeasureSpec.getSize(heightMeasureSpec)

            // factor the padding out
            val horizPadding = getPaddingLeft() + getPaddingRight()
            val vertPadding = paddingTop + paddingBottom
            initialWidth -= horizPadding
            initialHeight -= vertPadding

            val viewAspectRatio = initialWidth.toDouble() / initialHeight
            val aspectDiff: Double = mTargetAspect / viewAspectRatio - 1

            if (abs(aspectDiff) < 0.01) {
                // We're very close already.  We don't want to risk switching from e.g. non-scaled
                // 1280x720 to scaled 1280x719 because of some floating-point round-off error,
                // so if we're really close just leave it alone.
                Log.d(
                    TAG, "aspect ratio is good (target=" + mTargetAspect +
                            ", view=" + initialWidth + "x" + initialHeight + ")"
                )
            } else {
                if (aspectDiff > 0) {
                    // limited by narrow width; restrict height
                    initialHeight = (initialWidth / mTargetAspect).toInt()
                } else {
                    // limited by short height; restrict width
                    initialWidth = (initialHeight * mTargetAspect).toInt()
                }
                Log.d(
                    TAG, "new size=" + initialWidth + "x" + initialHeight + " + padding " +
                            horizPadding + "x" + vertPadding
                )
                initialWidth += horizPadding
                initialHeight += vertPadding
                widthMeasureSpec = MeasureSpec.makeMeasureSpec(initialWidth, MeasureSpec.EXACTLY)
                heightMeasureSpec = MeasureSpec.makeMeasureSpec(initialHeight, MeasureSpec.EXACTLY)
            }
        }

        //Log.d(TAG, "set width=[" + MeasureSpec.toString(widthMeasureSpec) +
        //        "] height=[" + View.MeasureSpec.toString(heightMeasureSpec) + "]");
        super.onMeasure(widthMeasureSpec, heightMeasureSpec)
    }

    private var mTargetAspect = -1.0 // initially use default window size
}