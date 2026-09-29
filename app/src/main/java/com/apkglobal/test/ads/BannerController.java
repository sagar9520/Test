package com.apkglobal.test.ads;

import android.app.Activity;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.MainThread;

import com.apkglobal.test.R;
import com.applovin.mediation.MaxAd;
import com.applovin.mediation.MaxAdFormat;
import com.applovin.mediation.MaxAdViewAdListener;
import com.applovin.mediation.MaxAdViewConfiguration;
import com.applovin.mediation.MaxError;
import com.applovin.mediation.ads.MaxAdView;
import com.applovin.sdk.AppLovinSdkUtils;

import java.lang.ref.WeakReference;

/**
 * Anchored adaptive banner at the very bottom of a screen (outside the scroll view, above the nav bar).
 *
 * <p>Why only here: a banner squeezed between buttons (old layout) gets accidental clicks; networks detect
 * them, discount the whole app's bids and may add "confirm click" friction. An anchored adaptive banner is
 * full width (more demand competes for it) and never sits under the user's thumb.
 */
public final class BannerController {

    private static final String TAG = AdsManager.TAG;

    /** Stateless on purpose (no Activity captured): MaxAdView auto-refreshes and retries by itself. */
    private static final MaxAdViewAdListener LISTENER = new MaxAdViewAdListener() {
        @Override
        public void onAdLoaded(MaxAd ad) {
        }

        @Override
        public void onAdLoadFailed(String adUnitId, MaxError error) {
            // Do NOT call loadAd() here: MaxAdView already retries/refreshes on its own.
            Log.d(TAG, "Banner load failed: " + (error != null ? error.getCode() + " " + error.getMessage() : ""));
        }

        @Override
        public void onAdDisplayed(MaxAd ad) {
        }

        @Override
        public void onAdHidden(MaxAd ad) {
        }

        @Override
        public void onAdClicked(MaxAd ad) {
            AdsManager.get().onAdClicked();
        }

        @Override
        public void onAdDisplayFailed(MaxAd ad, MaxError error) {
        }

        @Override
        public void onAdExpanded(MaxAd ad) {
        }

        @Override
        public void onAdCollapsed(MaxAd ad) {
        }
    };

    private BannerController() {
    }

    /**
     * Attaches an anchored adaptive banner to {@code container} (R.id.banner_container). Call from onCreate;
     * then {@link BannerHandle#pause()} in onStop, {@link BannerHandle#resume()} in onStart and
     * {@link BannerHandle#destroy()} in onDestroy. If banners are off or not configured, the container is hidden.
     */
    @MainThread
    public static BannerHandle attachAnchored(Activity activity, ViewGroup container, String placement) {
        if (container == null) return BannerHandle.noop();
        AdsManager manager = AdsManager.get();
        if (activity == null || activity.isFinishing() || activity.isDestroyed()
                || !isEnabledFor(placement) || !manager.adsAvailable()
                || !AdUnits.isConfigured(AdUnits.BANNER)) {
            container.setVisibility(View.GONE);
            return BannerHandle.noop();
        }

        final MaxAdView adView;
        final int heightPx;
        try {
            MaxAdViewConfiguration config = MaxAdViewConfiguration.builder()
                    .setAdaptiveType(MaxAdViewConfiguration.AdaptiveType.ANCHORED)
                    .build();
            adView = new MaxAdView(AdUnits.clean(AdUnits.BANNER), config);
            // MAX picks BANNER on phones and LEADER on tablets; size the view for the format it chose.
            MaxAdFormat format = adView.getAdFormat() != null ? adView.getAdFormat() : MaxAdFormat.BANNER;
            heightPx = AppLovinSdkUtils.dpToPx(activity, format.getAdaptiveSize(activity).getHeight());
        } catch (RuntimeException e) {
            Log.e(TAG, "Cannot create banner", e);
            container.setVisibility(View.GONE);
            return BannerHandle.noop();
        }

        // AppLovin: banners need an explicit background to be fully functional.
        adView.setBackgroundColor(activity.getColor(R.color.xr_surface));
        adView.setPlacement(placement);
        // Lets stopAutoRefresh() work even before the first ad has loaded (e.g. screen left quickly).
        adView.setExtraParameter("allow_pause_auto_refresh_immediately", "true");
        adView.setRevenueListener(RevenueTracker.LISTENER);
        adView.setListener(LISTENER);

        container.removeAllViews();
        // Fixed height from the start: the space is reserved, so content never jumps when the ad arrives.
        container.addView(adView, new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, heightPx));
        container.setVisibility(View.VISIBLE);

        BannerHandle handle = new BannerHandle(container, adView);
        // Load only after SDK init (earlier requests reach AdMob/Meta uninitialised and earn less).
        // The queued task holds the handle weakly so a closed screen is never kept alive by the queue.
        final WeakReference<BannerHandle> ref = new WeakReference<>(handle);
        Runnable loadTask = () -> {
            BannerHandle h = ref.get();
            if (h != null) h.loadWhenVisible();
        };
        handle.setLoadTask(loadTask);
        manager.runWhenSdkReady(loadTask);
        return handle;
    }

    private static boolean isEnabledFor(String placement) {
        if (placement == null) return true;
        switch (placement) {
            case AdUnits.PL_HOME_BANNER:
                return AdConfig.BANNER_ON_HOME;
            case AdUnits.PL_QUESTION_BANNER:
                return AdConfig.BANNER_ON_QUESTION;
            case AdUnits.PL_CHOICE_BANNER:
                return AdConfig.BANNER_ON_CHOICE;
            default:
                return true;
        }
    }
}
