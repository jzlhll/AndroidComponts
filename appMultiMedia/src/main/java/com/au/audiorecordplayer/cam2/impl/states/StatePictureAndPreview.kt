package com.au.audiorecordplayer.cam2.impl.states

import android.content.Context
import android.graphics.ImageFormat
import android.hardware.camera2.CameraCaptureSession
import android.hardware.camera2.CameraDevice
import android.hardware.camera2.CameraManager
import android.hardware.camera2.CaptureRequest
import android.view.Surface
import com.au.audiorecordplayer.cam2.base.IActionTakePicture
import com.au.audiorecordplayer.cam2.bean.TakePictureCallbackWrap
import com.au.audiorecordplayer.cam2.impl.IStatePreviewCallback
import com.au.audiorecordplayer.cam2.impl.MyCamManager
import com.au.audiorecordplayer.cam2.impl.PreviewSizeUtil
import com.au.audiorecordplayer.cam2.impl.picture.TakePictureWorker
import com.au.audiorecordplayer.util.MyLog
import com.au.module_android.Globals

open class StatePictureAndPreview(mgr: MyCamManager) : StatePreview(mgr), IActionTakePicture {
    var mTakePic: TakePictureWorker? = null

    fun getCameraSession() = camSession

    init {
        val systemCameraManager = Globals.app.getSystemService(Context.CAMERA_SERVICE) as CameraManager
        //由于super中有添加了preview的surface。这里处理拍照即可
        val needSize = PreviewSizeUtil().needSize(
            "StatePictureAndPreview",
            ImageFormat.JPEG, systemCameraManager, "" + cameraManager.cameraId, 1920, 1080
        )
        MyLog.d("StatePictureAndPreview needSize " + needSize.width + " * " + needSize.height)

        mTakePic = TakePictureWorker(this, cameraManager, needSize.width, needSize.height)
    }

    override fun allIncludePictureSurfaces(): List<Surface> {
        return listOf(mTakePic!!.surface, cameraManager.surface!!)
    }

    override fun closeSession() {
        mTakePic?.release()
        super.closeSession()
    }

    override fun takePicture(bean: TakePictureCallbackWrap) {
        mTakePic?.takePicture(bean)
    }

    override fun createCaptureBuilder(cameraDevice: CameraDevice): CaptureRequest.Builder {
        val captureRequestBuilder = cameraDevice.createCaptureRequest(CameraDevice.TEMPLATE_PREVIEW)
        captureRequestBuilder.addTarget(cameraManager.surface!!)
        captureRequestBuilder.set(
            CaptureRequest.CONTROL_AF_MODE,
            CaptureRequest.CONTROL_AF_MODE_CONTINUOUS_PICTURE
        )
        //set(CaptureRequest.JPEG_THUMBNAIL_SIZE, new Size(1080, 1920));
        return captureRequestBuilder
    }

    override fun s1_createCaptureSessionStateCallback(cameraDevice: CameraDevice): CameraCaptureSession.StateCallback {
        return object : CameraCaptureSession.StateCallback() {
            override fun onConfigured(cameraCaptureSession: CameraCaptureSession) {
                camSession = cameraCaptureSession

                s2_camCaptureSessionSetRepeatingRequest(cameraDevice, cameraCaptureSession)

                if (mStateBaseCb != null) {
                    val cb = mStateBaseCb as IStatePreviewCallback
                    cb.onPreviewSucceeded()
                }
            }

            override fun onConfigureFailed(cameraCaptureSession: CameraCaptureSession) {
                MyLog.e("Error Configure Preview!")
                if (mStateBaseCb != null) {
                    val cb = mStateBaseCb as IStatePreviewCallback
                    cb.onPreviewFailed()
                }
            }
        }
    }
}
