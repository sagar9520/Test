package com.apkglobal.test.ads;

import android.app.Activity;
import android.os.Handler;
import android.util.Log;

import androidx.annotation.MainThread;

import com.applovin.mediation.MaxAd;
import com.applovin.mediation.MaxAdListener;
import com.applovin.mediation.MaxError;
import com.applovin.mediation.ads.MaxInterstitialAd;

/**
 * Paced interstitials at natural breaks (step -> next step, "Start scan").
 *
 * <p>One app-scoped {@link MaxInterstitialAd}, always preloaded, so a tap never waits for a network request.
 */
public final class InterstitialController implements MaxAdListener {

    private static final String TAG = AdsManager.TAG;

    /** If the ad has not appeared by then, continue without it so the user is never stuck on a frozen screen. */
    static final long DISPLAY_WATCHDOG_MS = 5_000L;

    private final AdsManager manager;
    private final Handler main;
    private final LoadRetry retry;
    private final Runnable watchdog = this::onDisplayWatchdog;

    private MaxInterstitialAd ad;
    private boolean showInProgress;
    private boolean displayed;
    private Runnable pendingNext;

    InterstitialController(AdsManager manager, Handler main) {
        this.manager = manager;
        this.main = main;
        this.retry = new LoadRetry(main, this::load);
    }

    /**
     * Call on EVERY navigation tap. Shows an interstitial only if the pacing rules allow it and one is ready;
     * {@code next} runs exactly once: after the ad closes, or immediately if nothing is shown.
     * Taps that arrive while an ad is being shown are ignored (double-tap guard).
     */
    @MainThread
    public void onNavigation(Activity activity, String placement, Runnable next) {
        request(activity, placement, next, false);
    }

    /**
     * Like {@link #onNavigation} but ignores the every-N-taps counter (for a big moment like "Start scan").
     * Minimum interval, fullscreen cooldown, session cap and skip-first still apply.
     */
    @MainThread
    public void onMajorAction(Activity activity, String placement, Runnable next) {
        request(activity, placement, next, true);
    }

    private void request(Activity activity, String placement, Runnable next, boolean major) {
        if (!AdsManager.isMainThread()) {
            main.post(() -> request(activity, placement, next, major));
            return;
        }
        if (showInProgress) {
            Log.d(TAG, "Interstitial already in progress; duplicate tap ignored");
            return;
        }

        final Runnable once = Once.of(next);
        FrequencyCapper capper = manager.capper();
        capper.recordAction();

        if (!manager.canLoad(AdUnits.INTERSTITIAL)
                || !AdsManager.canPresentOn(activity)
                || manager.isFullscreenShowing()) {
            once.run();
            return;
        }

        FrequencyCapper.Rules rules = AdConfig.interstitialRules();
        if (major) {
            rules = new FrequencyCapper.Rules(rules.skipFirstActions, 0, rules.minIntervalMs,
                    rules.maxPerSession, rules.fullscreenCooldownMs);
        }
        if (!capper.canShowInterstitial(rules)) {
            once.run();
            return;
        }

        if (ad == null || !ad.isReady()) {
            // Never make the user wait for a load: continue now, the next eligible tap gets the ad.
            once.run();
            load();
            return;
        }

        showInProgress = true;
        displayed = false;
        pendingNext = once;
        manager.setFullscreenShowing(true);
        main.postDelayed(watchdog, DISPLAY_WATCHDOG_MS);
        try {
            ad.showAd(placement, activity);
        } catch (RuntimeException e) {
            Log.w(TAG, "Interstitial showAd failed", e);
            finishShow(false);
            load();
        }
    }

    /** Loads the next interstitial unless one is ready/loading or a backoff retry is pending. */
    void load() {
        if (!manager.canLoad(AdUnits.INTERSTITIAL) || retry.isPending()) return;
        MaxInterstitialAd a = ensureAd();
        if (a == null || a.isReady() || a.isLoading()) return;
        a.loadAd();
    }

    private MaxInterstitialAd ensureAd() {
        if (ad == null) {
            try {
                ad = new MaxInterstitialAd(AdUnits.clean(AdUnits.INTERSTITIAL));
                ad.setListener(this);
                ad.setRevenueListener(RevenueTracker.LISTENER);
            } catch (RuntimeException e) {
                Log.e(TAG, "Cannot create MaxInterstitialAd", e);
                ad = null;
            }
        }
        return ad;
    }

    private void finishShow(boolean wasDisplayed) {
        main.removeCallbacks(watchdog);
        showInProgress = false;
        if (wasDisplayed) {
            manager.setFullscreenShowing(false);
        } else {
            manager.clearFullscreenShowing();
        }
        Runnable next = pendingNext;
        pendingNext = null;
        if (next != null) next.run();
    }

    private void onDisplayWatchdog() {
        if (!showInProgress || displayed) return;
        Log.w(TAG, "Interstitial did not appear in " + DISPLAY_WATCHDOG_MS + " ms; continuing without it");
        finishShow(false);
    }

    // ---- MaxAdListener (MAX calls these on the main thread) ----

    @Override
    public void onAdLoaded(MaxAd maxAd) {
        retry.onSuccess();
    }

    @Override
    public void onAdLoadFailed(String adUnitId, MaxError error) {
        Log.d(TAG, "Interstitial load failed: " + (error != null ? error.getCode() + " " + error.getMessage() : ""));
        retry.scheduleRetry();
    }

    @Override
    public void onAdDisplayed(MaxAd maxAd) {
        main.removeCallbacks(watchdog);
        displayed = true;
        manager.setFullscreenShowing(true); // also covers an ad that appeared after the watchdog
        manager.capper().onInterstitialShown();
    }

    @Override
    public void onAdHidden(MaxAd maxAd) {
        finishShow(true);
        load();
    }

    @Override
    public void onAdClicked(MaxAd maxAd) {
        manager.onAdClicked();
    }

    @Override
    public void onAdDisplayFailed(MaxAd maxAd, MaxError error) {
        Log.w(TAG, "Interstitial display failed: " + (error != null ? error.getCode() + " " + error.getMessage() : ""));
        finishShow(false);
        load();
    }
}
