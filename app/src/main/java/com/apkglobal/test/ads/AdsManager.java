package com.apkglobal.test.ads;

import android.app.Activity;
import android.app.Application;
import android.content.Context;
import android.net.Uri;
import android.os.Handler;
import android.os.Looper;
import android.os.SystemClock;
import android.util.Log;

import androidx.annotation.MainThread;
import androidx.lifecycle.Lifecycle;
import androidx.lifecycle.LifecycleOwner;

import com.apkglobal.test.BuildConfig;
import com.applovin.sdk.AppLovinMediationProvider;
import com.applovin.sdk.AppLovinSdk;
import com.applovin.sdk.AppLovinSdkInitializationConfiguration;
import com.applovin.sdk.AppLovinSdkSettings;
import com.applovin.sdk.AppLovinTermsAndPrivacyPolicyFlowSettings;

import java.util.ArrayList;
import java.util.List;

/**
 * Single entry point for ads. All methods are meant to be called on the main thread.
 *
 * <p>If the SDK key or an ad unit id is still a placeholder, everything degrades to "no ads" and the app keeps
 * working: continuations still run, ad slots collapse.
 */
public final class AdsManager {

    static final String TAG = "XRayAds";

    /** REPLACE with your real privacy policy URL (also required on the Play listing). */
    public static final String PRIVACY_POLICY_URL = "https://example.com/privacy";

    /**
     * MAX Terms &amp; Privacy Policy flow (Google UMP consent in the EEA/UK).
     *
     * <p>Off by default: once enabled, MAX also shows a terms/privacy alert to new users OUTSIDE the EEA (India
     * included), an extra dialog before the first screen. Turn it on if you have EEA/UK traffic (Google requires a
     * certified consent message there to serve personalised ads) and add
     * {@code com.google.android.ump:user-messaging-platform} to app/build.gradle when you do.
     */
    public static final boolean TERMS_FLOW_ENABLED = false;

    private static final class Holder {
        static final AdsManager INSTANCE = new AdsManager();
    }

    private final Handler main = new Handler(Looper.getMainLooper());
    // Monotonic clock: pacing must not break when the user changes the device time.
    private final FrequencyCapper capper = new FrequencyCapper(SystemClock::elapsedRealtime);
    private final InterstitialController interstitials;
    private final RewardedController rewarded;
    private final NativeAdController natives;
    private final AppOpenController appOpen;
    private final List<Runnable> whenReady = new ArrayList<>();

    private boolean initStarted;
    private boolean initSkipped;
    private boolean sdkReady;
    private boolean fullscreenShowing;

    private AdsManager() {
        interstitials = new InterstitialController(this, main);
        rewarded = new RewardedController(this, main);
        natives = new NativeAdController(this, main);
        appOpen = new AppOpenController(this, main);
    }

    public static AdsManager get() {
        return Holder.INSTANCE;
    }

    /** Idempotent. Call from {@link Application#onCreate()}. */
    @MainThread
    public void initialize(Application app) {
        if (app == null) return;
        if (!isMainThread()) {
            main.post(() -> initialize(app));
            return;
        }
        if (initStarted) return;
        initStarted = true;

        appOpen.attach(app);

        if (!AdConfig.ADS_ENABLED) {
            skipInit("ads disabled (AdConfig.ADS_ENABLED=false)");
            return;
        }
        String sdkKey = BuildConfig.APPLOVIN_SDK_KEY == null ? "" : BuildConfig.APPLOVIN_SDK_KEY.trim();
        if (sdkKey.isEmpty() || sdkKey.startsWith("YOUR_")) {
            skipInit("AppLovin SDK key is not set: add applovin.sdk.key to local.properties. Running without ads.");
            return;
        }

        try {
            AppLovinSdk sdk = AppLovinSdk.getInstance(app);

            // All settings must be applied BEFORE initialize().
            AppLovinSdkSettings settings = sdk.getSettings();
            settings.setVerboseLogging(BuildConfig.DEBUG);
            // The creative debugger defaults to ON, so release builds must switch it off explicitly.
            settings.setCreativeDebuggerEnabled(BuildConfig.DEBUG);

            boolean termsFlow = TERMS_FLOW_ENABLED && isRealPrivacyPolicyUrl();
            AppLovinTermsAndPrivacyPolicyFlowSettings flow = settings.getTermsAndPrivacyPolicyFlowSettings();
            flow.setEnabled(termsFlow);
            if (termsFlow) {
                flow.setPrivacyPolicyUri(Uri.parse(PRIVACY_POLICY_URL));
            }

            // Selective init: mediated networks are initialised only for the units this app really loads.
            AppLovinSdkInitializationConfiguration config =
                    AppLovinSdkInitializationConfiguration.builder(sdkKey)
                            .setMediationProvider(AppLovinMediationProvider.MAX)
                            .setTestDeviceAdvertisingIds(AdUnits.TEST_DEVICE_GAIDS)
                            .setAdUnitIds(AdUnits.configuredUnits())
                            .build();

            sdk.initialize(config, sdkConfiguration -> runOnMain(this::onSdkInitialized));
        } catch (RuntimeException e) {
            Log.e(TAG, "AppLovin SDK init failed; running without ads", e);
            skipInit("init threw");
        }
    }

