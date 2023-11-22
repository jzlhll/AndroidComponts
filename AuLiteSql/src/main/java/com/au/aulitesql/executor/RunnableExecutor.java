package com.au.aulitesql.executor;

import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.util.Log;

import androidx.annotation.NonNull;

import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

public final class RunnableExecutor implements IRunnableExecutor, Executor{
    private final static class HandlerExecutor implements Executor {

        private volatile Handler subHandler;

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

    private final static class ThreadPoolExecutor implements Executor {

        private volatile Executor threadPoolExecutor;
        boolean isEnd = false;

        private Executor getThreadPoolExecutor() {
            if (threadPoolExecutor == null) {
                synchronized (this) {
                    if (threadPoolExecutor == null) {
                        threadPoolExecutor = Executors.newSingleThreadExecutor();
                    }
                }
            }
            return threadPoolExecutor;
        }

        @Override
        public void execute(Runnable command) {
            var newRun = new NewRunnable(command);
            threadPoolExecutor.execute(newRun);
        }

        private class NewRunnable implements Runnable {
            private final Runnable runnable;
            NewRunnable(@NonNull Runnable run) {
                this.runnable = run;
            }

            @Override
            public void run() {
                if (!isEnd) {
                    runnable.run();
                }
            }
        }
    }

    private Executor mExecutor;

    @Override
    public void useHandlerMode() {
        mExecutor = new HandlerExecutor();
    }

    @Override
    public void useHandlerMode(Handler subHandler) {
        var executor = new HandlerExecutor();
        executor.subHandler = subHandler;
        mExecutor = executor;
    }

    @Override
    public void useHandlerMode(Looper looper) {
        var executor = new HandlerExecutor();
        executor.subHandler = new Handler(looper);
        mExecutor = executor;
    }

    @Override
    public void useThreadPool() {
        mExecutor = new ThreadPoolExecutor();
    }

    @Override
    public void useThreadPool(Executor executor) {
        var exec = new ThreadPoolExecutor();
        exec.threadPoolExecutor = executor;
        mExecutor = exec;
    }

    @Override
    public void execute(@NonNull Runnable command) {
        var exec = mExecutor;
        if (exec != null) {
            exec.execute(command);
        } else {
            Log.e("AuLiteSql", "You has not set executor or handler!");
        }
    }

    @Override
    public void close() {
        var exec = mExecutor;
        if (exec instanceof HandlerExecutor he) {
            var subHandler = he.subHandler;
            if (subHandler != null) {
                subHandler.removeCallbacksAndMessages(null);
            }
        } else if (exec instanceof ThreadPoolExecutor tpe) {
            tpe.isEnd = true;
        }
    }
}
