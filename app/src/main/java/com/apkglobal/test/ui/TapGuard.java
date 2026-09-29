package com.apkglobal.test.ui;

import android.os.Handler;
import android.os.Looper;

import androidx.annotation.NonNull;

import com.apkglobal.test.ads.AdsManager;

/**
 * One-action-at-a-time guard for buttons that may show a full-screen ad before continuing.
 *
 * <p>Double taps are the main source of accidental ad clicks and of duplicate screens. A tap locks the
 * screen until the action finishes. If the action opened another screen, the lock is released only when
 * the user comes back to this one (onResume). As a safety net, a lock that is still held while this screen
 * is visible and no full-screen ad is showing is released after {@link #STUCK_AFTER_MS}.
 */
public final class TapGuard {

    public interface Listener {
        /** The screen accepts taps again: re-enable the views. */
        void onUnlocked();
    }

    private static final int IDLE = 0;
    private static final int BUSY = 1;
    private static final int AWAY = 2;

    static final long STUCK_AFTER_MS = 8_000L;
    private static final long RECHECK_MS = 2_000L;

    private final Handler handler = new Handler(Looper.getMainLooper());
    private final Runnable stuckCheck = this::checkStuck;
    private final Listener listener;
    private int state = IDLE;
    private boolean resumed;

    public TapGuard(@NonNull Listener listener) {
        this.listener = listener;
    }

    /** @return true if the tap may proceed (and the guard is now locked); false while another action runs. */
    public boolean tryLock() {
        if (state != IDLE) return false;
        state = BUSY;
        handler.removeCallbacks(stuckCheck);
        handler.postDelayed(stuckCheck, STUCK_AFTER_MS);
        return true;
    }

    /** The action finished by opening another screen: unlock when this screen is resumed again. */
    public void doneAway() {
        handler.removeCallbacks(stuckCheck);
        state = AWAY;
    }

    /** The action finished on this screen: unlock now. */
    public void doneHere() {
        handler.removeCallbacks(stuckCheck);
        unlock();
    }

    public void onResume() {
        resumed = true;
        if (state == AWAY) unlock();
    }

    public void onPause() {
        resumed = false;
    }

    public void onDestroy() {
        handler.removeCallbacks(stuckCheck);
    }

    private void unlock() {
        state = IDLE;
        listener.onUnlocked();
    }

    private void checkStuck() {
        if (state != BUSY) return;
        if (resumed && !AdsManager.get().isFullscreenShowing()) {
            unlock();
        } else {
            handler.postDelayed(stuckCheck, RECHECK_MS);
        }
    }
}
