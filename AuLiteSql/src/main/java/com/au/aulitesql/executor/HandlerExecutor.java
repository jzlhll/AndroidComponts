package com.au.aulitesql.executor;

import android.os.Handler;
import android.os.HandlerThread;

import java.util.concurrent.Executor;

final class HandlerExecutor implements Executor {

    volatile Handler subHandler;

    @Override
    public void execute(Runnable command) {
        getHandler().post(command);
    }

    private Handler getHandler() {
        if (subHandler == null) {
            synchronized (this) {
                if (subHandler == null) {
                    HandlerThread handlerThread = new HandlerThread("auLiteSql-Thread");
                    handlerThread.start();
                    subHandler = new Handler(handlerThread.getLooper());
                }
            }
        }
        return subHandler;
    }
}
