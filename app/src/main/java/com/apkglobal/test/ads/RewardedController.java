package com.apkglobal.test.ads;

import android.app.Activity;
import android.os.Handler;
import android.util.Log;

import androidx.annotation.MainThread;

import com.applovin.mediation.MaxAd;
import com.applovin.mediation.MaxError;
import com.applovin.mediation.MaxReward;
import com.applovin.mediation.MaxRewardedAdListener;
import com.applovin.mediation.ads.MaxRewardedAd;

/**
 * Opt-in rewarded ads ("Watch an ad to unlock detailed analysis / HD scan").
 *
 * <p>Rewarded is the best-paid format in India and the user chooses to watch it, so it earns without hurting
 * the experience. It never blocks the user: if no ad is available the reward is still granted
 * ({@link AdConfig#GRANT_REWARD_IF_NO_AD}).
 */
public final class RewardedController implements MaxRewardedAdListener {

    /** Called exactly once, on the main thread. */
    public interface Callback {
        void onResult(boolean rewarded);
    }

    private static final String TAG = AdsManager.TAG;

    /** MAX does not guarantee onUserRewarded arrives before onAdHidden; wait this long for it. */
    static final long REWARD_GRACE_MS = 300L;

    private final AdsManager manager;
    private final Handler main;
    private final LoadRetry retry;
    private final Runnable watchdog = this::onDisplayWatchdog;
    private final Runnable resolveAfterHidden = () -> resolve(this.earned);

    private MaxRewardedAd ad;
    private Callback pending;
    private boolean earned;
    private boolean displayed;

    RewardedController(AdsManager manager, Handler main) {
        this.manager = manager;
        this.main = main;
        this.retry = new LoadRetry(main, this::load);
    }

    public boolean isReady() {
        return manager.canLoad(AdUnits.REWARDED) && ad != null && ad.isReady();
    }

    /**
     * Shows a rewarded ad. {@code cb} gets true if the user earned the reward; if no ad can be shown it gets
     * {@link AdConfig#GRANT_REWARD_IF_NO_AD} right away. A second call while one is in progress is ignored.
     */
    @MainThread
    public void show(Activity activity, String placement, Callback cb) {
        if (!AdsManager.isMainThread()) {
            main.post(() -> show(activity, placement, cb));
            return;
        }
        final Callback callback = cb != null ? cb : rewarded -> { };
        if (pending != null) {
            Log.d(TAG, "Rewarded already in progress; duplicate tap ignored");
            return;
        }
        if (!isReady() || !AdsManager.canPresentOn(activity) || manager.isFullscreenShowing()) {
            load();
            callback.onResult(AdConfig.GRANT_REWARD_IF_NO_AD);
            return;
        }

        pending = callback;
        earned = false;
        displayed = false;
        manager.setFullscreenShowing(true);
        main.postDelayed(watchdog, InterstitialController.DISPLAY_WATCHDOG_MS);
        try {
            ad.showAd(placement, activity);
        } catch (RuntimeException e) {
            Log.w(TAG, "Rewarded showAd failed", e);
            failShow();
        }
    }

    /** Loads the next rewarded ad unless one is ready/loading or a backoff retry is pending. */
    void load() {
        if (!manager.canLoad(AdUnits.REWARDED) || retry.isPending()) return;
        MaxRewardedAd a = ensureAd();
        if (a == null || a.isReady() || a.isLoading()) return;
        a.loadAd();
    }

    private MaxRewardedAd ensureAd() {
        if (ad == null) {
            try {
                // Process-wide singleton per ad unit. Never destroy() it: that removes it from MAX's map and a
                // later getInstance() would return a new object without our listener.
                ad = MaxRewardedAd.getInstance(AdUnits.clean(AdUnits.REWARDED));
                ad.setListener(this);
                ad.setRevenueListener(RevenueTracker.LISTENER);
            } catch (RuntimeException e) {
                Log.e(TAG, "Cannot create MaxRewardedAd", e);
                ad = null;
            }
        }
        return ad;
    }

    private void resolve(boolean result) {
        main.removeCallbacks(resolveAfterHidden);
        Callback cb = pending;
        pending = null;
        if (cb != null) cb.onResult(result);
    }

    private void failShow() {
        main.removeCallbacks(watchdog);
        manager.clearFullscreenShowing();
        resolve(AdConfig.GRANT_REWARD_IF_NO_AD);
        load();
    }

    private void onDisplayWatchdog() {
        if (pending == null || displayed) return;
        Log.w(TAG, "Rewarded did not appear in time; continuing without it");
        manager.clearFullscreenShowing();
        resolve(AdConfig.GRANT_REWARD_IF_NO_AD);
    }

    // ---- MaxRewardedAdListener (main thread) ----

    @Override
    public void onUserRewarded(MaxAd maxAd, MaxReward reward) {
        earned = true;
    }

    @Override
    public void onAdLoaded(MaxAd maxAd) {
        retry.onSuccess();
    }

    @Override
    public void onAdLoadFailed(String adUnitId, MaxError error) {
        Log.d(TAG, "Rewarded load failed: " + (error != null ? error.getCode() + " " + error.getMessage() : ""));
        retry.scheduleRetry();
    }

    @Override
    public void onAdDisplayed(MaxAd maxAd) {
        main.removeCallbacks(watchdog);
        displayed = true;
        manager.setFullscreenShowing(true);
    }

    @Override
    public void onAdHidden(MaxAd maxAd) {
        main.removeCallbacks(watchdog);
        manager.setFullscreenShowing(false);
        main.removeCallbacks(resolveAfterHidden);
        main.postDelayed(resolveAfterHidden, REWARD_GRACE_MS);
        load();
    }

    @Override
    public void onAdClicked(MaxAd maxAd) {
        manager.onAdClicked();
    }

    @Override
    public void onAdDisplayFailed(MaxAd maxAd, MaxError error) {
        Log.w(TAG, "Rewarded display failed: " + (error != null ? error.getCode() + " " + error.getMessage() : ""));
        failShow();
    }
}
