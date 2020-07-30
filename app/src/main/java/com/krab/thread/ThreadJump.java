package com.krab.thread;

/**
 * @author xkz
 * @date 2020/7/6 14:50
 */
public interface ThreadJump<T> {
    int CURRENT = 0;
    int MAIN = 1;
    int SUB = 2;

    ThreadJump<T> jump(int jumpTo);

    <V> ThreadJump<V> put(ConnectedCallable<T, V> callable);

    <V> ThreadJump<V> put(Callable<V> callable);

    ThreadJump<Object> put(Runnable runnable);

    ThreadJump<Object> put(ConnectedRunnable<T> runnable);

    <V> ThreadJump<V> ePut(SConnectedCallable<T, V> callable);

    <V> ThreadJump<V> ePut(SCallable<V> callable);

    ThreadJump<Object> ePut(SRunnable runnable);

    ThreadJump<Object> ePut(SConnectedRunnable<T> runnable);

    JumpEngine complete();


    interface SConnectedCallable<K, V> {
        V call(JumpEngine jumpEngine, K t);
    }

    interface SCallable<V> {
        V call(JumpEngine jumpEngine);
    }

    interface SConnectedRunnable<K> {
        void run(JumpEngine jumpEngine, K t);
    }

    interface SRunnable {
        void run(JumpEngine jumpEngine);
    }

    interface ConnectedCallable<K, V> {
        V call(K t);
    }

    interface Callable<V> {
        V call();
    }

    interface ConnectedRunnable<K> {
        void run(K t);
    }

    interface Runnable {
        void run();
    }
}
