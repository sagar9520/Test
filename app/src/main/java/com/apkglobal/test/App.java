package com.apkglobal.test;

import android.app.Application;

import com.apkglobal.test.ads.AdsInitializer;

public class App extends Application {

    @Override
    public void onCreate() {
        super.onCreate();
        AdsInitializer.initialize(this);
    }
}
