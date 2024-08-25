package com.allan.androidlearning.recordview

import android.content.Context
import android.graphics.Canvas
import android.media.MediaRecorder
import android.os.Build
import android.os.Handler
import android.os.Looper
import android.os.SystemClock
import android.view.Surface
import androidx.annotation.RequiresApi
import java.util.concurrent.atomic.AtomicBoolean

/**
 * 这个类继承了{@link MediaRecorder}，并管理每个录制视频帧的组成。
 * 在{@link #start()}之前的两个额外初始化步骤,
 * <pre>
 * {@link #setWorkerLooper(Looper)}
 * {@link #setVideoFrameDrawer(VideoFrameDrawer)}
 * </pre>
 *
 * 你也可以像使用{@link MediaRecorder}的其他功能一样使用它。
 *
 * <p> 顺便说一句，定义了一个更多的错误类型 {@link #MEDIA_RECORDER_ERROR_SURFACE} 用于Surface错误。
 */

open class SurfaceMediaRecorder : MediaRecorder {
    constructor():super()

    @RequiresApi(Build.VERSION_CODES.S)
    constructor(context: Context) : super(context)
    /**
     * 录制过程中表面错误，在这种情况下，应用程序必须释放
     * MediaRecorder 对象并实例化一个新的。
     *
     * @see android.media.MediaRecorder.OnErrorListener
     */
    companion object {
        const val MEDIA_RECORDER_ERROR_SURFACE = 10000

        /**
         * 在获取用于绘制此{@link Surface}时的表面错误。
         *
         * @see android.media.MediaRecorder.OnErrorListener
         */
        const val MEDIA_RECORDER_ERROR_CODE_LOCK_CANVAS = 1

        /**
         * 在释放并发布内容到{@link Surface}时的表面错误。
         *
         * @see android.media.MediaRecorder.OnErrorListener
         */
        const val MEDIA_RECORDER_ERROR_CODE_UNLOCK_CANVAS = 2

        /**
         * 默认帧间隔
         */
        private const val DEFAULT_INTERFRAME_GAP: Long = 1000
    }

    interface VideoFrameDrawer {
        /**
         * 视频帧组合时调用
         *
         * @param canvas 要在其上绘制内容的画布
         */
        fun onDraw(canvas: Canvas)
    }

    private var mVideoSource: Int = 0
    var mOnErrorListener: OnErrorListener? = null
    private var mInterframeGap = DEFAULT_INTERFRAME_GAP // 默认为1000毫秒
    private var mSurface: Surface? = null
    // 如果设置，这个类的工作方式与 MediaRecorder 相同
    private var mInputSurface: Surface? = null
    private var mWorkerHandler: Handler? = null
    private var mVideoFrameDrawer: VideoFrameDrawer? = null
    // 表示表面组合是否已开始
    private val mStarted = AtomicBoolean(false)
    // 表示表面组合是否暂停
    private val mPaused = AtomicBoolean(false)

    private val mWorkerRunnable = object : Runnable {
        private fun handlerCanvasError(errorCode: Int) {
            try {
                stop()
            } catch (e: Exception) {
                e.printStackTrace()
            }
            mOnErrorListener?.onError(this@SurfaceMediaRecorder, MEDIA_RECORDER_ERROR_SURFACE, errorCode)
        }

        override fun run() {
            if (!isRecording()) {
                return
            }

            var errorCode: Int? = null
            val start = SystemClock.elapsedRealtime()
            do {
                val canvas: Canvas = try {
                    mSurface!!.lockCanvas(null)
                } catch (e: Exception) {
                    errorCode = MEDIA_RECORDER_ERROR_CODE_LOCK_CANVAS
                    e.printStackTrace()
                    break
                }
                mVideoFrameDrawer?.onDraw(canvas)
                try {
                    mSurface!!.unlockCanvasAndPost(canvas)
                } catch (e: Exception) {
                    errorCode = MEDIA_RECORDER_ERROR_CODE_UNLOCK_CANVAS
                    e.printStackTrace()
                    break
                }
            } while (false)

            if (!isRecording()) {
                return
            }

            if (errorCode != null) {
                handlerCanvasError(errorCode)
            } else {
                // 如果为负值，Handler:sendMessageDelayed中的延迟将重置为0
                mWorkerHandler?.postDelayed(this, start + mInterframeGap - SystemClock.elapsedRealtime())
            }
        }
    }

    override fun pause() {
        if (isSurfaceAvailable()) {
            mPaused.set(true)
            mWorkerHandler?.removeCallbacks(mWorkerRunnable)
        }
        super.pause()
    }

    override fun reset() {
        localReset()
        super.reset()
    }

    override fun resume() {
        super.resume()
        if (isSurfaceAvailable()) {
            mPaused.set(false)
            mWorkerHandler?.post(mWorkerRunnable)
        }
    }

    override fun setOnErrorListener(l: OnErrorListener?) {
        super.setOnErrorListener(l)
        mOnErrorListener = l
    }

    override fun setInputSurface(surface: Surface) {
        super.setInputSurface(surface)
        mInputSurface = surface
    }

    override fun setVideoFrameRate(rate: Int) {
        super.setVideoFrameRate(rate)
        mInterframeGap = (1000 / rate + if (1000 % rate == 0) 0 else 1).toLong()
    }

    override fun setVideoSource(videoSource: Int) {
        super.setVideoSource(videoSource)
        mVideoSource = videoSource
    }

    override fun start() {
        if (isSurfaceAvailable()) {
            if (mWorkerHandler == null) {
                throw IllegalStateException("worker looper is not initialized yet")
            }
            if (mVideoFrameDrawer == null) {
                throw IllegalStateException("video frame drawer is not initialized yet")
            }
        }

        super.start()
        if (isSurfaceAvailable()) {
            mSurface = surface
            mStarted.set(true)
            mWorkerHandler?.post(mWorkerRunnable)
        }
    }

    override fun stop() {
        localReset()
        super.stop()
    }

    /**
     * 设置用于组合视频帧的绘制器。
     * @param drawer 用于组合帧的绘制器 {@link Canvas}
     * @throws IllegalStateException 如果在{@link #start()}之后调用它
     */
    fun setVideoFrameDrawer(drawer: VideoFrameDrawer) {
        if (isRecording()) {
            throw IllegalStateException("setVideoFrameDrawer called in an invalid state: Recording")
        }
        mVideoFrameDrawer = drawer
    }

    /**
     * 设置执行组合任务的工作looper
     * @param looper 用于组合的looper
     * @throws IllegalStateException 如果在{@link #start()}之后调用它
     */
    fun setWorkerLooper(looper: Looper) {
        if (isRecording()) {
            throw IllegalStateException("setWorkerLooper called in an invalid state: Recording")
        }
        mWorkerHandler = Handler(looper)
    }

    /**
     * 返回表面是否可编辑
     * @return 如果表面可编辑则返回true
     */
    protected fun isSurfaceAvailable(): Boolean {
        return mVideoSource == VideoSource.SURFACE && mInputSurface == null
    }

    private fun isRecording(): Boolean {
        return mStarted.get() && !mPaused.get()
    }

    private fun localReset() {
        if (isSurfaceAvailable()) {
            mStarted.compareAndSet(true, false)
            mPaused.compareAndSet(true, false)
            mWorkerHandler?.removeCallbacks(mWorkerRunnable)
        }
        mInterframeGap = DEFAULT_INTERFRAME_GAP
        mInputSurface = null
        mOnErrorListener = null
        mVideoFrameDrawer = null
        mWorkerHandler = null
    }
}
