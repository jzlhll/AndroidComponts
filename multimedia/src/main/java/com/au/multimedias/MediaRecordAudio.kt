package com.au.multimedias

import android.annotation.SuppressLint
import android.content.Context
import android.media.MediaRecorder
import android.os.Build
import android.os.Environment
import android.util.Log
import androidx.annotation.ChecksSdkIntAtLeast
import java.io.File
import java.io.IOException
import java.lang.StringBuilder

@SuppressLint("AnnotateVersionCheck")
class MediaRecordAudio(private val fileName:String) : IRecord {
    private var mMediaRecorder: MediaRecorder? = null

    @Volatile
    private var mCurrentSt = St.NOT_INIT

    private enum class St {
        NOT_INIT,
        RECORDING,
        PAUSED
    }

    override val isRecording: Boolean
        get() = mCurrentSt == St.RECORDING

    override val isPaused: Boolean
        get() = mCurrentSt == St.PAUSED

    override fun start(context:Context) {
        if (mCurrentSt != St.NOT_INIT) {
            throw RuntimeException("哈哈乱搞咯")
        }

        val file = File(fileName)
        if (file.exists()) {
            file.delete()
        }
        //        if (!file.exists()) {
//            try {
//                file.createNewFile();
//            } catch (IOException e) {
//                e.printStackTrace();
//            }
//        }

        //初始实例化。
        val recorder = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            MediaRecorder(context)
        } else {
            MediaRecorder()
        }

        mMediaRecorder = recorder
        recorder.setAudioSource(MediaRecorder.AudioSource.MIC) //音频输入源
        recorder.setOutputFormat(MediaRecorder.OutputFormat.AMR_NB) //设置输出格式
        recorder.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_WB) //设置编码格式
        recorder.setAudioEncodingBitRate(16000)
        recorder.setOutputFile(file.absolutePath) //设置输出文件的路径

        try {
            recorder.prepare() //准备录制
            recorder.start() //开始录制
            mCurrentSt = St.RECORDING
        } catch (e: IOException) {
            e.printStackTrace()
        }
    }

    override fun stop() {
        if (mCurrentSt == St.RECORDING || mCurrentSt == St.PAUSED) {
            mMediaRecorder?.stop()
        }
        mMediaRecorder?.release()
        mMediaRecorder = null
        mCurrentSt = St.NOT_INIT
    }

    override fun resume(file: String) {
        if (mCurrentSt == St.PAUSED) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                Log.d("MediaRecord", "resumeeeee")
                mMediaRecorder?.resume()
                mCurrentSt = St.RECORDING
            }
        }
    }

    override val supportResume: Boolean
        get() = Build.VERSION.SDK_INT >= Build.VERSION_CODES.N

    override fun pause() {
        if (mCurrentSt == St.RECORDING) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                Log.d("MediaRecord", "pauseeeee")
                mMediaRecorder?.pause()
                mCurrentSt = St.PAUSED
            }
        }
    }
}
