package com.apkglobal.test.ads;

import android.app.Activity;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.MainThread;

import com.apkglobal.test.R;
import com.applovin.mediation.MaxAd;
import com.applovin.mediation.MaxError;
import com.applovin.mediation.nativeAds.MaxNativeAd;
import com.applovin.mediation.nativeAds.MaxNativeAdListener;
import com.applovin.mediation.nativeAds.MaxNativeAdLoader;
import com.applovin.mediation.nativeAds.MaxNativeAdView;
import com.applovin.mediation.nativeAds.MaxNativeAdViewBinder;

import java.util.ArrayDeque;
import java.util.Iterator;

/**
 * Native ads with a small preloaded pool, rendered into the {@code view_native_ad_slot} of each screen.
 *
 * <p>Why a pool: a native that is already loaded appears together with the screen, is fully visible while the
 * user reads the question, and counts as a viewable impression. Natives that pop in late (or half off-screen,
 * as in the old layout) are exactly what drags eCPM down.
 *
 * <p>Placement reporting is approximate: MAX stamps the loader's placement on an ad when it LOADS (render does
 * not re-stamp), so each load is tagged with the placement of the waiting slot or of the last bound screen.
 * Use separate native ad units per screen only if exact per-screen native eCPM is ever needed.
 */
public final class NativeAdController {

    private static final String TAG = AdsManager.TAG;
    private static final String PL_PRELOAD_FALLBACK = "native_preload";

    private final AdsManager manager;
    private final Handler main;
    private final LoadRetry retry;
    private final ArrayDeque<MaxAd> pool = new ArrayDeque<>();
    private final ArrayDeque<NativeAdSlot> waiting = new ArrayDeque<>();

    private MaxNativeAdLoader loader;
    private MaxNativeAdViewBinder binder;
    private boolean loading;
    private String lastPlacement;

    private final MaxNativeAdListener listener = new MaxNativeAdListener() {
        @Override
        public void onNativeAdLoaded(MaxNativeAdView nativeAdView, MaxAd ad) {
            loading = false;
            retry.onSuccess();
            if (ad != null) {
                pool.addLast(ad);
                serveWaiting();
            }
            preload();
        }

        @Override
        public void onNativeAdLoadFailed(String adUnitId, MaxError error) {
            loading = false;
            Log.d(TAG, "Native load failed: " + (error != null ? error.getCode() + " " + error.getMessage() : ""));
            retry.scheduleRetry();
        }

        @Override
        public void onNativeAdClicked(MaxAd ad) {
            manager.onAdClicked();
        }

        @Override
        public void onNativeAdExpired(MaxAd ad) {
            // MAX does not reload expired natives. Only pooled ones are dropped; one already on screen stays.
            if (pool.remove(ad)) {
                destroyAd(ad);
                preload();
            }
        }
    };

    NativeAdController(AdsManager manager, Handler main) {
        this.manager = manager;
        this.main = main;
        this.retry = new LoadRetry(main, this::preload);
    }

    /**
     * Binds a native slot. {@code slotRoot} is the inflated {@code view_native_ad_slot}. The slot shows its
     * placeholder, gets the next ready ad (now or as soon as one loads) and collapses if none arrives within
     * {@link AdConfig#NATIVE_SLOT_TIMEOUT_MS}. Call {@link NativeAdSlot#release()} in onDestroy.
     */
    @MainThread
    public NativeAdSlot bind(Activity activity, View slotRoot, String placement) {
        if (slotRoot == null) return NativeAdSlot.noop();
        ViewGroup container = slotRoot.findViewById(R.id.native_ad_container);
        if (activity == null || container == null || !isEnabledFor(placement)
                || !manager.adsAvailable() || !AdUnits.isConfigured(AdUnits.NATIVE_MEDIUM)) {
            slotRoot.setVisibility(View.GONE);
            return NativeAdSlot.noop();
        }

        NativeAdSlot slot = new NativeAdSlot(this, activity, slotRoot, placement);
        slot.showPlaceholder();
        if (placement != null) lastPlacement = placement;
        if (!tryServe(slot)) {
            waiting.addLast(slot);
            main.postDelayed(slot.timeoutTask, AdConfig.NATIVE_SLOT_TIMEOUT_MS);
            preload();
        }
        return slot;
    }

    /** Keeps up to {@link AdConfig#NATIVE_PRELOAD_COUNT} natives ready. One request in flight at a time. */
    void preload() {
        if (!manager.canLoad(AdUnits.NATIVE_MEDIUM) || !anyNativeEnabled()) return;
        if (loading || retry.isPending()) return;
        purgeExpired();
        int target = Math.max(0, Math.min(AdConfig.NATIVE_PRELOAD_MAX, AdConfig.NATIVE_PRELOAD_COUNT));
        if (pool.size() >= target && waiting.isEmpty()) return;

        MaxNativeAdLoader l = ensureLoader();
        if (l == null) return;
        NativeAdSlot first = waiting.peekFirst();
        String placement = first != null && first.placement != null ? first.placement
                : (lastPlacement != null ? lastPlacement : PL_PRELOAD_FALLBACK);
        l.setPlacement(placement); // stamped on the ad at load time (see class doc)
        loading = true;
        try {
            l.loadAd();
        } catch (RuntimeException e) {
            Log.w(TAG, "Native loadAd failed", e);
            loading = false;
            retry.scheduleRetry();
        }
    }

