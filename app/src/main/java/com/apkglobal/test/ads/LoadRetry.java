package com.apkglobal.test.ads;

import android.os.Handler;

import java.util.concurrent.TimeUnit;

/**
 * Exponential backoff for ad reloads: 2, 4, 8 ... 64 s (AppLovin's recommended 2^min(6, attempt) s).
 *
 * <p>Why not retry at once: after a no-fill, instant retries just produce more no-fills, drain battery/data and
 * lower the unit's fill-rate stats. Backing off lets demand recover. Main thread only.
 */
final class LoadRetry {

    private static final int MAX_EXPONENT = 6;

    private final Handler handler;
    private final Runnable task;
    private final Runnable fire = new Runnable() {
        @Override
        public void run() {
            pending = false;
            task.run();
        }
    };

    private int attempt;
    private boolean pending;

    LoadRetry(Handler handler, Runnable task) {
        this.handler = handler;
        this.task = task;
    }

    /** A load succeeded: next failure starts again at 2 s. */
    void onSuccess() {
        attempt = 0;
        cancel();
    }

    /** A load failed: run {@code task} again after the next backoff delay. */
    void scheduleRetry() {
        if (attempt < MAX_EXPONENT) attempt++;
        handler.removeCallbacks(fire);
        handler.postDelayed(fire, delayMs(attempt));
        pending = true;
    }

    /** True while a retry is scheduled; callers must not load early (that would defeat the backoff). */
    boolean isPending() {
        return pending;
    }

    void cancel() {
        handler.removeCallbacks(fire);
        pending = false;
    }

    static long delayMs(int attempt) {
        int exp = Math.max(0, Math.min(MAX_EXPONENT, attempt));
        return TimeUnit.SECONDS.toMillis(1L << exp);
    }
}
