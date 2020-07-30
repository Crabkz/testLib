package com.krab.thread;

import androidx.lifecycle.Lifecycle;
import androidx.lifecycle.LifecycleObserver;
import androidx.lifecycle.OnLifecycleEvent;

import java.util.ArrayList;
import java.util.List;

/**
 * @author xkz
 * @date 2020/7/6 15:30
 */
public class JumpEngine {
    private JumpLifecycleObserver lifecycleObserver;
    public List<Package> packages = new ArrayList<>();
    private boolean cut;
    private Lifecycle lifecycle;

    public static ThreadJump<Object> instance() {
        JumpEngine jumpEngine = new JumpEngine();
        return ThreadJumpImpl.instance(jumpEngine);
    }

    public void emit() {
        emit(null);
    }

    private void emit(Object value) {
        if (packages.size() > 0) {
            if (cut) {
                packages.clear();
                jumpCompleted();
                return;
            }
            Package aPackage = packages.get(0);
            packages.remove(aPackage);
            switch (aPackage.jumpTo) {
                case ThreadJump.CURRENT:
                    witchType(aPackage.task, value);
                    break;
                case ThreadJump.MAIN:
                    ThreadUtils.runOnUiThread(() -> witchType(aPackage.task, value));
                    break;
                case ThreadJump.SUB:
                    ThreadUtils.runOnSubThread(() -> witchType(aPackage.task, value));
                    break;
            }
        } else {
            jumpCompleted();
        }
    }

    private void jumpCompleted() {
        if (lifecycle != null) {
            lifecycle.removeObserver(lifecycleObserver);
            lifecycle = null;
        }
    }

    private void witchType(Object task, Object value) {
        Object result = null;
        if (task instanceof ThreadJump.ConnectedCallable) {
            result = ((ThreadJump.ConnectedCallable) task).call(value);
        } else if (task instanceof ThreadJump.Callable) {
            result = ((ThreadJump.Callable) task).call();
        } else if (task instanceof ThreadJump.Runnable) {
            ((ThreadJump.Runnable) task).run();
        } else if (task instanceof ThreadJump.ConnectedRunnable) {
            ((ThreadJump.ConnectedRunnable) task).run(value);
        } else if (task instanceof ThreadJump.SConnectedCallable) {
            result = ((ThreadJump.SConnectedCallable) task).call(this, value);
        } else if (task instanceof ThreadJump.SCallable) {
            result = ((ThreadJump.SCallable) task).call(this);
        } else if (task instanceof ThreadJump.SRunnable) {
            ((ThreadJump.SRunnable) task).run(this);
        } else if (task instanceof ThreadJump.SConnectedRunnable) {
            ((ThreadJump.SConnectedRunnable) task).run(this, value);
        }
        emit(result);
    }

    public void cut() {
        cut = true;
    }

    public static class Package {
        public int jumpTo;
        public Object task;
    }

    public JumpEngine setLifecycleObserver(Lifecycle lifecycle) {
        if (this.lifecycle == null) {
            lifecycleObserver = new JumpLifecycleObserver();
            this.lifecycle = lifecycle;
            lifecycle.addObserver(lifecycleObserver);
        }
        return this;
    }

    private class JumpLifecycleObserver implements LifecycleObserver {
        @OnLifecycleEvent(Lifecycle.Event.ON_DESTROY)
        public void onDestroy() {
            cut();
        }
    }
}
