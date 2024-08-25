package com.au.aulitesql.executor;

import android.os.Handler;
import android.os.Looper;

import java.util.concurrent.Executor;

/**
 * @author au
 * @date :2023/11/22 12:27
 * @description:
 */
public interface IRunnableExecutor extends Executor {
    void useHandlerMode();

    void useHandlerMode(Handler subHandler);

    void useHandlerMode(Looper looper);

    void useThreadPool();

    void useThreadPool(Executor executor);

    void close();
}
