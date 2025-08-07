package com.au.audiorecordplayer.recorder;

public interface IRecordObserver {
    void onRecordStart();
    void onRecordStop(String filePath);
    void onRecordPause();
    void onRecordResume();
}
