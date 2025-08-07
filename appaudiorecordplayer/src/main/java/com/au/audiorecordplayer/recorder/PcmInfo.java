package com.au.audiorecordplayer.recorder;

import android.media.AudioFormat;

import androidx.annotation.NonNull;

public class PcmInfo {
    private int sampleRate;
    private int inChannelConfig;
    private int outChannelConfig;
    private int encodingFmt;
    private String mask;

    public PcmInfo() {
    }

    public PcmInfo(String mask, int sampleRate, int inChannelConfig, int encodingFmt) {
        this.sampleRate = sampleRate;
        this.inChannelConfig = inChannelConfig;
        switch (inChannelConfig) {
            case AudioFormat.CHANNEL_IN_MONO:
                outChannelConfig = AudioFormat.CHANNEL_OUT_MONO;
                break;
            case AudioFormat.CHANNEL_IN_STEREO:
                outChannelConfig = AudioFormat.CHANNEL_OUT_STEREO;
                break;
        }

        this.encodingFmt = encodingFmt;
        this.mask = mask;
    }

    public int getSampleRate() {
        return sampleRate;
    }

    public void setSampleRate(int sampleRate) {
        this.sampleRate = sampleRate;
    }

    public int getInChannelConfig() {
        return inChannelConfig;
    }

    public int getOutChannelConfig() {
        return outChannelConfig;
    }

    public void setInChannelConfig(int channelConfig) {
        this.inChannelConfig = channelConfig;
    }

    public int getEncodingFmt() {
        return encodingFmt;
    }

    public void setEncodingFmt(int encodingFmt) {
        this.encodingFmt = encodingFmt;
    }


    public String getMask() {
        return mask;
    }

    public void setMask(String mask) {
        this.mask = mask;
    }

    @NonNull
    @Override
    public String toString() {
        return "mask:" + mask + " :sampleRate=" + sampleRate + ",channelIn/out=" + inChannelConfig + "/" + outChannelConfig + ",fmt=" + encodingFmt;
    }
}
