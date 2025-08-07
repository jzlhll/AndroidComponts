package com.au.audiorecordplayer.recorder.a2AudioRecord;

import android.Manifest;
import android.media.AudioFormat;
import android.media.AudioRecord;
import android.media.MediaRecorder;

import androidx.annotation.RequiresPermission;

import com.au.audiorecordplayer.recorder.ISimpleRecord;
import com.au.audiorecordplayer.recorder.PCMAndWavUtil;
import com.au.audiorecordplayer.recorder.PcmInfo;
import com.au.audiorecordplayer.util.CacheFileGenerator;
import com.au.audiorecordplayer.util.MyLog;
import com.au.module_android.utils.ThreadPoolUtils;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;

/**
 * SimplePCMAudioRecord是一个实现了普通录制声音的代码。只能录制PCM。不支持停止的模式。而且PCM不能播放。
 * 2.0版本则将使用randomAccessFile直接先写入头；再录制；再最后stop的时候跳回去写入length即可。
 */
public class SimpleWavAudioRecord2_0 implements ISimpleRecord {
    private static final String TAG = SimpleWavAudioRecord2_0.class.getSimpleName();
    private volatile AudioRecord mAudioRecord;
    private int SAMPLE_RATE = 44100; //采样率
    private int CHANNEL_CONFIG = AudioFormat.CHANNEL_IN_STEREO;//双声道;//AudioFormat.CHANNEL_IN_MONO;//单声道
    private int FORMAT = AudioFormat.ENCODING_PCM_16BIT;//AudioFormat.ENCODING_PCM_8BIT;//音频格式
    private int mMinBufferSize;
    private byte[] mData;
    private boolean mIsRecording = false;

    private final String FILE_NAME;

    private static PcmInfo[] mPcmInfoList;
    public static PcmInfo[] getAllPcmInfos() { //8bit 很多设备不支持8bit(AudioFormat.ENCODING_PCM_8BIT 3)
                                            // 都支持encoding16bit(AudioFormat.ENCODING_PCM_16BIT  2)
                                            //CHANNEL_IN_MONO单通道都支持
        if (mPcmInfoList == null) {
            mPcmInfoList = new PcmInfo[]{
                    new PcmInfo("16k-Mono-16bit",16000, AudioFormat.CHANNEL_IN_MONO, AudioFormat.ENCODING_PCM_16BIT),
                    new PcmInfo("16k-Stereo-16bit",16000, AudioFormat.CHANNEL_IN_STEREO, AudioFormat.ENCODING_PCM_16BIT),
                    new PcmInfo("16k-Stereo-8bit",16000, AudioFormat.CHANNEL_IN_STEREO, AudioFormat.ENCODING_PCM_8BIT),
                    new PcmInfo("16k-Mono-8bit",16000, AudioFormat.CHANNEL_IN_MONO, AudioFormat.ENCODING_PCM_8BIT),

                    new PcmInfo("44k-Stereo-16bit",44100, AudioFormat.CHANNEL_IN_STEREO, AudioFormat.ENCODING_PCM_16BIT),
                    new PcmInfo("44k-Mono-16bit", 44100, AudioFormat.CHANNEL_IN_MONO, AudioFormat.ENCODING_PCM_16BIT),
                    new PcmInfo("44k-Stereo-8bit",44100, AudioFormat.CHANNEL_IN_STEREO, AudioFormat.ENCODING_PCM_8BIT),
                    new PcmInfo("44k-Mono-8bit",44100, AudioFormat.CHANNEL_IN_MONO, AudioFormat.ENCODING_PCM_8BIT),
            };
        }
        return mPcmInfoList;
    }

    public SimpleWavAudioRecord2_0(PcmInfo pcmInfo) {
        SAMPLE_RATE = pcmInfo.getSampleRate();
        CHANNEL_CONFIG = pcmInfo.getInChannelConfig();
        FORMAT = pcmInfo.getEncodingFmt();
        FILE_NAME = CacheFileGenerator.generateCacheFilePath("record2_0_ts", ".wav");
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
                mMinBufferSize = AudioRecord.getMinBufferSize(SAMPLE_RATE, CHANNEL_CONFIG, FORMAT);
                MyLog.d(TAG, "min buff size " + mMinBufferSize + " ; " + SAMPLE_RATE + " " + CHANNEL_CONFIG + " " + FORMAT);
                if (mMinBufferSize < 0) {
                    MyLog.d(TAG, "错误的miniBuffSize " + mMinBufferSize);
                    return;
                }
                MyLog.d(TAG, "min buff size " + mMinBufferSize);
                mAudioRecord = new AudioRecord(MediaRecorder.AudioSource.MIC, SAMPLE_RATE,
                        CHANNEL_CONFIG, FORMAT, mMinBufferSize);
                mData = new byte[mMinBufferSize];

                mAudioRecord.startRecording();
                mIsRecording = true;
                File f = new File(FILE_NAME);
                if (f.exists()) {
                    f.delete();
                }//先删掉，再重建RandomAccessFile文件

                RandomAccessFile file = null;
                try {
                    file = new RandomAccessFile(f, "rw");
                } catch (FileNotFoundException e) {
                    com.au.audiorecordplayer.util.MyLog.ex(e);
                }

                PCMAndWavUtil pcm = new PCMAndWavUtil(SAMPLE_RATE, CHANNEL_CONFIG, FORMAT);
                long dataLength = 0;
                if (null != file) {
                    try {
                        pcm.addPcmHeader(file);
                        MyLog.d("成功添加fake head！");
                    } catch (IOException e) {
                        com.au.audiorecordplayer.util.MyLog.ex(e);
                    }
                    while (mIsRecording) {
                        int read = mAudioRecord.read(mData, 0, mMinBufferSize);
                        // 如果读取音频数据没有出现错误，就将数据写入到文件
                        if (AudioRecord.ERROR_INVALID_OPERATION != read) {
                            try {
                                file.write(mData);
                                dataLength += mData.length;
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

                    if(file != null) {
                        pcm.endPcmHeader(file, dataLength);
                        file.close();
                    }
                } catch (IOException e) {
                    com.au.audiorecordplayer.util.MyLog.ex(e);
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
