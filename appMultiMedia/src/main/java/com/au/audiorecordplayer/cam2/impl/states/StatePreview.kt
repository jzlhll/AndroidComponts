package com.au.audiorecordplayer.cam2.impl.states

import android.hardware.camera2.CameraCaptureSession
import android.hardware.camera2.CameraDevice
import android.hardware.camera2.CaptureRequest
import android.view.Surface
import com.au.audiorecordplayer.cam2.impl.AbstractStateBase
import com.au.audiorecordplayer.cam2.impl.IStatePreviewCallback
import com.au.audiorecordplayer.cam2.impl.MyCamManager
import com.au.audiorecordplayer.util.MyLog

open class StatePreview(mgr: MyCamManager) : AbstractStateBase(mgr) {
    override fun allIncludePictureSurfaces(): List<Surface> {
        return listOf(cameraManager.surface!!)
    }

    override fun createCaptureBuilder(cameraDevice: CameraDevice): CaptureRequest.Builder {
        val captureRequestBuilder = cameraDevice.createCaptureRequest(CameraDevice.TEMPLATE_PREVIEW)
        captureRequestBuilder.addTarget(cameraManager.surface!!)
        captureRequestBuilder.set(
            CaptureRequest.CONTROL_AF_MODE,
            CaptureRequest.CONTROL_AF_MODE_CONTINUOUS_PICTURE
        )
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

