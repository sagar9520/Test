package com.apkglobal.test.ads;

import android.util.Log;
import android.view.ViewGroup;

import com.applovin.mediation.ads.MaxAdView;

/**
 * Lifecycle handle for one banner. pause() in onStop, resume() in onStart, destroy() in onDestroy.
 * All methods are safe on a no-op handle and after destroy().
 */
public final class BannerHandle {

    private ViewGroup container;
    private MaxAdView adView;
    private Runnable loadTask;
    private boolean destroyed;
    private boolean paused;
    private boolean loadRequested;
    private boolean loadDeferred;

    BannerHandle(ViewGroup container, MaxAdView adView) {
        this.container = container;
        this.adView = adView;
    }

    static BannerHandle noop() {
        return new BannerHandle(null, null);
    }

    void setLoadTask(Runnable loadTask) {
        this.loadTask = loadTask;
    }

    /** SDK is ready. Load now, or on resume() if the screen is currently in the background. */
    void loadWhenVisible() {
        if (destroyed || adView == null || loadRequested) return;
        if (paused) {
            loadDeferred = true; // no banner request while nobody can see it
            return;
        }
        loadRequested = true;
        try {
            adView.loadAd();
        } catch (RuntimeException e) {
            Log.w(AdsManager.TAG, "Banner loadAd failed", e);
        }
    }

    /** Stops auto-refresh (screen not visible: refreshing would only produce unviewable impressions). */
    public void pause() {
        if (destroyed || adView == null || paused) return;
        paused = true;
        if (loadRequested) adView.stopAutoRefresh();
    }

    public void resume() {
        if (destroyed || adView == null || !paused) return;
        paused = false;
        if (loadDeferred) {
            loadDeferred = false;
            loadWhenVisible();
        } else if (loadRequested) {
            adView.startAutoRefresh();
        }
    }

    public void destroy() {
        if (destroyed) return;
        destroyed = true;
        if (loadTask != null) {
            AdsManager.get().cancelWhenSdkReady(loadTask);
            loadTask = null;
        }
        if (adView != null) {
            if (container != null) container.removeView(adView);
            try {
                adView.destroy();
            } catch (RuntimeException e) {
                Log.w(AdsManager.TAG, "Banner destroy failed", e);
            }
        }
        adView = null;
        container = null;
    }
}
