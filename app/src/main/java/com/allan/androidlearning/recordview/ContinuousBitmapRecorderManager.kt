package com.allan.androidlearning.recordview

import android.graphics.Bitmap
import android.media.MediaRecorder
import com.au.module_android.utils.logStace
import com.au.module_android.utils.logd
import java.io.File
import java.io.IOException

class ContinuousBitmapRecorderManager {
    private var mRecording = false
    private var mRecorder:ContinuousBitmapRecorder? = null

    var curRecordFile : File? = null

    fun setup(outputFileStr: String, bitmapOffer:()-> Bitmap?) : Boolean {
        if (mRecording) {
            return false
        }

        val outputFile = File(outputFileStr)
        curRecordFile = outputFile
        try {
            if (outputFile.exists()) {
                outputFile.delete()
            } else {
                val dir = outputFile.parent ?: return false
                val directory = File(dir)
                directory.mkdirs()
            }
        } catch (e:Exception) {
            e.printStackTrace()
            return false
        }

        val record = ContinuousBitmapRecorder(bitmapOffer).apply {
//            setAudioSource(MediaRecorder.AudioSource.MIC) // uncomment this line if audio required
            setVideoSource(MediaRecorder.VideoSource.SURFACE)
            setOutputFormat(MediaRecorder.OutputFormat.MPEG_4)
 //           setAudioEncoder(MediaRecorder.AudioEncoder.AAC)
            setVideoFrameRate(30) // 5fps
            setVideoEncoder(MediaRecorder.VideoEncoder.H264)
            setVideoSize(720, 1280)
            setVideoEncodingBitRate(3000 * 1000)
            setOutputFile(outputFile)
            setOnErrorListener { mr, what, extra->
                logd { "record error: $what, $extra" }
            }
        }
        mRecorder = record
        return true
    }

    fun startRecord() : Boolean {
        val r = mRecorder?: return false
        try {
            r.prepare()
            r.start()
        } catch (e: IOException) {
            logStace("startRecord failed")
            return false
        }

        logd { "startRecord successfully!" }
        mRecording = true
        return true
    }

    fun stopRecord() : Boolean{
        val r = mRecorder ?: return false
        mRecording = false
        try {
            r.stop()
            r.reset()
            r.release()
            mRecorder = null
        } catch (e: Exception) {
            e.printStackTrace()
            return false
        }

        logd { "stopRecord successfully!" }
        return true
    }
}