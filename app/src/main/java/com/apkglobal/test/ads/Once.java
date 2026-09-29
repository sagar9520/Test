package com.apkglobal.test.ads;

import java.util.concurrent.atomic.AtomicBoolean;

/** Wraps a continuation so it runs at most once, whichever ad callback (or watchdog) gets there first. */
final class Once implements Runnable {

    private final AtomicBoolean done = new AtomicBoolean(false);
    private final Runnable delegate;

    private Once(Runnable delegate) {
        this.delegate = delegate;
    }

    static Once of(Runnable delegate) {
        return new Once(delegate);
    }

    @Override
    public void run() {
        if (done.compareAndSet(false, true) && delegate != null) {
            delegate.run();
        }
    }
}
