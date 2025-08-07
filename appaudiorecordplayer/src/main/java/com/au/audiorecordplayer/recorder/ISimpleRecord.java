package com.au.audiorecordplayer.recorder;

public interface ISimpleRecord {
    void start() throws Exception;
    void stop();
    boolean isRecording();

    String getCurrentFilePath();
}