    private void skipInit(String reason) {
        Log.w(TAG, "Ads not initialised: " + reason);
        initSkipped = true;
        whenReady.clear();
    }

    private void onSdkInitialized() {
        if (sdkReady) return;
        sdkReady = true;
        Log.i(TAG, "AppLovin SDK initialised");

        List<Runnable> queued = new ArrayList<>(whenReady);
        whenReady.clear();
        for (Runnable r : queued) {
            r.run();
        }

        // Load only after init: requests made earlier reach AdMob/Meta before they are initialised
        // and win fewer auctions (lower fill and eCPM).
        appOpen.load();        // first, so a cold-start app-open can make its short window
        natives.preload();     // the home screen is usually waiting for one
        interstitials.load();
        rewarded.load();
    }

    public boolean isSdkReady() {
        return sdkReady;
    }

    /**
     * Runs {@code r} on the main thread once the SDK is initialised (immediately if it already is).
     * Dropped if ads are disabled / init was skipped.
     */
    @MainThread
    public void runWhenSdkReady(Runnable r) {
        if (r == null) return;
        if (!isMainThread()) {
            main.post(() -> runWhenSdkReady(r));
            return;
        }
        if (initSkipped) return;
        if (sdkReady) {
            r.run();
        } else if (!whenReady.contains(r)) {
            whenReady.add(r);
        }
    }

    void cancelWhenSdkReady(Runnable r) {
        whenReady.remove(r);
    }

    public InterstitialController interstitials() {
        return interstitials;
    }

    public RewardedController rewarded() {
        return rewarded;
    }

    public NativeAdController natives() {
        return natives;
    }

    public AppOpenController appOpen() {
        return appOpen;
    }

    public FrequencyCapper capper() {
        return capper;
    }

    public boolean isFullscreenShowing() {
        return fullscreenShowing;
    }

    /** A fullscreen ad started (true) or was closed (false). Closing starts the fullscreen cooldown. */
    void setFullscreenShowing(boolean showing) {
        boolean wasShowing = fullscreenShowing;
        fullscreenShowing = showing;
        if (wasShowing && !showing) {
            capper.onFullscreenDismissed();
        }
    }

    /** A fullscreen ad failed to appear: clear the flag without a cooldown (the user saw no ad). */
    void clearFullscreenShowing() {
        fullscreenShowing = false;
    }

    /** Any ad was clicked; the user is probably leaving for the Play Store or a browser. */
    void onAdClicked() {
        appOpen.onAnyAdClicked();
    }

    /** True unless ads are switched off or SDK init was skipped (placeholder key). */
    boolean adsAvailable() {
        return AdConfig.ADS_ENABLED && !initSkipped;
    }

    /** True when the SDK is initialised, ads are on and {@code adUnitId} is a real id. */
    boolean canLoad(String adUnitId) {
        return sdkReady && AdConfig.ADS_ENABLED && AdUnits.isConfigured(adUnitId);
    }

    /** Debug helper: MAX Mediation Debugger (adapter status, test ads per network). */
    public void showMediationDebugger(Context ctx) {
        if (ctx == null) return;
        if (!sdkReady) {
            Log.w(TAG, "Mediation debugger needs an initialised SDK (is applovin.sdk.key set?)");
            return;
        }
        AppLovinSdk.getInstance(ctx).showMediationDebugger();
    }

    /**
     * True if a fullscreen ad may be presented over {@code activity} now: alive AND resumed. A tap handled a
     * moment before the user pressed Home must not pop an ad over another app (out-of-app ads break network
     * policy); the caller then just continues without an ad.
     */
    static boolean canPresentOn(Activity activity) {
        if (activity == null || activity.isFinishing() || activity.isDestroyed()) return false;
        if (activity instanceof LifecycleOwner) {
            return ((LifecycleOwner) activity).getLifecycle().getCurrentState()
                    .isAtLeast(Lifecycle.State.RESUMED);
        }
        return true;
    }

    static boolean isRealPrivacyPolicyUrl() {
        String url = PRIVACY_POLICY_URL;
        return url != null && url.startsWith("https://") && !url.contains("example.com");
    }

    static boolean isMainThread() {
        return Looper.myLooper() == Looper.getMainLooper();
    }

    void runOnMain(Runnable r) {
        if (isMainThread()) {
            r.run();
        } else {
            main.post(r);
        }
    }
}
