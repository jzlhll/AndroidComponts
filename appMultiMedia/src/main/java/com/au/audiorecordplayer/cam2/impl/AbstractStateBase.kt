package com.au.audiorecordplayer.cam2.impl

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

    /**
     * 对于在创建会话的时候，必须将所有用到的surface(包含take picture)都贴入createSession里面
     * 与addTargetSurfaces差异是：后者用于addTarget使用的时候，这是创建基础使用，不包含takePicture的ImageReader
     */
    protected var allIncludePictureSurfaces: MutableList<Surface>? = null
    protected var addTargetSurfaces: MutableList<Surface>? = null

    protected var camSession: CameraCaptureSession? = null

    init {
        MyLog.d("create state " + javaClass.getSimpleName())
        step0_createSurfaces()
    }

    /**
     * 在类初始化的时候被调用。你不应该调用它，只需要实现它。
     *
     *
     * 在camera open之后，session创建之前
     * 根据不同的state，组合不同的surface
     */
    protected abstract fun step0_createSurfaces()

    open fun closeSession() {
        MyLog.d("close session")
        if (camSession == null) {
            MyLog.d("$javaClass no camera cam session")
        }
        camSession?.close()
        camSession = null
        addTargetSurfaces?.clear()
        addTargetSurfaces = null

        allIncludePictureSurfaces?.clear()
        allIncludePictureSurfaces = null
        mStateBaseCb = null
    }

    /**
     * 不同的session下有不同的模式
     * 子类可以根据需要覆写该方法。
     */
    protected open fun step1_getTemplateType() : Int {
        return CameraDevice.TEMPLATE_PREVIEW
    }

    /**
     * 子类必须实现，而不应该调用
     * 创建一个监听完成session的回调信息，并将StateBaseCb外部监听处理
     */
    protected abstract fun createCameraCaptureSessionStateCallback(captureRequestBuilder: CaptureRequest.Builder): CameraCaptureSession.StateCallback

    /**
     * 该方法用于camera opened以后，创建preview、picture和record等的会话
     * 且session只有一个
     */
    open fun createSession(cb: IStateBaseCallback?): Boolean {
        mStateBaseCb = cb
        val cameraDevice = cameraManager.cameraDevice
        if (cameraDevice != null) {
            try {
                val captureRequestBuilder = cameraDevice.createCaptureRequest(step1_getTemplateType())
                for (surface in addTargetSurfaces!!) {
                    captureRequestBuilder.addTarget(surface)
                }
                //todo 可不能只做图片的surface即可
                cameraManager.cameraDevice!!.createCaptureSession(
                    allIncludePictureSurfaces!!,
                    createCameraCaptureSessionStateCallback(captureRequestBuilder), cameraManager
                )
            } catch (e: Exception) {
                MyLog.ex(e)
                return false
            }
        }
        return true
    }
}
