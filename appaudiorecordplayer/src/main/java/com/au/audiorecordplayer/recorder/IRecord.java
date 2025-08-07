package com.au.audiorecordplayer.recorder;

public interface IRecord extends ISimpleRecord {
    void resume() throws UnsupportedOperationException;
    void pause() throws UnsupportedOperationException;
}