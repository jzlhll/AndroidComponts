package com.au.audiorecordplayer.cam2.impl;

import android.hardware.camera2.CameraCaptureSession;
import android.hardware.camera2.CameraDevice;
import android.hardware.camera2.CaptureRequest;
import android.view.Surface;

import androidx.annotation.NonNull;

import com.au.audiorecordplayer.util.MyLog;

import java.util.List;

/**
 * 抽象类，用于描述，不同的camera session状态
 * 这个类的子类都将是处于camera open之后的状态（StateDied除外）
 */
public abstract class AbstractStateBase {

    protected MyCamManager cameraManager;

    protected IStateBaseCallback mStateBaseCb;

    /**
     * 对于在创建会话的时候，必须将所有用到的surface(包含take picture)都贴入createSession里面
     * 与addTargetSurfaces差异是：后者用于addTarget使用的时候，这是创建基础使用，不包含takePicture的ImageReader
     */
    protected List<Surface> allIncludePictureSurfaces;
    protected List<Surface> addTargetSurfaces;

    protected AbstractStateBase(MyCamManager mgr) {
        cameraManager = mgr;
        MyLog.d("create state " + getClass().getSimpleName());
        step0_createSurfaces();
    }

    /**
     * 在类初始化的时候被调用。你不应该调用它，只需要实现它。
     * <p>
     * 在camera open之后，session创建之前
     * 根据不同的state，组合不同的surface
     */
    protected abstract void step0_createSurfaces();

    public void closeSession() {
        if (cameraManager != null) {
            var camSession = cameraManager.getCamSession();
            if (camSession != null) {
                MyLog.d("close session");
                camSession.close();
                cameraManager.setCamSession(null);
            } else {
                MyLog.d("no camera cam session");
            }
        }
        if (addTargetSurfaces != null) {
            addTargetSurfaces.clear();
        }
        addTargetSurfaces = null;

        if (allIncludePictureSurfaces != null) {
            allIncludePictureSurfaces.clear();
        }
        allIncludePictureSurfaces = null;
    }

    /**
     * 不同的session下有不同的模式
     * 子类可以根据需要覆写该方法。
     */
    protected int step1_getTemplateType() {
        return CameraDevice.TEMPLATE_PREVIEW;
    }

    /**
     * 子类必须实现，而不应该调用
     * 创建一个监听完成session的回调信息，并将StateBaseCb外部监听处理
     */
    protected CameraCaptureSession.StateCallback createCameraCaptureSessionStateCallback(@NonNull CaptureRequest.Builder captureRequestBuilder) {
        return null;
    }

    /**
     * 该方法用于camera opened以后，创建preview、picture和record等的会话
     * 且session只有一个
     */
    public boolean createSession(IStateBaseCallback cb) {
        mStateBaseCb = cb;
        var cameraDevice = cameraManager.getCameraDevice();
        if (cameraDevice != null) {
            try {
                CaptureRequest.Builder captureRequestBuilder = cameraDevice.createCaptureRequest(step1_getTemplateType());
                for (Surface surface : addTargetSurfaces) {
                    captureRequestBuilder.addTarget(surface);
                }
                //todo 可不能只做图片的surface即可
                cameraManager.getCameraDevice().createCaptureSession(allIncludePictureSurfaces,
                        createCameraCaptureSessionStateCallback(captureRequestBuilder), cameraManager);
            } catch (Exception e) {
                MyLog.ex(e);
                return false;
            }
        }
        return true;
    }
}
