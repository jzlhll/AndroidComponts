package com.au.audiorecordplayer.util;

import android.os.Handler;
import android.os.Looper;
import android.os.Message;

import java.lang.ref.WeakReference;

import androidx.annotation.NonNull;

public class WeakHandler extends Handler {
    public interface Callback {
        void handleMessage(Message msg);
    }

    private final WeakReference<Callback> mRef;

    public WeakHandler(Callback callback) {
        super(Looper.getMainLooper());
        mRef = new WeakReference<>(callback);
    }
    public WeakHandler(Callback context, Looper looper) {
        super(looper);
        mRef = new WeakReference<>(context);
    }

    @Override
    public void handleMessage(@NonNull Message msg) {
        Callback ref = mRef != null ? mRef.get() : null;
        if (ref != null) {
            ref.handleMessage(msg);
        }
    }
}
