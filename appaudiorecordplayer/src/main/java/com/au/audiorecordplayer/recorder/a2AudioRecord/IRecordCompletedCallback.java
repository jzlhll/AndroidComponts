package com.au.audiorecordplayer.recorder.a2AudioRecord;

import androidx.annotation.WorkerThread;

import java.io.File;

public interface IRecordCompletedCallback {
    @WorkerThread
    void onComplete(File file);
}
