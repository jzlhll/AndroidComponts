package com.au.audiorecordplayer.cam2.impl.states;


import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraCaptureSession;
import android.hardware.camera2.CaptureRequest;
import android.hardware.camera2.params.SessionConfiguration;

import androidx.annotation.NonNull;

import com.au.audiorecordplayer.cam2.base.FeatureUtil;
import com.au.audiorecordplayer.cam2.base.IActionTakePicture;
import com.au.audiorecordplayer.cam2.bean.TakePictureCallbackWrap;
import com.au.audiorecordplayer.cam2.impl.MyCamManager;
import com.au.audiorecordplayer.cam2.impl.picture.TakePictureBuilder;
import com.au.audiorecordplayer.util.CamLog;

public class StatePictureAndPreview extends StatePreview implements IActionTakePicture {
    private TakePictureBuilder mTakePic;

    public StatePictureAndPreview(MyCamManager cd) {
        super(cd);
    }

    @Override
    protected void step0_createSurfaces() {
        super.step0_createSurfaces();
        //由于super中有添加了preview的surface。这里处理拍照即可
        mTakePic = new TakePictureBuilder(cameraManager, mNeedSize.getWidth(), mNeedSize.getHeight());
    }

    @Override
    public void closeSession() {
        mTakePic.release();
        super.closeSession();
    }

    @Override
    public void takePicture(@NonNull TakePictureCallbackWrap func) {
        mTakePic.takePicture(func);
    }

    @Override
    public boolean createSession(IStateBaseCallback cb) {
        var ret = super.createSession(cb);
        if (ret) {
            cameraManager.getCameraDevice().createCaptureSession(mTakePic.getSurface(),
                    createCameraCaptureSessionStateCallback(),
                    cameraManager);
        }
        return ret;
    }

    private CameraCaptureSession.StateCallback createCameraCaptureSessionStateCallback() {
        return new CameraCaptureSession.StateCallback() {
            @Override
            public void onConfigured(@NonNull CameraCaptureSession session) {
                cameraManager.setCamSession(session);
                cameraManager.getPreviewBuilder().set(CaptureRequest.CONTROL_AF_MODE,
                        CaptureRequest.CONTROL_AF_MODE_CONTINUOUS_PICTURE);
                //camera.previewBuilder.set(CaptureRequest.JPEG_THUMBNAIL_SIZE, new Size(1080, 1920));
                try {
                    cameraManager.getCamSession().setRepeatingRequest(cameraManager.getPreviewBuilder().build(),
                            null, cameraManager);
                } catch (CameraAccessException e) {
                    e.printStackTrace();
                }
                if (mStateBaseCb != null) {
                    IStatePreviewCallback cb = (IStatePreviewCallback) mStateBaseCb;
                    cb.onPreviewSucceeded();
                }
            }

            @Override
            public void onConfigureFailed(@NonNull CameraCaptureSession session) {
                CamLog.e("Error Configure Preview!");
                if (mStateBaseCb != null) {
                    IStatePreviewCallback cb = (IStatePreviewCallback) mStateBaseCb;
                    cb.onPreviewFailed();
                }
            }
        };
    }
}
