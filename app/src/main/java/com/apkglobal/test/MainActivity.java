package com.apkglobal.test;

import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.apkglobal.test.ads.AdUnits;
import com.apkglobal.test.ads.AdsManager;
import com.apkglobal.test.ads.BannerController;
import com.apkglobal.test.ads.BannerHandle;
import com.apkglobal.test.ads.NativeAdSlot;
import com.apkglobal.test.flow.ScanFlow;
import com.apkglobal.test.flow.ScanSession;
import com.apkglobal.test.flow.XRayFacts;
import com.apkglobal.test.ui.FactCard;
import com.apkglobal.test.ui.Insets;
import com.apkglobal.test.ui.StepNavigator;
import com.apkglobal.test.ui.TapGuard;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.chip.Chip;

/**
 * Home: hero card, one big "Start scan" button, disclaimer, ONE native below the content, a fact card and an
 * anchored banner at the bottom.
 */
public class MainActivity extends AppCompatActivity {

    private final TapGuard guard = new TapGuard(this::onStartUnlocked);
    private NativeAdSlot nativeSlot;
    private BannerHandle banner;
    private MaterialButton startButton;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Insets.enableEdgeToEdge(this);
        setContentView(R.layout.activity_main);
        Insets.apply(findViewById(R.id.root));

        if (BuildConfig.DEBUG) {
            // Debug builds only: long-press the title bar to open the AppLovin Mediation Debugger.
            MaterialToolbar toolbar = findViewById(R.id.toolbar);
            toolbar.setOnLongClickListener(v -> {
                AdsManager.get().showMediationDebugger(this);
                return true;
            });
        }

        int steps = ScanFlow.size();
        Chip stepsChip = findViewById(R.id.home_feature_steps);
        stepsChip.setText(getResources().getQuantityString(R.plurals.home_feature_steps, steps, steps));
        FactCard.bind(findViewById(R.id.fact_card), XRayFacts.homeFact());

        startButton = findViewById(R.id.start_button);
        startButton.setOnClickListener(v -> start());

        nativeSlot = AdsManager.get().natives().bind(this, findViewById(R.id.native_slot), AdUnits.PL_HOME_NATIVE);
        banner = BannerController.attachAnchored(this, findViewById(R.id.banner_container), AdUnits.PL_HOME_BANNER);
    }

    private void start() {
        if (!guard.tryLock()) return; // double-tap guard
        startButton.setEnabled(false);
        ScanSession.get().clear();
        // FrequencyCapper never shows an interstitial on the first tap of a session, so a new user goes
        // straight into the flow; a returning user may see one here, at a natural break.
        AdsManager.get().interstitials().onNavigation(this, AdUnits.PL_STEP_INTERSTITIAL, () -> {
            if (isFinishing() || isDestroyed()) return;
            guard.doneAway();
            StepNavigator.open(this, 0);
        });
    }

    private void onStartUnlocked() {
        startButton.setEnabled(true);
    }

    @Override
    protected void onStart() {
        super.onStart();
        if (banner != null) banner.resume();
    }

    @Override
    protected void onResume() {
        super.onResume();
        guard.onResume();
    }

    @Override
    protected void onPause() {
        guard.onPause();
        super.onPause();
    }

    @Override
    protected void onStop() {
        if (banner != null) banner.pause();
        super.onStop();
    }

    @Override
    protected void onDestroy() {
        guard.onDestroy();
        if (nativeSlot != null) nativeSlot.release();
        if (banner != null) banner.destroy();
        super.onDestroy();
    }
}
