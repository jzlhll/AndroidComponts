package com.au.audiorecordplayer.recorder.a2AudioRecord;

import android.Manifest;
import android.media.AudioFormat;
import android.media.AudioRecord;
import android.media.MediaRecorder;

import androidx.annotation.RequiresPermission;

import com.au.audiorecordplayer.recorder.ISimpleRecord;
import com.au.audiorecordplayer.util.CacheFileGenerator;
import com.au.audiorecordplayer.util.MyLog;
import com.au.module_android.utils.ThreadPoolUtils;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
/**
 * 这是一个实现了普通录制声音的代码。只能录制PCM。不支持停止的模式。而且PCM不能播放。
 */
public class SimplePCMAudioRecord implements ISimpleRecord {
    private static final String TAG = SimplePCMAudioRecord.class.getSimpleName();
    private AudioRecord mAudioRecord;
    public static int SAMPLE_RATE = 16000; //采样率
    public static int CANNEL_CONFIG = AudioFormat.CHANNEL_IN_STEREO;//双声道;//AudioFormat.CHANNEL_IN_MONO;//单声道
    public static int FORMAT = AudioFormat.ENCODING_PCM_16BIT;//音频格式
    private int mMinBufferSize;
    private byte[] mData;
    private boolean mIsRecording = false;

    private static final String FILE_NAME = CacheFileGenerator.generateCacheFilePath("testV1", ".pcm");

    private IRecordCompletedCallback mCompletedCallback;
    public void setCompletedCallback(IRecordCompletedCallback mCompletedCallback) {
        this.mCompletedCallback = mCompletedCallback;
    }

    public SimplePCMAudioRecord() {
    }

    @Override
    public String getCurrentFilePath() {
        return FILE_NAME;
    }

    @Override
    public boolean isRecording() {
        return mAudioRecord != null;
    }

    @Override
    public void start() {
        ThreadPoolUtils.getThreadPollProxy().execute(new Runnable() {
            @RequiresPermission(Manifest.permission.RECORD_AUDIO)
            @Override
            public void run() {
                if (mAudioRecord != null) {
                    throw new RuntimeException("错误init");
                }
                mMinBufferSize = AudioRecord.getMinBufferSize(SAMPLE_RATE, CANNEL_CONFIG, FORMAT);
                MyLog.d(TAG, "min buff size " + mMinBufferSize + " ; " + SAMPLE_RATE + " " + CANNEL_CONFIG + " " + FORMAT);
                if (mMinBufferSize < 0) {
                    MyLog.e(TAG, "Error ! min buff size " + mMinBufferSize);
                    return;
                }
                mAudioRecord = new AudioRecord(MediaRecorder.AudioSource.MIC, SAMPLE_RATE,
                        CANNEL_CONFIG, FORMAT, mMinBufferSize);
                mData = new byte[mMinBufferSize];

                mAudioRecord.startRecording();
                mIsRecording = true;
                File file = new File(FILE_NAME);
                if (file.exists()) {
                    file.delete();
                }

                FileOutputStream fos = null;
                try {
                    fos = new FileOutputStream(file);
                }catch (FileNotFoundException e) {
                    com.au.audiorecordplayer.util.MyLog.ex(e);
                }

                if (null != fos) {
                    while (mIsRecording) {
                        int read = mAudioRecord.read(mData, 0, mMinBufferSize);
                        // 如果读取音频数据没有出现错误，就将数据写入到文件
                        if (AudioRecord.ERROR_INVALID_OPERATION != read) {
                            try {
                                fos.write(mData);
                            } catch (IOException e) {
                                com.au.audiorecordplayer.util.MyLog.ex(e);
                            }
                        }
                    }

                    MyLog.d("isRecord end ! (mIsRecording=" + mIsRecording + ")");
                    if (!mIsRecording) {
                        mAudioRecord.stop();
                    }
                }

                try {
                    MyLog.d(TAG, "run: close file output stream !");
                    if(fos != null) fos.close();
                } catch (IOException e) {
                    com.au.audiorecordplayer.util.MyLog.ex(e);
                }

                if (mCompletedCallback != null) {
                    mCompletedCallback.onComplete(file);
                }

                MyLog.d(TAG, "release in run()");
                if (mAudioRecord != null) {
                    mAudioRecord.release();
                }
                mData = null;
                mAudioRecord = null;
                MyLog.d(TAG,"release!!!");
            }
        });
    }

    @Override
    public void stop() {
        mIsRecording = false;
    }
}
