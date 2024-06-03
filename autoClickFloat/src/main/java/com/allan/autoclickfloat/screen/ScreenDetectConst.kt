package com.allan.autoclickfloat.screen

import android.content.Context
import android.graphics.Bitmap
import android.graphics.PixelFormat
import android.graphics.Point
import android.hardware.display.DisplayManager
import android.media.Image
import android.media.ImageReader
import android.media.projection.MediaProjection
import android.util.Log
import com.au.module_android.utils.logd
import java.nio.ByteBuffer
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

class ScreenDetectConst {
    companion object {
        private var mScreenDetectObject:ScreenDetectConst? = null
        fun getInstance() : ScreenDetectConst {
            if (mScreenDetectObject == null) {
                synchronized(ScreenDetectConst::class.java) {
                    if (mScreenDetectObject == null) {
                        mScreenDetectObject = ScreenDetectConst()
                    }
                }
            }
            return mScreenDetectObject!!
        }
    }

    var projection : MediaProjection? = null

    private var imageReader:ImageReader? = null

    private var mPool:ExecutorService? = null
    private val mPoolLock = Any()

    private val frameTime = 1000 //多长时间处理一次

    private val pickColorArray = listOf(Point(100, 100), Point(100, 150), Point(100, 120))

    @Volatile
    private var lastParsedTime = 0L

    fun onDestroy() {
        imageReader?.close()
        mPool?.shutdownNow()
    }

    /**
     * 记得释放；外部不做释放Image。
     */
    private fun executeImageParseTask(image:Image?) {
        image ?: return
        val width = image.width
        val height = image.height
        //拿到所有的 Plane 数组
        val planes = image.planes
        val plane = planes[0]

        val buffer: ByteBuffer = plane.buffer
        //相邻像素样本之间的距离，因为RGBA，所以间距是4个字节
        val pixelStride = plane.pixelStride
        //每行的宽度
        val rowStride = plane.rowStride
        //因为内存对齐问题，每个buffer 宽度不同，所以通过pixelStride * width 得到大概的宽度，
        //然后通过 rowStride 去减，得到大概的内存偏移量，不过一般都是对齐的。
        val rowPadding = rowStride - pixelStride * width
        // 创建具体的bitmap大小，由于rowPadding是RGBA 4个通道的，所以也要除以pixelStride，得到实际的宽
        val bitmap = Bitmap.createBitmap(
            width + rowPadding / pixelStride,
            height, Bitmap.Config.ARGB_8888
        )
        bitmap.copyPixelsFromBuffer(buffer)
        image.close()

        for (point in pickColorArray) {
            logd { "pick Color $point #" + Integer.toHexString(bitmap.getPixel(point.x, point.y)) }
        }

        lastParsedTime = System.currentTimeMillis()
    }

    private fun imageParse() {
        val imageReader = imageReader?:return
        try {
            //获取捕获的照片数据
            executeImageParseTask(imageReader.acquireLatestImage())
        } catch (e: java.lang.Exception) {
            e.printStackTrace()
        } finally {
        }
    }

    fun configImageReader(context: Context) {
        val resources = context.resources?:return
        val projection = projection ?: return

        synchronized(mPoolLock) {
            val pool = mPool
            if (pool == null || pool.isShutdown || pool.isTerminated) {
                mPool = Executors.newSingleThreadExecutor()
            }
        }

        if (imageReader != null) {
            return
        }

        val dm = resources.displayMetrics


        imageReader = ImageReader.newInstance(
            dm.widthPixels, dm.heightPixels,
            PixelFormat.RGBA_8888, 2
        ).apply {
            setOnImageAvailableListener({
                Log.d("ScreenDetect", "a new frame coming...")
                if (lastParsedTime + frameTime < System.currentTimeMillis()) {
                    imageParse()
                }
            }, null)

            //把内容投射到ImageReader 的surface
            projection.createVirtualDisplay(
                "ScreenDetect", dm.widthPixels, dm.heightPixels, dm.densityDpi,
                DisplayManager.VIRTUAL_DISPLAY_FLAG_AUTO_MIRROR, surface, null, null
            )
        }
    }
}