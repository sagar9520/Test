package com.apkglobal.test.ads;

import android.app.Activity;
import android.app.Application;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.SystemClock;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.lifecycle.DefaultLifecycleObserver;
import androidx.lifecycle.LifecycleOwner;
import androidx.lifecycle.ProcessLifecycleOwner;

import com.applovin.mediation.MaxAd;
import com.applovin.mediation.MaxAdListener;
import com.applovin.mediation.MaxError;
import com.applovin.mediation.ads.MaxAppOpenAd;

/**
 * App-open ads: shown when the user returns to the app after a while, and at cold start only if one is
 * ready almost immediately. A high-eCPM fullscreen slot that does not interrupt any in-app action.
 *
 * <p>Rules (see {@link AdConfig}): never on the first-ever launch, only after enough time in background, never
 * over/just after another fullscreen ad, never when coming back from an ad the user tapped.
 */
public final class AppOpenController implements DefaultLifecycleObserver,
        Application.ActivityLifecycleCallbacks, MaxAdListener {

    private static final String TAG = AdsManager.TAG;
    private static final String PREFS = "ads_prefs";
    private static final String KEY_LAUNCH_COUNT = "launch_count";
    /** Going to background this soon after an ad click means the click sent the user away. */
    private static final long AD_CLICK_LEAVE_WINDOW_MS = 5_000L;

    private final AdsManager manager;
    private final Handler main;
    private final LoadRetry retry;
    private final Runnable watchdog = this::onDisplayWatchdog;

    private MaxAppOpenAd ad;
    private boolean attached;
    private int launchCount;
    private long processStartMs;
    private boolean coldStartPending = true;
    private long backgroundSinceMs = -1L;
    private boolean leftViaAdClick;
    private long lastAdClickMs = -1L;
    private int startedActivities;
    private boolean showing;
    private boolean displayed;

    AppOpenController(AdsManager manager, Handler main) {
        this.manager = manager;
        this.main = main;
        this.retry = new LoadRetry(main, this::load);
    }

    /** Counts this launch and registers the lifecycle observers. Called once from AdsManager.initialize. */
    public void attach(Application app) {
        if (app == null || attached) return;
        if (!AdsManager.isMainThread()) {
            main.post(() -> attach(app));
            return;
        }
        attached = true;
        processStartMs = SystemClock.elapsedRealtime();
        launchCount = incrementLaunchCount(app);
        app.registerActivityLifecycleCallbacks(this);
        ProcessLifecycleOwner.get().getLifecycle().addObserver(this);
    }

    private static int incrementLaunchCount(Context context) {
        try {
            SharedPreferences prefs = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE);
            int previous = prefs.getInt(KEY_LAUNCH_COUNT, 0);
            int count = previous < Integer.MAX_VALUE ? previous + 1 : previous;
            prefs.edit().putInt(KEY_LAUNCH_COUNT, count).apply();
            return count;
        } catch (RuntimeException e) {
            Log.w(TAG, "Cannot read launch count", e);
            return 1;
        }
    }

    private boolean isEnabled() {
        return AdConfig.APP_OPEN_ENABLED
                && launchCount >= AdConfig.APP_OPEN_MIN_LAUNCHES
                && manager.canLoad(AdUnits.APP_OPEN);
    }

    /** Loads the next app-open ad unless disabled, ready/loading, or a backoff retry is pending. */
    void load() {
        if (!isEnabled() || retry.isPending()) return;
        MaxAppOpenAd a = ensureAd();
        if (a == null || a.isReady() || a.isLoading()) return;
        a.loadAd();
    }

    private MaxAppOpenAd ensureAd() {
        if (ad == null) {
            try {
                ad = new MaxAppOpenAd(AdUnits.clean(AdUnits.APP_OPEN));
                ad.setListener(this);
                ad.setRevenueListener(RevenueTracker.LISTENER);
            } catch (RuntimeException e) {
                Log.e(TAG, "Cannot create MaxAppOpenAd", e);
                ad = null;
            }
        }
        return ad;
    }

    private void tryShow() {
        if (!isEnabled()) return;
        if (ad == null || !ad.isReady()) {
            load();
            return;
        }
        if (showing || manager.isFullscreenShowing()) return;
        if (manager.capper().isInFullscreenCooldown(AdConfig.FULLSCREEN_COOLDOWN_MS)) return;
        if (startedActivities <= 0) return; // nothing on screen for MAX to present over

        showing = true;
        displayed = false;
        manager.setFullscreenShowing(true);
        main.postDelayed(watchdog, InterstitialController.DISPLAY_WATCHDOG_MS);
        try {
            // MaxAppOpenAd has no Activity overload: MAX presents over its tracked top Activity.
            ad.showAd(AdUnits.PL_APP_OPEN);
        } catch (RuntimeException e) {
            Log.w(TAG, "App-open showAd failed", e);
            endShow(false);
            load();
        }
    }

    private void endShow(boolean wasDisplayed) {
        main.removeCallbacks(watchdog);
        showing = false;
        if (wasDisplayed) {
            manager.setFullscreenShowing(false);
        } else {
            manager.clearFullscreenShowing();
        }
    }

    private void onDisplayWatchdog() {
        if (!showing || displayed) return;
        Log.w(TAG, "App-open did not appear in time");
        endShow(false);
    }

    void onAnyAdClicked() {
        lastAdClickMs = SystemClock.elapsedRealtime();
    }

    // ---- ProcessLifecycleOwner: whole app foreground/background ----

    @Override
    public void onStart(@NonNull LifecycleOwner owner) {
        long now = SystemClock.elapsedRealtime();
        boolean returning = backgroundSinceMs >= 0;
        long backgroundMs = returning ? now - backgroundSinceMs : 0L;
        boolean fromAdClick = leftViaAdClick;
        backgroundSinceMs = -1L;
        leftViaAdClick = false;

        if (!returning) return; // cold start is handled when the first ad loads (onAdLoaded)

        if (backgroundMs >= AdConfig.SESSION_TIMEOUT_MS) {
            manager.capper().resetSession();
        }
        // Coming back from an ad the user tapped: another ad right now feels like a trap.
        if (fromAdClick) return;
        if (backgroundMs < AdConfig.APP_OPEN_MIN_BACKGROUND_MS) return;
        tryShow();
    }

    @Override
    public void onStop(@NonNull LifecycleOwner owner) {
        long now = SystemClock.elapsedRealtime();
        backgroundSinceMs = now;
        leftViaAdClick = lastAdClickMs >= 0 && now - lastAdClickMs <= AD_CLICK_LEAVE_WINDOW_MS;
        coldStartPending = false; // user left before the cold-start ad was ready
    }

    // ---- ActivityLifecycleCallbacks: is any Activity on screen? ----

    @Override
    public void onActivityStarted(@NonNull Activity activity) {
        startedActivities++;
    }

    @Override
    public void onActivityStopped(@NonNull Activity activity) {
        if (startedActivities > 0) startedActivities--;
    }

    @Override
    public void onActivityCreated(@NonNull Activity activity, Bundle savedInstanceState) {
    }

    @Override
    public void onActivityResumed(@NonNull Activity activity) {
    }

    @Override
    public void onActivityPaused(@NonNull Activity activity) {
    }

    @Override
    public void onActivitySaveInstanceState(@NonNull Activity activity, @NonNull Bundle outState) {
    }

    @Override
    public void onActivityDestroyed(@NonNull Activity activity) {
    }

    // ---- MaxAdListener (main thread) ----

    @Override
    public void onAdLoaded(MaxAd maxAd) {
        retry.onSuccess();
        if (coldStartPending) {
            coldStartPending = false;
            boolean inWindow = SystemClock.elapsedRealtime() - processStartMs <= AdConfig.APP_OPEN_COLD_START_WINDOW_MS;
            // Do not pop up once the user has already started tapping through the app.
            if (inWindow && manager.capper().actionsThisSession() == 0) {
                tryShow();
            }
        }
    }

    @Override
    public void onAdLoadFailed(String adUnitId, MaxError error) {
        Log.d(TAG, "App-open load failed: " + (error != null ? error.getCode() + " " + error.getMessage() : ""));
        coldStartPending = false;
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
        endShow(true);
        load();
    }

    @Override
    public void onAdClicked(MaxAd maxAd) {
        manager.onAdClicked();
    }

    @Override
    public void onAdDisplayFailed(MaxAd maxAd, MaxError error) {
        Log.w(TAG, "App-open display failed: " + (error != null ? error.getCode() + " " + error.getMessage() : ""));
        endShow(false);
        load();
    }
}
