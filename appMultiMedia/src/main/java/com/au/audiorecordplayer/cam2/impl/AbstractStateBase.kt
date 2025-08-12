package com.au.audiorecordplayer.cam2.impl

import android.hardware.camera2.CameraAccessException
import android.hardware.camera2.CameraCaptureSession
import android.hardware.camera2.CameraDevice
import android.hardware.camera2.CaptureRequest
import android.view.Surface
import com.au.audiorecordplayer.util.MyLog

/**
 * 抽象类，用于描述，不同的camera session状态
 * 这个类的子类都将是处于camera open之后的状态（StateDied除外）
 */
abstract class AbstractStateBase protected constructor(protected var cameraManager: MyCamManager) {
    protected var mStateBaseCb: IStateBaseCallback? = null

    protected var camSession: CameraCaptureSession? = null

    open fun closeSession() {
        MyLog.d("close session")
        if (camSession == null) {
            MyLog.d("$javaClass no camera cam session")
        }
        camSession?.close()
        camSession = null
        mStateBaseCb = null
    }

    /**
     * 子类必须实现，而不应该调用
     * 创建一个监听完成session的回调信息，并将StateBaseCb外部监听处理
     */
    protected abstract fun s1_createCaptureSessionStateCallback(cameraDevice: CameraDevice): CameraCaptureSession.StateCallback

    protected fun s2_camCaptureSessionSetRepeatingRequest(cameraDevice:CameraDevice, cameraCaptureSession: CameraCaptureSession) {
        try {
            cameraCaptureSession.setRepeatingRequest(
                createCaptureBuilder(cameraDevice).build(),
                null, cameraManager
            )
        } catch (e: CameraAccessException) {
            MyLog.ex(e)
        }
    }

    /**
     * 在createCameraCaptureSessionStateCallback的回调onConfigured中调用实现
     */
    protected abstract fun createCaptureBuilder(cameraDevice: CameraDevice): CaptureRequest.Builder

    abstract fun allIncludePictureSurfaces() : List<Surface>

    /**
     * 该方法用于camera opened以后，创建preview、picture和record等的会话
     * 且session只有一个
     */
    open fun createSession(cb: IStateBaseCallback?): Boolean {
        mStateBaseCb = cb
        val cameraDevice = cameraManager.cameraDevice
        if (cameraDevice != null) {
            try {
                //todo 可不能只做图片的surface即可
                cameraManager.cameraDevice!!.createCaptureSession(
                    allIncludePictureSurfaces(),
                    s1_createCaptureSessionStateCallback(cameraDevice), cameraManager
                )
            } catch (e: Exception) {
                MyLog.ex(e)
                return false
            }
        }
        return true
    }
}
