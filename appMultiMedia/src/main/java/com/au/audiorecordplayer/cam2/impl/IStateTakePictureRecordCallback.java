package com.au.audiorecordplayer.cam2.impl;

public interface IStateTakePictureRecordCallback extends IStatePreviewCallback {
        void onRecordStart(boolean suc);

        void onRecordError(int err);

        void onRecordEnd(String path);
    }