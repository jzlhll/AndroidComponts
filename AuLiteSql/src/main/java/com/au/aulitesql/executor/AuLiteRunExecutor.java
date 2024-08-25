package com.au.aulitesql.executor;

import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import androidx.annotation.NonNull;

import java.util.concurrent.Executor;

public final class AuLiteRunExecutor implements IRunnableExecutor, Executor{
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
