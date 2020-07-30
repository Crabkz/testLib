package com.krab.thread;

import android.os.Handler;
import android.os.Looper;

import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

public class ThreadUtils {

    private volatile static ThreadPoolExecutor mThreadPool;
    private volatile static Handler uiHandler;
    private static Thread uiThread;

    public static boolean isMainThread() {
        Thread currentThread = Thread.currentThread();
        if (uiThread == null) {// getMainLooper内部有同步锁，取出缓存
            uiThread = Looper.getMainLooper().getThread();
        }
        return currentThread == uiThread;
    }

    private static Handler getUiHandler() {
        if (uiHandler == null) {
            synchronized (ThreadUtils.class) {
                if (uiHandler == null) {
                    uiHandler = new Handler(Looper.getMainLooper());
                }
            }
        }
        return uiHandler;
    }

    private static ExecutorService getSubThread() {
        if (mThreadPool == null) {
            synchronized (ThreadUtils.class) {
                if (mThreadPool == null) {
                    mThreadPool = new ThreadPoolExecutor(10, 10, 0, TimeUnit.MILLISECONDS, new LinkedBlockingQueue<>(10000), r -> new Thread(r, "Thread-Utils"));
                }
            }
        }
        return mThreadPool;
    }

    /**
     * 提供不依赖Activity Context的runOnUiThread方法
     *
     * @param runnable
     */
    public static void runOnUiThread(Runnable runnable) {
        if (!isMainThread()) {
            getUiHandler().post(runnable);
        } else {
            runnable.run();
        }
    }

    /**
     * 提供线程管理的子线程调用方法
     *
     * @param runnable
     */
    public static void runOnSubThread(Runnable runnable) {
        if (isMainThread()) {
            getSubThread().execute(runnable);
        } else {
            runnable.run();
        }
    }

    public static <T> Future<T> subThreadSubmit(Callable<T> callable) {
        return getSubThread().submit(callable);
    }
}
