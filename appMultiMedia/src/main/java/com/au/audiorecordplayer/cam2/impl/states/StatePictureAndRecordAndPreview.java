package com.au.audiorecordplayer.cam2.impl.states;

import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraCaptureSession;
import android.hardware.camera2.CameraCharacteristics;
import android.hardware.camera2.CameraDevice;
import android.hardware.camera2.CameraMetadata;
import android.hardware.camera2.CaptureRequest;
import android.media.CamcorderProfile;
import android.media.MediaRecorder;

import androidx.annotation.NonNull;

import com.au.audiorecordplayer.cam2.base.IActionRecord;
import com.au.audiorecordplayer.cam2.impl.IStateTakePictureRecordCallback;
import com.au.audiorecordplayer.cam2.impl.MyCamManager;
import com.au.audiorecordplayer.cam2.impl.PreviewSizeUtil;
import com.au.audiorecordplayer.util.FileUtil;
import com.au.audiorecordplayer.util.MyLog;

public class StatePictureAndRecordAndPreview extends StatePictureAndPreview implements MediaRecorder.OnErrorListener,
        MediaRecorder.OnInfoListener, IActionRecord {

    private MediaRecorder mMediaRecorder;

    public StatePictureAndRecordAndPreview(MyCamManager mgr) {
        super(mgr);
    }

    private String mLastMp4;

    @Override
    protected void step0_createSurfaces() {
        super.step0_createSurfaces();
        try {
            mMediaRecorder = new MediaRecorder();
            mMediaRecorder.setOnErrorListener(this);
            mMediaRecorder.setOnInfoListener(this);
            mMediaRecorder.setAudioSource(MediaRecorder.AudioSource.DEFAULT);
            mMediaRecorder.setVideoSource(MediaRecorder.VideoSource.SURFACE);
            mMediaRecorder.setOutputFormat(MediaRecorder.OutputFormat.MPEG_4);
            var wishWidth = 1920;
            var wishHeight = 1080;
            if (cameraManager.getCameraId() == CameraCharacteristics.LENS_FACING_FRONT) {
                wishHeight = 1280;
                wishWidth = 720;
            }
            var needSize = new PreviewSizeUtil().needSize("StatePictureAndRecordAndPreview",
                    MediaRecorder.class, cameraManager, wishWidth, wishHeight);
            mMediaRecorder.setVideoSize(needSize.getWidth(), needSize.getHeight());
            mMediaRecorder.setVideoEncoder(MediaRecorder.VideoEncoder.H264);
            CamcorderProfile camPro = CamcorderProfile.get(cameraManager.getCameraId(),
                    cameraManager.getCameraId() == CameraCharacteristics.LENS_FACING_FRONT ?
                            CamcorderProfile.QUALITY_1080P : CamcorderProfile.QUALITY_720P);
            mMediaRecorder.setAudioEncoder(camPro.audioCodec);
            mMediaRecorder.setAudioChannels(camPro.audioChannels);
            // mMediaRecorder.setAudioSamplingRate(camPro.audioSampleRate);
            mMediaRecorder.setAudioSamplingRate(16000);
            mMediaRecorder.setAudioEncodingBitRate(camPro.audioBitRate);
            mMediaRecorder.setVideoEncodingBitRate(camPro.videoBitRate / 2); //码率，自行调节，我希望录制小一点码率
            mMediaRecorder.setVideoFrameRate(camPro.videoFrameRate);
            MyLog.d("Video frame " + camPro.videoFrameRate + " bitRate " + camPro.videoBitRate / 2);
            // mMediaRecorder.setMaxDuration(video.duration);
            // mMediaRecorder.setMaxDuration(30000/*video.duration*/);
            var lastMp4 = FileUtil.getNextRecordFilePath(".mp4");
            mLastMp4 = lastMp4;
            mMediaRecorder.setOutputFile(lastMp4);
            mMediaRecorder.prepare();
        } catch (Exception e) {
            MyLog.ex(e);
            if (mMediaRecorder != null) {
                mMediaRecorder.release();
                mMediaRecorder = null;
            }
        }
        //由于super中有添加了preview和拍照的surface。这里处理好录像surface即可
        assert mMediaRecorder != null;
        addTargetSurfaces.add(mMediaRecorder.getSurface());
        allIncludePictureSurfaces.add(mMediaRecorder.getSurface());
        MyLog.d("State3： allIncludePictureSurfaces.size=" + allIncludePictureSurfaces.size());
    }

    @Override
    protected CameraCaptureSession.StateCallback createCameraCaptureSessionStateCallback(@NonNull CaptureRequest.Builder captureRequestBuilder) {
        return new CameraCaptureSession.StateCallback() {
            @Override
            public void onConfigured(@NonNull CameraCaptureSession cameraCaptureSession) {
                cameraManager.setCamSession(cameraCaptureSession);
                captureRequestBuilder.set(CaptureRequest.CONTROL_MODE, CameraMetadata.CONTROL_MODE_AUTO);
                try {
                    cameraCaptureSession.setRepeatingRequest(captureRequestBuilder.build(),
                            null, cameraManager);
                    if (mMediaRecorder != null) {
                        mMediaRecorder.start();
                    } else {
                        MyLog.e("error!!!! mediaRecord is null");
                    }
                    IStateTakePictureRecordCallback statePPRCB = (IStateTakePictureRecordCallback) mStateBaseCb;
                    statePPRCB.onRecordStart(true);
                } catch (CameraAccessException ignored) {
                }
            }

            @Override
            public void onConfigureFailed(@NonNull CameraCaptureSession cameraCaptureSession) {
                IStateTakePictureRecordCallback statePPRCB = (IStateTakePictureRecordCallback) mStateBaseCb;
                statePPRCB.onRecordStart(false);
            }
        };
    }

    @Override
    protected int step1_getTemplateType() {
        return CameraDevice.TEMPLATE_RECORD;
    }

    @Override
    public synchronized void stopRecord() {
        if (mMediaRecorder == null) return;
        mMediaRecorder.setOnErrorListener(null);
        mMediaRecorder.setOnInfoListener(null);
        mMediaRecorder.stop();
        mMediaRecorder.release();
        mMediaRecorder = null;
        IStateTakePictureRecordCallback statePPRCB = (IStateTakePictureRecordCallback) mStateBaseCb;
        statePPRCB.onRecordEnd(mLastMp4);
    }

    @Override
    public void closeSession() {
        stopRecord();
        super.closeSession();
    }

    @Override
    public void onError(MediaRecorder mediaRecorder, int i, int i1) {
        IStateTakePictureRecordCallback statePPRCB = (IStateTakePictureRecordCallback) mStateBaseCb;
        statePPRCB.onRecordError(i);
    }

    @Override
    public void onInfo(MediaRecorder mediaRecorder, int i, int i1) {
        //TODO file reach file finish
    }
}

