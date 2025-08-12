package com.au.audiorecordplayer.cam2.view;

import android.graphics.SurfaceTexture;
import android.view.SurfaceHolder;

import androidx.annotation.Nullable;

public interface IViewStatusChangeCallback {
    void onSurfaceCreated(@Nullable SurfaceHolder holder, @Nullable SurfaceTexture surfaceTexture);
    void onSurfaceDestroyed();
    void onSurfaceChanged();
}
