package com.apkglobal.test.ads;

import android.content.Context;

import com.google.android.gms.ads.MobileAds;

import java.util.concurrent.atomic.AtomicBoolean;

/** Initializes the Google Mobile Ads SDK once, off the main thread. */
public final class AdsInitializer {

    private static final AtomicBoolean STARTED = new AtomicBoolean(false);

    private AdsInitializer() {
    }

    public static void initialize(Context context) {
        if (STARTED.getAndSet(true)) {
            return;
        }
        final Context appContext = context.getApplicationContext();
        new Thread(() -> MobileAds.initialize(appContext, initializationStatus -> {
        }), "ads-init").start();
    }
}
