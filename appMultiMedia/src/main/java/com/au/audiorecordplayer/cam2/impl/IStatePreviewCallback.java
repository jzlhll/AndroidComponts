package com.au.audiorecordplayer.cam2.impl;

public interface IStatePreviewCallback extends IStateBaseCallback{
        void onPreviewSucceeded();
        void onPreviewFailed();
    }