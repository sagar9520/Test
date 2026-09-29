package com.apkglobal.test.ads;

import android.util.Log;

import com.applovin.mediation.MaxAd;
import com.applovin.mediation.MaxAdFormat;
import com.applovin.mediation.MaxAdRevenueListener;

import java.util.Locale;

/**
 * Impression-level revenue (ILRD). Set {@link #LISTENER} on every MAX ad object.
 *
 * <p>Why it matters for eCPM: with per-impression revenue in your analytics you can see the real value of
 * each placement/screen and cut the ones that only add impressions at a very low price.
 */
public final class RevenueTracker {

    private static final String TAG = "XRayAds";

    /** Called by MAX on the main thread for every paid impression. */
    public static final MaxAdRevenueListener LISTENER = RevenueTracker::onRevenue;

    private RevenueTracker() {
    }

    public static void onRevenue(MaxAd ad) {
        if (ad == null) return;
        MaxAdFormat format = ad.getFormat();
        String formatLabel = format != null ? format.getLabel() : "unknown";
        Log.d(TAG, String.format(Locale.US,
                "revenue=%.6f USD precision=%s network=%s format=%s placement=%s adUnit=%s",
                ad.getRevenue(), ad.getRevenuePrecision(), ad.getNetworkName(), formatLabel,
                ad.getPlacement(), ad.getAdUnitId()));

        // Firebase Analytics (add com.google.firebase:firebase-analytics first) — lets you compare
        // ad revenue per screen/network and feed tROAS campaigns:
        //
        // Bundle params = new Bundle();
        // params.putString(FirebaseAnalytics.Param.AD_PLATFORM, "appLovin");
        // params.putString(FirebaseAnalytics.Param.AD_SOURCE, ad.getNetworkName());
        // params.putString(FirebaseAnalytics.Param.AD_FORMAT, formatLabel);
        // params.putString(FirebaseAnalytics.Param.AD_UNIT_NAME, ad.getAdUnitId());
        // params.putString("placement", ad.getPlacement());
        // params.putDouble(FirebaseAnalytics.Param.VALUE, ad.getRevenue());
        // params.putString(FirebaseAnalytics.Param.CURRENCY, "USD"); // MAX always reports USD
        // FirebaseAnalytics.getInstance(context).logEvent(FirebaseAnalytics.Event.AD_IMPRESSION, params);
    }
}
