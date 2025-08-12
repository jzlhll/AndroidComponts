package com.au.audiorecordplayer.cam2.impl.states

import android.hardware.camera2.CameraCaptureSession
import android.hardware.camera2.CameraDevice
import android.hardware.camera2.CaptureRequest
import android.view.Surface
import com.au.audiorecordplayer.cam2.impl.AbstractStateBase
import com.au.audiorecordplayer.cam2.impl.IStateBaseCallback
import com.au.audiorecordplayer.cam2.impl.MyCamManager

/**
 * 一个特例，我只想用这个类来描述camera 没有open或者died的状态
 */
class StateDied(mgr: MyCamManager) : AbstractStateBase(mgr) {

    override fun allIncludePictureSurfaces(): List<Surface> {
        return emptyList()
    }

    override fun s1_createCaptureSessionStateCallback(cameraDevice: CameraDevice): CameraCaptureSession.StateCallback {
        throw IllegalStateException("StateDied Never call this1.")
    }

    override fun createCaptureBuilder(cameraDevice: CameraDevice): CaptureRequest.Builder {
        throw IllegalStateException("StateDied Never call this2.")
    }

    override fun createSession(cb: IStateBaseCallback?): Boolean {
        return true
    }
}
