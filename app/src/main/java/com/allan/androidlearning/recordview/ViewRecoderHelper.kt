package com.allan.androidlearning.recordview

import android.content.Context
import android.media.MediaRecorder
import android.os.Handler
import android.os.HandlerThread
import android.util.Log
import com.au.module_android.utils.ignoreError
import java.io.File
import java.io.IOException


/**
 * @author allan
 * @date :2024/6/20 18:17
 * @description:
 */
class ViewRecoderHelper {
    private var mRecording = false
    private lateinit var mWorkerHandler: Handler
    private

    fun setup() {
        val ht = HandlerThread("bg_view_recorder")
        ht.start()
        mWorkerHandler = Handler(ht.looper)
    }

    private fun startRecord(outputFileStr:String) : Boolean {
        val suc = ignoreError {
            val outputFile = File(outputFileStr)
            if (outputFile.exists()) {
                outputFile.delete()
            } else {
                val directory = File(outputFile.parent)
                if (directory != null) {
                    directory.mkdirs()
                    if (!directory.exists()) {
                        Log.w(TAG, "startRecord failed: $directory does not exist!")
                        return
                    }
                }
            }
            Unit
        }
        if (suc == null) {
            return false
        }

        mViewRecorder = ViewRecorder()
        mViewRecorder.setAudioSource(MediaRecorder.AudioSource.MIC) // uncomment this line if audio required
        mViewRecorder.setVideoSource(MediaRecorder.VideoSource.SURFACE)
        mViewRecorder.setOutputFormat(MediaRecorder.OutputFormat.MPEG_4)
        mViewRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AAC)
        mViewRecorder.setVideoFrameRate(5) // 5fps
        mViewRecorder.setVideoEncoder(MediaRecorder.VideoEncoder.H264)
        mViewRecorder.setVideoSize(720, 1280)
        mViewRecorder.setVideoEncodingBitRate(2000 * 1000)
        mViewRecorder.setOutputFile(getCacheDir() + "/" + System.currentTimeMillis() + ".mp4")
        mViewRecorder.setOnErrorListener(mOnErrorListener)

        mViewRecorder.setRecordedView(mTextView)
        try {
            mViewRecorder.prepare()
            mViewRecorder.start()
        } catch (e: IOException) {
            Log.e(TAG, "startRecord failed", e)
            return
        }

        Log.d(TAG, "startRecord successfully!")
        mRecording = true
    }

    private fun stopRecord() {
        try {
            mViewRecorder.stop()
            mViewRecorder.reset()
            mViewRecorder.release()
        } catch (e: Exception) {
            e.printStackTrace()
        }
        mRecording = false
        Log.d(TAG, "stopRecord successfully!")
    }
}