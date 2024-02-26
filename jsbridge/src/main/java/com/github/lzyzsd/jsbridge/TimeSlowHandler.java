package com.github.lzyzsd.jsbridge;

import android.os.Handler;
import android.os.SystemClock;

import androidx.annotation.MainThread;
import androidx.annotation.NonNull;

/**
 * @author allan
 * @date :2024/2/22 19:30
 * 当调用执行动作，进行稀疏处理，保证每次执行，但是每次执行必须有最小间隔时间。
 */
public class TimeSlowHandler{
    /**
     * 最小间隔时间
     */
    public long minSpaceTime = 50;

    //下次允许执行的时间
    private long nextRunTime = 0L;

    private final Handler handler;

    public TimeSlowHandler(@NonNull Handler handler) {
        this.handler = handler;
    }

    /**
     * 执行一次
     */
    @MainThread
    public void execute(@NonNull Runnable runnable) {
        long cur = SystemClock.elapsedRealtime();
        long delta = cur - nextRunTime;
        if (delta >= 0) {
            runnable.run();
            nextRunTime = cur + minSpaceTime;
        } else {
            long shouldDelayTime = nextRunTime - cur; //再根据下次可以执行的时间点，减去现在就是delay的时间了。
            handler.postDelayed(runnable, shouldDelayTime);
            nextRunTime += minSpaceTime; //那么下次再允许执行的时间，再往后延。
        }
    }
}
