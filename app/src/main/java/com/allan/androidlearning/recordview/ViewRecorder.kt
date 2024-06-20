package com.allan.androidlearning.recordview

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Matrix
import android.graphics.PorterDuff
import android.os.Build
import android.os.Looper
import android.util.Size
import android.view.View
import androidx.annotation.RequiresApi

/**
 * Used to record a video with view that can be captured. It also supports to switch views during recording.
 * This class extends [SurfaceMediaRecorder] and provides an extra API [.setRecordedView].
 *
 *
 * By default, capture is drawn in the center of canvas in scale if necessary.
 * It is easy to change drawing behavior with [.setVideoFrameDrawer].
 *
 *
 * Main thread is set for drawing as capture is only available in this thread,
 * it's OK to move composing to a background thread with [.setWorkerLooper],
 * in this case, a capture buffer for multi-thread may be required.
 *
 * Created by z4hyoung on 2017/11/8.
 */
class ViewRecorder : SurfaceMediaRecorder {
    constructor() : super()
    @RequiresApi(Build.VERSION_CODES.S)
    constructor(context: Context) : super(context)

    private var mRecordedView: View? = null

    private var mVideoSize: Size? = null

    private val mVideoFrameDrawer: VideoFrameDrawer = object : VideoFrameDrawer {
        private fun getMatrix(bw: Int, bh: Int, vw: Int, vh: Int): Matrix {
            val matrix = Matrix()
            val scale: Float
            var scaleX = 1f
            var scaleY = 1f

            if (bw > vw) {
                scaleX = (vw.toFloat()) / bw
            }
            if (bh > vh) {
                scaleY = (vh.toFloat()) / bh
            }
            scale = (if (scaleX < scaleY) scaleX else scaleY)
            val transX = (vw - bw * scale) / 2
            val transY = (vh - bh * scale) / 2

            matrix.postScale(scale, scale)
            matrix.postTranslate(transX, transY)

            return matrix
        }

        override fun onDraw(canvas: Canvas) {
            val rv = mRecordedView ?: return

            rv.isDrawingCacheEnabled = true
            val bitmap = rv.drawingCache

            val bitmapWidth = bitmap.width
            val bitmapHeight = bitmap.height
            val videoWidth = mVideoSize!!.width
            val videoHeight = mVideoSize!!.height
            val matrix = getMatrix(bitmapWidth, bitmapHeight, videoWidth, videoHeight)
            canvas.drawColor(Color.BLACK, PorterDuff.Mode.CLEAR)
            canvas.drawBitmap(bitmap, matrix, null)

            rv.isDrawingCacheEnabled = false
        }
    }

    @Throws(IllegalStateException::class)
    override fun setVideoSize(width: Int, height: Int) {
        super.setVideoSize(width, height)
        mVideoSize = Size(width, height)
    }

    @Throws(IllegalStateException::class)
    override fun start() {
        if (isSurfaceAvailable()) {
            checkNotNull(mVideoSize) { "video size is not initialized yet" }
            checkNotNull(mRecordedView) { "recorded view is not initialized yet" }
            setWorkerLooper(Looper.getMainLooper())
            setVideoFrameDrawer(mVideoFrameDrawer)
        }

        super.start()
    }

    /**
     * Sets recorded view to be captured for video frame composition. Call this method before start().
     * You may change the recorded view with this method during recording.
     *
     * @param view the view to be captured
     */
    @Throws(IllegalStateException::class)
    fun setRecordedView(view: View) {
        mRecordedView = view
    }
}
