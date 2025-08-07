package com.au.module_android.utils;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * 线程池管理的工具类
 */
public class ThreadPoolUtils {

    private static final class MThreadPollProxyHolder {
        //通过ThreadPoolExecutor的代理类来对线程池的管理
        private static final ThreadPollProxy mThreadPollProxy = new ThreadPollProxy(4, 8, 1000); //单列对象
    }

    public static ThreadPollProxy getThreadPollProxy() {
        return MThreadPollProxyHolder.mThreadPollProxy;
    }

    //通过ThreadPoolExecutor的代理类来对线程池的管理
    public static class ThreadPollProxy {
        private ThreadPoolExecutor poolExecutor;//线程池执行者 ，java内部通过该api实现对线程池管理
        private final int corePoolSize;
        private final int maximumPoolSize;
        private final long keepAliveTime;

        ThreadPollProxy(int corePoolSize, int maximumPoolSize, long keepAliveTime) {
            this.corePoolSize = corePoolSize;
            this.maximumPoolSize = maximumPoolSize;
            this.keepAliveTime = keepAliveTime;
        } //对外提供一个执行任务的方法

        public void execute(Runnable r) {
            if (poolExecutor == null || poolExecutor.isShutdown()) {
                poolExecutor = new ThreadPoolExecutor(
                        //核心线程数量
                        corePoolSize,
                        //最大线程数量
                        maximumPoolSize,
                        //当线程空闲时，保持活跃的时间
                        keepAliveTime, //时间单元 ，
                        // 毫秒级
                        TimeUnit.MILLISECONDS,
                        //线程任务队列
                        new LinkedBlockingQueue<Runnable>(),
                        //创建线程的工厂
                        Executors.defaultThreadFactory());
            }
            poolExecutor.execute(r);
        }
    }
}
