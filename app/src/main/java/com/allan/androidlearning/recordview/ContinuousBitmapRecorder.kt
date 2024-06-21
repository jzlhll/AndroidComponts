package com.allan.androidlearning.recordview

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Matrix
import android.graphics.Paint
import android.graphics.PorterDuff
import android.os.Build
import android.os.Looper
import android.util.Size
import androidx.annotation.RequiresApi
import com.au.module_android.utils.logd

class ContinuousBitmapRecorder : SurfaceMediaRecorder {
    private var bitmapOffer:(()->Bitmap?)? = null

    constructor(bitmapOffer:()->Bitmap?) : super() {
        this.bitmapOffer = bitmapOffer
    }

    @RequiresApi(Build.VERSION_CODES.S)
    constructor(bitmapOffer:()->Bitmap?, context: Context) : super(context) {
        this.bitmapOffer = bitmapOffer
    }

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

        private var onceMatrix :Matrix? = null
        private fun getMatrix2(bw: Int, bh: Int, vw: Int, vh: Int): Matrix {
            val one = onceMatrix
            if (one != null) {
                return one
            }

            val matrix = Matrix()
            var scaleX = 1f
            var scaleY = 1f
            var transX = 0f
            var transY = 0f

            if(vw != bw) {
                scaleX = (vw.toFloat()) / bw
                transX = (vw - bw * scaleX) / 2
            }
            if(vh != bh) {
                scaleY = (vh.toFloat()) / bh
                transY = (vh - bh * scaleY) / 2
            }

            if(scaleX != 0f || scaleY != 0f) matrix.postScale(scaleX, scaleY)
            if(transX != 0f || transY != 0f) matrix.postTranslate(transX, transY)

            onceMatrix = matrix
            return matrix
        }

        override fun onDraw(canvas: Canvas) {
            onDrawOrig(canvas, bitmapOffer!!())
        }

        fun onDrawOrig(canvas: Canvas, bitmap: Bitmap?) {
            bitmap ?: return
            val bitmapWidth = bitmap.width
            val bitmapHeight = bitmap.height

            val videoWidth = mVideoSize!!.width
            val videoHeight = mVideoSize!!.height

            logd { "bitmap $bitmapWidth * $bitmapHeight; $videoWidth * $videoHeight" }
            val matrix = getMatrix2(bitmapWidth, bitmapHeight, videoWidth, videoHeight)
            canvas.drawColor(Color.WHITE, PorterDuff.Mode.CLEAR)
            canvas.drawBitmap(bitmap, matrix, null)
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
            setWorkerLooper(Looper.getMainLooper())
            setVideoFrameDrawer(mVideoFrameDrawer)
        }

        super.start()
    }
}
