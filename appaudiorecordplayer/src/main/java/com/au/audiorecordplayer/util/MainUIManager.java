package com.au.audiorecordplayer.util;

import android.os.Message;
import android.view.View;

import com.google.android.material.snackbar.Snackbar;

public class MainUIManager implements WeakHandler.Callback {
    private final WeakHandler mMainWeakHandler;

    private MainUIManager() {
        mMainWeakHandler = new WeakHandler(this);
    }

    private static final class MManagerHolder {
        private static final MainUIManager mManager = new MainUIManager();
    }

    public static MainUIManager get() {
        return MManagerHolder.mManager;
    }

    public void toastSnackbar(final View view, final String str) {
        mMainWeakHandler.post(() -> Snackbar.make(view, str, Snackbar.LENGTH_LONG)
                .setAction("Action", null).show());
    }

    public void toastSnackbar(final View view, final String str, final View.OnClickListener l) {
        mMainWeakHandler.post(() -> Snackbar.make(view, str, Snackbar.LENGTH_LONG)
                .setAction("Action", l).show());
    }

    @Override
    public void handleMessage(Message msg) {
        //
    }
}
