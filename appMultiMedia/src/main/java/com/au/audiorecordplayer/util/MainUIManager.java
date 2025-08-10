package com.au.audiorecordplayer.util;

import android.app.AlertDialog;
import android.content.Context;
import android.os.Message;
import android.view.View;
import android.widget.Toast;

import com.google.android.material.snackbar.Snackbar;

public class MainUIManager implements WeakHandler.Callback {
    private final WeakHandler mMainWeakHandler;

    private MainUIManager() {
        mMainWeakHandler = new WeakHandler(this);
    }

    private static final class MManagerHolder {
        private static final MainUIManager mManager = new MainUIManager();
    }

    public static WeakHandler mainHandler() {return MManagerHolder.mManager.mMainWeakHandler;}

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

    public static void toastOld(Context context, String s) {
        Toast.makeText(context, s, Toast.LENGTH_SHORT).show();
    }

    public static void toastNew(final Context context, View parent, String s) {
        Snackbar.make(parent, s, Snackbar.LENGTH_LONG).show();
    }

    public static void toastNew(final Context context, View parent, String s, String action, View.OnClickListener listener) {
        Snackbar.make(parent, s, Snackbar.LENGTH_LONG).setAction(action, listener).show();
    }

    public static void alertDialog(final Context context, String warningInfo) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle("Warning").setMessage(warningInfo).setPositiveButton("OK", null);
        builder.create().show();
    }

    @Override
    public void handleMessage(Message msg) {
        //
    }
}
