package com.au.audiorecordplayer.cam2.impl.states

import android.hardware.camera2.CameraAccessException
import android.hardware.camera2.CameraCaptureSession
import android.hardware.camera2.CaptureRequest
import com.au.audiorecordplayer.cam2.impl.AbstractStateBase
import com.au.audiorecordplayer.cam2.impl.IStatePreviewCallback
import com.au.audiorecordplayer.cam2.impl.MyCamManager
import com.au.audiorecordplayer.util.MyLog

open class StatePreview(mgr: MyCamManager) : AbstractStateBase(mgr) {
    override fun step0_createSurfaces() {
        val surface = cameraManager.surface!!

        addTargetSurfaces = mutableListOf(surface)
        allIncludePictureSurfaces = mutableListOf(surface)
        MyLog.d("StatePreview: addTargetSurfaces.size=" + addTargetSurfaces?.size)
    }

    override fun createCameraCaptureSessionStateCallback(captureRequestBuilder: CaptureRequest.Builder): CameraCaptureSession.StateCallback {
        return object : CameraCaptureSession.StateCallback() {
            override fun onConfigured(cameraCaptureSession: CameraCaptureSession) {
                camSession = cameraCaptureSession
                captureRequestBuilder.set(
                    CaptureRequest.CONTROL_AF_MODE,
                    CaptureRequest.CONTROL_AF_MODE_CONTINUOUS_PICTURE
                )
                //camera.previewBuilder.set(CaptureRequest.JPEG_THUMBNAIL_SIZE, new Size(1080, 1920));
                try {
                    cameraCaptureSession.setRepeatingRequest(
                        captureRequestBuilder.build(),
                        null, cameraManager
                    )
                } catch (e: CameraAccessException) {
                    MyLog.ex(e)
                }
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

