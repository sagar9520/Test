package com.apkglobal.test.ads;

import android.app.Activity;
import android.os.Build;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowInsets;
import android.view.WindowMetrics;
import android.widget.FrameLayout;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.DefaultLifecycleObserver;
import androidx.lifecycle.LifecycleOwner;

import com.apkglobal.test.R;
import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdSize;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.LoadAdError;

/**
 * Loads an anchored adaptive banner into a slot (view_banner_ad_slot.xml).
 * Every banner on a screen uses the same {@link AdSize}, and the slot height is fixed to it
 * (plus the slot's frame padding) before loading, so the in-list banners match the bottom banner
 * exactly and nothing jumps when the ad arrives.
 */
public final class BannerAdController implements DefaultLifecycleObserver {

    private final AdView adView;
    private boolean loaded;
    private boolean destroyed;

    private BannerAdController(AppCompatActivity activity, FrameLayout slot, String adUnitId,
                               AdSize adSize, Runnable onNoAd) {
        ViewGroup.LayoutParams params = slot.getLayoutParams();
        params.height = adSize.getHeightInPixels(activity) + slot.getPaddingTop() + slot.getPaddingBottom();
        slot.setLayoutParams(params);

        adView = new AdView(activity);
        adView.setAdUnitId(adUnitId);
        adView.setAdSize(adSize);
        adView.setAdListener(new AdListener() {
            @Override
            public void onAdLoaded() {
                if (destroyed) {
                    return;
                }
                loaded = true;
                View placeholder = slot.findViewById(R.id.banner_placeholder);
                if (placeholder != null) {
                    placeholder.setVisibility(View.GONE);
                }
            }

            @Override
            public void onAdFailedToLoad(@NonNull LoadAdError loadAdError) {
                // A failed refresh keeps the previous ad; only give up the slot if nothing was ever shown.
                if (!destroyed && !loaded) {
                    onNoAd.run();
                }
            }
        });
        slot.addView(adView, new FrameLayout.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT, Gravity.CENTER));
    }

    /**
     * Starts loading immediately; the controller follows the activity lifecycle by itself.
     *
     * @param onNoAd called on the main thread if no ad could be loaded for this slot
     */
    public static BannerAdController attach(AppCompatActivity activity, FrameLayout slot,
                                            String adUnitId, AdSize adSize, Runnable onNoAd) {
        BannerAdController controller = new BannerAdController(activity, slot, adUnitId, adSize, onNoAd);
        activity.getLifecycle().addObserver(controller);
        controller.adView.loadAd(new AdRequest.Builder().build());
        return controller;
    }

    /**
     * One adaptive size for every banner on the screen: the window width (minus side system bars /
     * cutouts) minus the horizontal space around the ad, i.e. the inner width of the banner slots.
     * Works before layout, so the slot height can be fixed up front.
     */
    public static AdSize adaptiveSize(Activity activity, int horizontalInsetPx) {
        int windowWidthPx;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            WindowMetrics metrics = activity.getWindowManager().getCurrentWindowMetrics();
            android.graphics.Insets insets = metrics.getWindowInsets().getInsetsIgnoringVisibility(
                    WindowInsets.Type.systemBars() | WindowInsets.Type.displayCutout());
            windowWidthPx = metrics.getBounds().width() - insets.left - insets.right;
        } else {
            windowWidthPx = activity.getResources().getDisplayMetrics().widthPixels;
        }
        float density = activity.getResources().getDisplayMetrics().density;
        int widthDp = (int) ((windowWidthPx - 2 * horizontalInsetPx) / density);
        return AdSize.getCurrentOrientationAnchoredAdaptiveBannerAdSize(activity, widthDp);
    }

    @Override
    public void onResume(@NonNull LifecycleOwner owner) {
        adView.resume();
    }

    @Override
    public void onPause(@NonNull LifecycleOwner owner) {
        adView.pause();
    }

    @Override
    public void onDestroy(@NonNull LifecycleOwner owner) {
        destroyed = true;
        adView.destroy();
        owner.getLifecycle().removeObserver(this);
    }
}
