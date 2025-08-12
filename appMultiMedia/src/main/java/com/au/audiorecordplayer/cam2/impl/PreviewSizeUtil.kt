package com.au.audiorecordplayer.cam2.impl

import android.hardware.camera2.CameraCharacteristics
import android.hardware.camera2.params.StreamConfigurationMap
import android.util.Size
import com.au.audiorecordplayer.util.MyLog
import java.lang.Long
import kotlin.Array
import kotlin.Comparator
import kotlin.Int
import kotlin.RuntimeException

class PreviewSizeUtil {
    class CompareSizesByArea : Comparator<Size> {
        override fun compare(lhs: Size, rhs: Size): Int {
            return Long.signum(
                lhs.getWidth().toLong() * lhs.getHeight() -
                        rhs.getWidth().toLong() * rhs.getHeight()
            )
        }
    }

    fun needSize(from:String, fmt:Any, cameraManager: MyCamManager, wishWidth: Int, wishHeight: Int): Size {
        val camCharacteristics = cameraManager.systemCameraManager.getCameraCharacteristics("" + cameraManager.cameraId)
        var sizes: Array<Size>? = null
        var needSize: Size? = null
        val map: StreamConfigurationMap? = camCharacteristics.get(CameraCharacteristics.SCALER_STREAM_CONFIGURATION_MAP)
        if (map != null) {
            if (fmt is Int) {
                sizes = map.getOutputSizes(fmt)
            } else if(fmt is Class<*>) {
                sizes = map.getOutputSizes(fmt)
            }
        }
        if (sizes != null) {
            for (size in sizes) {
                if (needSize == null) {
                    needSize = size
                }
                MyLog.d("$from size: " + size.width + " " + size.height)
                if (size.height >= wishHeight && size.width >= wishWidth) {
                    needSize = size
                }
            }
        }

        if (needSize == null) {
            throw RuntimeException("No need Camera Size!")
        }

        return needSize
    }
}