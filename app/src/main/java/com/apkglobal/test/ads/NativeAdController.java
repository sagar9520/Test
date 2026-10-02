package com.apkglobal.test.ads;

import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.DefaultLifecycleObserver;
import androidx.lifecycle.LifecycleOwner;

import com.apkglobal.test.R;
import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdLoader;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.LoadAdError;
import com.google.android.gms.ads.VideoOptions;
import com.google.android.gms.ads.nativead.MediaView;
import com.google.android.gms.ads.nativead.NativeAd;
import com.google.android.gms.ads.nativead.NativeAdOptions;
import com.google.android.gms.ads.nativead.NativeAdView;

/**
 * Loads one native advanced ad into a slot (view_native_ad_slot.xml).
 * Shows a same-size loading skeleton meanwhile, collapses the slot if no ad is available and
 * destroys the ad together with the activity.
 */
public final class NativeAdController implements DefaultLifecycleObserver {

    private final AppCompatActivity activity;
    private final ViewGroup slot;
    private final String adUnitId;

    private NativeAd nativeAd;
    private ObjectAnimator shimmer;
    private boolean destroyed;

    private NativeAdController(AppCompatActivity activity, ViewGroup slot, String adUnitId) {
        this.activity = activity;
        this.slot = slot;
        this.adUnitId = adUnitId;
    }

    /** Starts loading immediately; the controller follows the activity lifecycle by itself. */
    public static NativeAdController attach(AppCompatActivity activity, ViewGroup slot, String adUnitId) {
        NativeAdController controller = new NativeAdController(activity, slot, adUnitId);
        activity.getLifecycle().addObserver(controller);
        controller.load();
        return controller;
    }

    private void load() {
        startShimmer();
        final AppCompatActivity context = activity;
        // Building the AdLoader is recommended off the main thread; callbacks arrive on the main thread.
        new Thread(() -> {
            AdLoader adLoader = new AdLoader.Builder(context, adUnitId)
                    .forNativeAd(this::onAdLoaded)
                    .withAdListener(new AdListener() {
                        @Override
                        public void onAdFailedToLoad(@NonNull LoadAdError loadAdError) {
                            if (!destroyed) {
                                collapse();
                            }
                        }
                    })
                    .withNativeAdOptions(new NativeAdOptions.Builder()
                            .setAdChoicesPlacement(NativeAdOptions.ADCHOICES_TOP_RIGHT)
                            .setMediaAspectRatio(NativeAdOptions.NATIVE_MEDIA_ASPECT_RATIO_LANDSCAPE)
                            .setVideoOptions(new VideoOptions.Builder().setStartMuted(true).build())
                            .build())
                    .build();
            adLoader.loadAd(new AdRequest.Builder().build());
        }, "native-ad-loader").start();
    }

    private void onAdLoaded(NativeAd ad) {
        if (destroyed || activity.isDestroyed() || activity.isFinishing()
                || activity.isChangingConfigurations()) {
            ad.destroy();
            return;
        }
        if (nativeAd != null) {
            nativeAd.destroy();
        }
        nativeAd = ad;

        NativeAdView adView = (NativeAdView) LayoutInflater.from(activity)
                .inflate(R.layout.ad_native_large, slot, false);
        populate(ad, adView);
        stopShimmer();
        slot.removeAllViews();
        slot.addView(adView);
        slot.setVisibility(View.VISIBLE);
    }

    private static void populate(NativeAd ad, NativeAdView adView) {
        TextView headline = adView.findViewById(R.id.ad_headline);
        TextView body = adView.findViewById(R.id.ad_body);
        TextView advertiser = adView.findViewById(R.id.ad_advertiser);
        TextView sponsored = adView.findViewById(R.id.ad_sponsored);
        TextView callToAction = adView.findViewById(R.id.ad_call_to_action);
        ImageView icon = adView.findViewById(R.id.ad_app_icon);
        MediaView media = adView.findViewById(R.id.ad_media);

        icon.setClipToOutline(true);
        media.setClipToOutline(true);

        adView.setHeadlineView(headline);
        adView.setBodyView(body);
        adView.setAdvertiserView(advertiser);
        adView.setCallToActionView(callToAction);
        adView.setIconView(icon);
        adView.setMediaView(media);

        // Headline and media content are guaranteed by the SDK; the rest is optional.
        headline.setText(ad.getHeadline());
        if (ad.getMediaContent() != null) {
            media.setMediaContent(ad.getMediaContent());
        }

        if (ad.getBody() == null) {
            body.setVisibility(View.GONE);
        } else {
            body.setText(ad.getBody());
            body.setVisibility(View.VISIBLE);
        }

        if (ad.getAdvertiser() == null) {
            advertiser.setVisibility(View.GONE);
            sponsored.setText(R.string.ad_sponsored);
        } else {
            advertiser.setText(ad.getAdvertiser());
            advertiser.setVisibility(View.VISIBLE);
            sponsored.setText(R.string.ad_sponsored_separator);
        }

        if (ad.getCallToAction() == null) {
            callToAction.setVisibility(View.GONE);
        } else {
            callToAction.setText(ad.getCallToAction());
            callToAction.setVisibility(View.VISIBLE);
        }

        if (ad.getIcon() == null || ad.getIcon().getDrawable() == null) {
            icon.setVisibility(View.GONE);
        } else {
            icon.setImageDrawable(ad.getIcon().getDrawable());
            icon.setVisibility(View.VISIBLE);
        }

        // Must be called last: hands the populated view over to the SDK.
        adView.setNativeAd(ad);
    }

    private void collapse() {
        stopShimmer();
        slot.removeAllViews();
        slot.setVisibility(View.GONE);
    }

    private void startShimmer() {
        View pulse = slot.findViewById(R.id.native_placeholder_shimmer);
        if (pulse == null) {
            return;
        }
        shimmer = ObjectAnimator.ofFloat(pulse, View.ALPHA, 1f, 0.45f);
        shimmer.setDuration(850);
        shimmer.setRepeatMode(ValueAnimator.REVERSE);
        shimmer.setRepeatCount(ValueAnimator.INFINITE);
        shimmer.start();
    }

    private void stopShimmer() {
        if (shimmer != null) {
            shimmer.cancel();
            shimmer = null;
        }
    }

    @Override
    public void onDestroy(@NonNull LifecycleOwner owner) {
        destroyed = true;
        stopShimmer();
        if (nativeAd != null) {
            nativeAd.destroy();
            nativeAd = null;
        }
        owner.getLifecycle().removeObserver(this);
    }
}
