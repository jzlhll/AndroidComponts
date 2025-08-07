package com.au.audiorecordplayer.recorder.a1mediaRecord;

import android.media.MediaRecorder;

import com.au.audiorecordplayer.recorder.IRecord;
import com.au.audiorecordplayer.util.CacheFileGenerator;
import com.au.audiorecordplayer.util.FileUtil;
import com.au.audiorecordplayer.util.MyLog;

import java.io.File;
import java.io.IOException;

public class MediaRecordAudio implements IRecord {
    private MediaRecorder mMediaRecorder;
    private volatile St mCurrentSt = St.NOT_INIT;
    private enum St {
        NOT_INIT,
        RECORDING,
        PAUSING,
    }

    private final static String FILE_NAME =
            CacheFileGenerator.generateCacheFilePath("mediaRecord_", ".amr");

    @Override
    public String getCurrentFilePath() {
        return FILE_NAME;
    }

    @Override
    public boolean isRecording() {
        return mCurrentSt == St.RECORDING;
    }

    @Override
    public void start() throws IOException{
        if (mCurrentSt != St.NOT_INIT) {
            throw new RuntimeException("哈哈乱搞咯");
        }
        File file = new File(FILE_NAME);
        FileUtil.delete(file);

        mMediaRecorder = new MediaRecorder();//初始实例化。
        mMediaRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);//音频输入源
        mMediaRecorder.setOutputFormat(MediaRecorder.OutputFormat.AMR_WB);//设置输出格式
        mMediaRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_WB);//设置编码格式

        mMediaRecorder.setOnInfoListener((mr, what, extra) -> {
            MyLog.d("media record info " + what + ", extra " + extra);
        });

        mMediaRecorder.setAudioEncodingBitRate(16000);

        mMediaRecorder.setOutputFile(file.getAbsolutePath());//设置输出文件的路径
        mMediaRecorder.prepare();//准备录制
        mMediaRecorder.start();//开始录制


        mCurrentSt = St.RECORDING;
    }

    @Override
    public void stop() {
        if (mCurrentSt == St.RECORDING || mCurrentSt == St.PAUSING) {
            mMediaRecorder.stop();
        }
        if (mMediaRecorder != null) mMediaRecorder.release();
        mMediaRecorder = null;
        mCurrentSt = St.NOT_INIT;
    }

    @Override
    public void resume() {
        if (mCurrentSt == St.PAUSING) {
            MyLog.d("resumeeeee");
            mMediaRecorder.resume();
            mCurrentSt = St.RECORDING;
        }
    }

    @Override
    public void pause() {
        if (mCurrentSt == St.RECORDING) {
            MyLog.d("pauseeeee");
            mMediaRecorder.pause();
            mCurrentSt = St.PAUSING;
        }
    }
}
