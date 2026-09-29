package com.apkglobal.test;

import android.app.Application;

import com.apkglobal.test.ads.AdsManager;

/** Application entry point: starts the ad SDK as early as possible so the first screens have ads ready. */
public class XRayApp extends Application {

    @Override
    public void onCreate() {
        super.onCreate();
        AdsManager.get().initialize(this);
    }
}
