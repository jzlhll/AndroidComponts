package com.au.aulitesql.executor;

import androidx.annotation.NonNull;

import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

final class ThreadPoolExecutor implements Executor {
    volatile Executor threadPoolExecutor;
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
        getThreadPoolExecutor().execute(newRun);
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
