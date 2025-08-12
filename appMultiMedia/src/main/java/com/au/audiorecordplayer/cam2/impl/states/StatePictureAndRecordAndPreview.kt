package com.au.audiorecordplayer.cam2.impl.states

import android.content.Context
import android.hardware.camera2.CameraAccessException
import android.hardware.camera2.CameraCaptureSession
import android.hardware.camera2.CameraCharacteristics
import android.hardware.camera2.CameraDevice
import android.hardware.camera2.CameraManager
import android.hardware.camera2.CameraMetadata
import android.hardware.camera2.CaptureRequest
import android.media.CamcorderProfile
import android.media.MediaRecorder
import com.au.audiorecordplayer.cam2.base.IActionRecord
import com.au.audiorecordplayer.cam2.impl.IStateTakePictureRecordCallback
import com.au.audiorecordplayer.cam2.impl.MyCamManager
import com.au.audiorecordplayer.cam2.impl.PreviewSizeUtil
import com.au.audiorecordplayer.util.FileUtil
import com.au.audiorecordplayer.util.MyLog
import com.au.module_android.Globals

class StatePictureAndRecordAndPreview(mgr: MyCamManager) : StatePictureAndPreview(mgr), MediaRecorder.OnErrorListener, MediaRecorder.OnInfoListener, IActionRecord {
    private var mMediaRecorder: MediaRecorder? = null

    private var mLastMp4: String? = null

    override fun step0_createSurfaces() {
        super.step0_createSurfaces()
        try {
            mMediaRecorder = MediaRecorder().also {
                it.setOnErrorListener(this)
                it.setOnInfoListener(this)
                it.setAudioSource(MediaRecorder.AudioSource.DEFAULT)
                it.setVideoSource(MediaRecorder.VideoSource.SURFACE)
                it.setOutputFormat(MediaRecorder.OutputFormat.MPEG_4)

                var wishWidth = 1920
                var wishHeight = 1080
                if (cameraManager.cameraId == CameraCharacteristics.LENS_FACING_FRONT) {
                    wishHeight = 1280
                    wishWidth = 720
                }
                val systemCameraManager = Globals.app.getSystemService(Context.CAMERA_SERVICE) as CameraManager
                val needSize = PreviewSizeUtil().needSize(
                    "StatePictureAndRecordAndPreview",
                    MediaRecorder::class.java, systemCameraManager, "" + cameraManager.cameraId, wishWidth, wishHeight
                )
                it.setVideoSize(needSize.getWidth(), needSize.getHeight())
                it.setVideoEncoder(MediaRecorder.VideoEncoder.H264)
                val camPro = CamcorderProfile.get(
                    cameraManager.cameraId,
                    if (cameraManager.cameraId == CameraCharacteristics.LENS_FACING_FRONT) CamcorderProfile.QUALITY_1080P else CamcorderProfile.QUALITY_720P
                )
                it.setAudioEncoder(camPro.audioCodec)
                it.setAudioChannels(camPro.audioChannels)
                // mMediaRecorder.setAudioSamplingRate(camPro.audioSampleRate);
                it.setAudioSamplingRate(16000)
                it.setAudioEncodingBitRate(camPro.audioBitRate)
                it.setVideoEncodingBitRate(camPro.videoBitRate / 2) //码率，自行调节，我希望录制小一点码率
                it.setVideoFrameRate(camPro.videoFrameRate)
                MyLog.d("Video frame " + camPro.videoFrameRate + " bitRate " + camPro.videoBitRate / 2)
                // mMediaRecorder.setMaxDuration(video.duration);
                // mMediaRecorder.setMaxDuration(30000/*video.duration*/);
                val lastMp4 = FileUtil.getNextRecordFilePath(".mp4")
                mLastMp4 = lastMp4
                it.setOutputFile(lastMp4)
                it.prepare()

                addTargetSurfaces!!.add(it.surface)
                allIncludePictureSurfaces!!.add(it.surface)
            }
        } catch (e: Exception) {
            MyLog.ex(e)
            if (mMediaRecorder != null) {
                mMediaRecorder!!.release()
                mMediaRecorder = null
            }
        }
        //由于super中有添加了preview和拍照的surface。这里处理好录像surface即可
        checkNotNull(mMediaRecorder)
        MyLog.d("State3： allIncludePictureSurfaces.size=" + allIncludePictureSurfaces!!.size)
    }

    override fun createCameraCaptureSessionStateCallback(captureRequestBuilder: CaptureRequest.Builder): CameraCaptureSession.StateCallback {
        return object : CameraCaptureSession.StateCallback() {
            override fun onConfigured(cameraCaptureSession: CameraCaptureSession) {
                camSession = cameraCaptureSession
                captureRequestBuilder.set(CaptureRequest.CONTROL_MODE, CameraMetadata.CONTROL_MODE_AUTO)
                try {
                    cameraCaptureSession.setRepeatingRequest(
                        captureRequestBuilder.build(),
                        null, cameraManager
                    )
                    if (mMediaRecorder != null) {
                        mMediaRecorder!!.start()
                    } else {
                        MyLog.e("error!!!! mediaRecord is null")
                    }
                    val statePPRCB = mStateBaseCb as IStateTakePictureRecordCallback?
                    statePPRCB!!.onRecordStart(true)
                } catch (ignored: CameraAccessException) {
                }
            }

            override fun onConfigureFailed(cameraCaptureSession: CameraCaptureSession) {
                val statePPRCB = mStateBaseCb as IStateTakePictureRecordCallback?
                statePPRCB!!.onRecordStart(false)
            }
        }
    }

    override fun step1_getTemplateType(): Int {
        return CameraDevice.TEMPLATE_RECORD
    }

    @Synchronized
    override fun stopRecord() {
        val mediaRecorder = mMediaRecorder
        if (mediaRecorder == null) return
        mediaRecorder.setOnErrorListener(null)
        mediaRecorder.setOnInfoListener(null)
        mediaRecorder.stop()
        mediaRecorder.release()
        mMediaRecorder = null
        val statePPRCB = mStateBaseCb as IStateTakePictureRecordCallback?
        statePPRCB!!.onRecordEnd(mLastMp4)
    }

    public override fun closeSession() {
        stopRecord()
        super.closeSession()
    }

    override fun onError(mediaRecorder: MediaRecorder?, i: Int, i1: Int) {
        val statePPRCB = mStateBaseCb as IStateTakePictureRecordCallback?
        statePPRCB!!.onRecordError(i)
    }

    override fun onInfo(mediaRecorder: MediaRecorder?, i: Int, i1: Int) {
        //TODO file reach file finish
    }
}

