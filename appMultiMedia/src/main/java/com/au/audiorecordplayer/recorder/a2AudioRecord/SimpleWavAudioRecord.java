package com.au.audiorecordplayer.recorder.a2AudioRecord;

import com.au.audiorecordplayer.recorder.ISimpleRecord;
import com.au.audiorecordplayer.recorder.PCMAndWavUtil;
import com.au.audiorecordplayer.util.CacheFileGenerator;

import java.io.File;

/**
 * 这是写完了以后再进行的2次处理。那他么的岂不是要将整个文件拷贝一次？！
 */
public class SimpleWavAudioRecord implements ISimpleRecord, IRecordCompletedCallback {
    private static final String FILE_NAME_WAV = CacheFileGenerator.generateCacheFilePath("testV1", ".wav");
    private final SimplePCMAudioRecord simplePCMAudioRecord;

    public SimpleWavAudioRecord() {
        simplePCMAudioRecord = new SimplePCMAudioRecord();
        simplePCMAudioRecord.setCompletedCallback(this);
    }

    @Override
    public boolean isRecording() {
        return simplePCMAudioRecord.isRecording();
    }

    @Override
    public void start() {
        simplePCMAudioRecord.start();
    }

    @Override
    public void stop() {
        simplePCMAudioRecord.stop();
    }

    @Override
    public void onComplete(File file) {
        if (file.exists()) {
            PCMAndWavUtil pcmToWavUtil = new PCMAndWavUtil(SimplePCMAudioRecord.SAMPLE_RATE,
                    SimplePCMAudioRecord.CANNEL_CONFIG, SimplePCMAudioRecord.FORMAT);
            File wavFile = new File(FILE_NAME_WAV);
            if (wavFile.exists()) {
                wavFile.delete();
            }
            //这是写完了以后再进行的2次处理。那他么的岂不是要将整个文件拷贝一次？！
            pcmToWavUtil.pcmToWav(file.getAbsolutePath(), wavFile.getAbsolutePath());
        }
    }

    @Override
    public String getCurrentFilePath() {
        return FILE_NAME_WAV;
    }
}

