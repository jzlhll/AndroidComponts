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
import android.view.View
import android.view.View.MeasureSpec
import android.webkit.WebView
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
            val v = mRecordedView ?: return
            //onDrawOrig(canvas, v)
            //onDrawNew1(canvas, v)
            onDrawNew2(canvas, v)
        }

        //无法拿到echarts
        fun onDrawOrig(canvas: Canvas, v:View) {
            v.isDrawingCacheEnabled = true
            val bitmap = v.drawingCache

            val bitmapWidth = bitmap.width
            val bitmapHeight = bitmap.height
            val videoWidth = mVideoSize!!.width
            val videoHeight = mVideoSize!!.height
            val matrix = getMatrix(bitmapWidth, bitmapHeight, videoWidth, videoHeight)
            canvas.drawColor(Color.BLACK, PorterDuff.Mode.CLEAR)
            canvas.drawBitmap(bitmap, matrix, null)

            v.isDrawingCacheEnabled = false
        }

        //无法拿到echarts
        fun onDrawNew1(canvas: Canvas, v:View) {
            val bitmap = loadBitmapFromView(v)

            val bitmapWidth = bitmap.width
            val bitmapHeight = bitmap.height
            val videoWidth = mVideoSize!!.width
            val videoHeight = mVideoSize!!.height
            val matrix = getMatrix(bitmapWidth, bitmapHeight, videoWidth, videoHeight)
            canvas.drawColor(Color.BLACK, PorterDuff.Mode.CLEAR)
            canvas.drawBitmap(bitmap, matrix, null)
            //不太需要调用 bitmap.recycle()
        }

        fun loadBitmapFromView(v: View): Bitmap {
            val screenshot = Bitmap.createBitmap(v.width, v.height, Bitmap.Config.RGB_565)
            val c = Canvas(screenshot)
            // c.translate(-v.scrollX.toFloat(), -v.scrollY.toFloat())
            v.draw(c)
            return screenshot
        }

        //无法拿到echarts
        fun onDrawNew2(canvas: Canvas, v:View) {
            val bitmap = loadBitmapFromWebView2(v as WebView, 1f) ?: return

            val bitmapWidth = bitmap.width
            val bitmapHeight = bitmap.height
            val videoWidth = mVideoSize!!.width
            val videoHeight = mVideoSize!!.height
            val matrix = getMatrix(bitmapWidth, bitmapHeight, videoWidth, videoHeight)
            canvas.drawColor(Color.BLACK, PorterDuff.Mode.CLEAR)
            canvas.drawBitmap(bitmap, matrix, null)
            //不太需要调用 bitmap.recycle()
        }

        /**
         * WebView 截图
         *
         * @param webView 要截图的WebView
         * @param scale11 （保留参数，但当前未使用）
         * @return 截图后的Bitmap
         */
        fun loadBitmapFromWebView2(webView: WebView, scale11: Float): Bitmap? {
            try {
                val scale = webView.scale //scale11
                val height = webView.height //(webView.contentHeight * scale + 0.5f).toInt()
                val bitmap = Bitmap.createBitmap(webView.width, height, Bitmap.Config.RGB_565)//ARGB_8888
                val canvas = Canvas(bitmap)
                webView.draw(canvas)
                return bitmap
            } catch (e: Exception) {
                e.printStackTrace()
            }
            return null
        }

        fun loadBitmapFromWebView3(webView: WebView): Bitmap? {
            try {
                val picture = webView.capturePicture()
                val bitmap = Bitmap.createBitmap(picture.width, picture.height, Bitmap.Config.RGB_565)
                val canvas = Canvas(bitmap)
                picture.draw(canvas)
                return bitmap
            } catch (e: Exception) {
                e.printStackTrace()
            }
            return null
        }

        fun screenshot2(webView: WebView): Bitmap {
            webView.measure(
                MeasureSpec.makeMeasureSpec(
                    MeasureSpec.UNSPECIFIED, MeasureSpec.UNSPECIFIED
                ),
                MeasureSpec.makeMeasureSpec(0, MeasureSpec.UNSPECIFIED)
            )
            webView.layout(0, 0, webView.measuredWidth, webView.measuredHeight)
            webView.isDrawingCacheEnabled = true
            webView.buildDrawingCache()
            val bitmap = Bitmap.createBitmap(
                webView.measuredWidth,
                webView.measuredHeight, Bitmap.Config.ARGB_8888
            )

            val canvas = Canvas(bitmap)
            val paint = Paint()
            val iHeight = bitmap.height
            canvas.drawBitmap(bitmap, 0f, iHeight.toFloat(), paint)
            webView.draw(canvas)
            return bitmap
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
