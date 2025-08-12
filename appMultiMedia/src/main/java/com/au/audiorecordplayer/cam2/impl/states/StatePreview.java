package com.au.audiorecordplayer.cam2.impl.states;

import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraCaptureSession;
import android.hardware.camera2.CaptureRequest;

import androidx.annotation.NonNull;

import com.au.audiorecordplayer.cam2.impl.AbstractStateBase;
import com.au.audiorecordplayer.cam2.impl.IStatePreviewCallback;
import com.au.audiorecordplayer.cam2.impl.MyCamManager;
import com.au.audiorecordplayer.util.MyLog;

import java.util.ArrayList;

public class StatePreview extends AbstractStateBase {

    public StatePreview(MyCamManager mgr) {
        super(mgr);
    }

    @Override
    protected void step0_createSurfaces() {
        addTargetSurfaces = new ArrayList<>();
        allIncludePictureSurfaces = new ArrayList<>();
        var surface = cameraManager.getRealViewSurface();
        addTargetSurfaces.add(surface);
        allIncludePictureSurfaces.add(surface);
        MyLog.d("StatePreview: addTargetSurfaces.size=" + addTargetSurfaces.size());
    }

    @Override
    protected CameraCaptureSession.StateCallback createCameraCaptureSessionStateCallback(@NonNull CaptureRequest.Builder captureRequestBuilder) {
        return new CameraCaptureSession.StateCallback() {
            @Override
            public void onConfigured(@NonNull CameraCaptureSession session) {
                cameraManager.setCamSession(session);
                var cameraCaptureSession = cameraManager.getCamSession();
                if (cameraCaptureSession != null) {
                    captureRequestBuilder.set(CaptureRequest.CONTROL_AF_MODE,
                            CaptureRequest.CONTROL_AF_MODE_CONTINUOUS_PICTURE);
                    //camera.previewBuilder.set(CaptureRequest.JPEG_THUMBNAIL_SIZE, new Size(1080, 1920));
                    try {
                        cameraCaptureSession.setRepeatingRequest(captureRequestBuilder.build(),
                                null, cameraManager);
                    } catch (CameraAccessException e) {
                        MyLog.ex(e);
                    }
                    if (mStateBaseCb != null) {
                        IStatePreviewCallback cb = (IStatePreviewCallback) mStateBaseCb;
                        cb.onPreviewSucceeded();
                    }
                }
            }

            @Override
            public void onConfigureFailed(@NonNull CameraCaptureSession session) {
                MyLog.e("Error Configure Preview!");
                if (mStateBaseCb != null) {
                    IStatePreviewCallback cb = (IStatePreviewCallback) mStateBaseCb;
                    cb.onPreviewFailed();
                }
            }
        };
    }
}