    private MaxNativeAdLoader ensureLoader() {
        if (loader == null) {
            try {
                loader = new MaxNativeAdLoader(AdUnits.clean(AdUnits.NATIVE_MEDIUM));
                loader.setNativeAdListener(listener);
                loader.setRevenueListener(RevenueTracker.LISTENER);
            } catch (RuntimeException e) {
                Log.e(TAG, "Cannot create MaxNativeAdLoader", e);
                loader = null;
            }
        }
        return loader;
    }

    private MaxNativeAdViewBinder binder() {
        if (binder == null) {
            binder = new MaxNativeAdViewBinder.Builder(R.layout.view_native_ad_medium)
                    .setTitleTextViewId(R.id.native_title)
                    .setAdvertiserTextViewId(R.id.native_advertiser)
                    .setBodyTextViewId(R.id.native_body)
                    .setIconImageViewId(R.id.native_icon)
                    .setMediaContentViewGroupId(R.id.native_media)
                    .setOptionsContentViewGroupId(R.id.native_options)
                    .setStarRatingContentViewGroupId(R.id.native_star_rating)
                    .setCallToActionButtonId(R.id.native_cta)
                    .build();
        }
        return binder;
    }

    /**
     * Renders the next usable pooled ad into {@code slot}.
     *
     * @return true if the slot needs nothing more (filled, or its screen is gone); false if the pool ran dry.
     */
    private boolean tryServe(NativeAdSlot slot) {
        Activity activity = slot.activity();
        if (activity == null || activity.isFinishing() || activity.isDestroyed() || !slot.isAlive()) {
            return true;
        }
        MaxAd ad;
        while ((ad = pool.pollFirst()) != null) {
            MaxNativeAd nativeAd = ad.getNativeAd();
            if (nativeAd == null || nativeAd.isExpired()) {
                destroyAd(ad);
                continue;
            }
            boolean rendered;
            MaxNativeAdView view = null;
            try {
                view = new MaxNativeAdView(binder(), activity);
                rendered = loader != null && loader.render(view, ad);
            } catch (RuntimeException e) {
                Log.w(TAG, "Native render failed", e);
                rendered = false;
            }
            if (!rendered) {
                destroyAd(ad);
                continue;
            }
            slot.showAd(view, ad);
            preload(); // refill the pool for the next screen
            return true;
        }
        return false;
    }

    private void serveWaiting() {
        while (!pool.isEmpty() && !waiting.isEmpty()) {
            NativeAdSlot slot = waiting.peekFirst();
            if (!tryServe(slot)) return; // pool ran dry
            waiting.pollFirst();
            main.removeCallbacks(slot.timeoutTask);
        }
    }

    private void purgeExpired() {
        for (Iterator<MaxAd> it = pool.iterator(); it.hasNext(); ) {
            MaxAd ad = it.next();
            MaxNativeAd nativeAd = ad.getNativeAd();
            if (nativeAd == null || nativeAd.isExpired()) {
                it.remove();
                destroyAd(ad);
            }
        }
    }

    void onSlotTimeout(NativeAdSlot slot) {
        if (waiting.remove(slot)) {
            slot.collapse();
        }
    }

    void onSlotReleased(NativeAdSlot slot) {
        waiting.remove(slot);
        main.removeCallbacks(slot.timeoutTask);
    }

    void destroyAd(MaxAd ad) {
        if (ad == null || loader == null) return;
        try {
            loader.destroy(ad);
        } catch (RuntimeException e) {
            Log.w(TAG, "Native destroy failed", e);
        }
    }

    private static boolean anyNativeEnabled() {
        return AdConfig.NATIVE_ON_HOME || AdConfig.NATIVE_ON_QUESTION
                || AdConfig.NATIVE_ON_CHOICE || AdConfig.NATIVE_ON_READY;
    }

    private static boolean isEnabledFor(String placement) {
        if (placement == null) return true;
        switch (placement) {
            case AdUnits.PL_HOME_NATIVE:
                return AdConfig.NATIVE_ON_HOME;
            case AdUnits.PL_QUESTION_NATIVE:
                return AdConfig.NATIVE_ON_QUESTION;
            case AdUnits.PL_CHOICE_NATIVE:
                return AdConfig.NATIVE_ON_CHOICE;
            case AdUnits.PL_READY_NATIVE:
                return AdConfig.NATIVE_ON_READY;
            default:
                return true;
        }
    }
}
