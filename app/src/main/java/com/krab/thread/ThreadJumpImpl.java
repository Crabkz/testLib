package com.krab.thread;

/**
 * @author xkz
 * @date 2020/7/6 11:58
 */
public class ThreadJumpImpl<T> implements ThreadJump<T> {
    private final JumpEngine jumpEngine;

    private int jumpTo;

    public static ThreadJumpImpl<Object> instance(JumpEngine jumpEngine) {
        return new ThreadJumpImpl<>(jumpEngine);
    }

    private ThreadJumpImpl(JumpEngine jumpEngine) {
        this.jumpEngine = jumpEngine;
    }

    /**
     * 不调用则使用当前线程
     *
     * @param jumpTo
     * @return
     */
    public ThreadJumpImpl<T> jump(int jumpTo) {
        this.jumpTo = jumpTo;
        return this;
    }

    public <V> ThreadJumpImpl<V> put(ConnectedCallable<T, V> callable) {
        packaging(callable);
        return new ThreadJumpImpl<>(jumpEngine);
    }

    public <V> ThreadJumpImpl<V> put(Callable<V> callable) {
        packaging(callable);
        return new ThreadJumpImpl<>(jumpEngine);
    }

    public ThreadJumpImpl<Object> put(Runnable runnable) {
        packaging(runnable);
        return new ThreadJumpImpl<>(jumpEngine);
    }

    public ThreadJumpImpl<Object> put(ConnectedRunnable<T> runnable) {
        packaging(runnable);
        return new ThreadJumpImpl<>(jumpEngine);
    }

    @Override
    public <V> ThreadJump<V> ePut(SConnectedCallable<T, V> callable) {
        packaging(callable);
        return new ThreadJumpImpl<>(jumpEngine);
    }

    @Override
    public <V> ThreadJump<V> ePut(SCallable<V> callable) {
        packaging(callable);
        return new ThreadJumpImpl<>(jumpEngine);
    }

    @Override
    public ThreadJump<Object> ePut(SRunnable runnable) {
        packaging(runnable);
        return new ThreadJumpImpl<>(jumpEngine);
    }

    @Override
    public ThreadJump<Object> ePut(SConnectedRunnable<T> runnable) {
        packaging(runnable);
        return new ThreadJumpImpl<>(jumpEngine);
    }

    private void packaging(Object task) {
        JumpEngine.Package aPackage = new JumpEngine.Package();
        aPackage.jumpTo = jumpTo;
        aPackage.task = task;
        jumpEngine.packages.add(aPackage);
    }

    public JumpEngine complete() {
        return jumpEngine;
    }
}
