package com.au.audiorecordplayer.cam2.impl.states;


import android.graphics.ImageFormat;
import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraCaptureSession;
import android.hardware.camera2.CaptureRequest;

import androidx.annotation.NonNull;

import com.au.audiorecordplayer.cam2.base.IActionTakePicture;
import com.au.audiorecordplayer.cam2.bean.TakePictureCallbackWrap;
import com.au.audiorecordplayer.cam2.impl.IStatePreviewCallback;
import com.au.audiorecordplayer.cam2.impl.MyCamManager;
import com.au.audiorecordplayer.cam2.impl.PreviewSizeUtil;
import com.au.audiorecordplayer.cam2.impl.picture.TakePictureWorker;
import com.au.audiorecordplayer.util.MyLog;

public class StatePictureAndPreview extends StatePreview implements IActionTakePicture {
    private TakePictureWorker mTakePic;

    public StatePictureAndPreview(MyCamManager mgr) {
        super(mgr);
    }

    @Override
    protected void step0_createSurfaces() {
        super.step0_createSurfaces();
        //由于super中有添加了preview的surface。这里处理拍照即可
        var needSize = new PreviewSizeUtil().needSize("StatePictureAndPreview",
                ImageFormat.JPEG, cameraManager, 1920, 1080);
        MyLog.d("StatePictureAndPreview needSize " + needSize.getWidth() + " * " + needSize.getHeight());

        mTakePic = new TakePictureWorker(cameraManager, needSize.getWidth(), needSize.getHeight());
        allIncludePictureSurfaces.add(mTakePic.getSurface()); //这个添加到allIncludePictureSurfaces 不需要添加到target里面
        MyLog.d("StatePictureAndPreview: allIncludePictureSurfaces.size=" + allIncludePictureSurfaces.size());
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
