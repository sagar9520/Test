package com.apkglobal.test.ui;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.widget.Toast;

import androidx.annotation.LayoutRes;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.apkglobal.test.R;
import com.apkglobal.test.ads.AdUnits;
import com.apkglobal.test.ads.AdsManager;
import com.apkglobal.test.ads.BannerController;
import com.apkglobal.test.ads.BannerHandle;
import com.apkglobal.test.ads.NativeAdSlot;
import com.apkglobal.test.flow.FlowStep;
import com.apkglobal.test.flow.ScanFlow;
import com.apkglobal.test.flow.ScanSession;
import com.google.android.material.card.MaterialCardView;

import java.util.ArrayList;
import java.util.List;

/**
 * Common behaviour of the two step screens (question cards / two-choice tiles):
 * header + progress, fact card, ONE native slot, ONE anchored banner, and the answer -> ad -> next-step logic.
 *
 * <p>Ad rules applied here: a normal answer goes through {@code InterstitialController.onNavigation} (the
 * FrequencyCapper decides; most taps show nothing). A rewarded answer shows ONLY the rewarded ad (never an
 * interstitial right after it).
 */
public abstract class BaseStepActivity extends AppCompatActivity {

    /** How long the picked card shows its selected state before moving on. */
    private static final long SELECT_FEEDBACK_MS = 150L;

    protected int stepIndex;
    protected FlowStep step;

    private final Handler handler = new Handler(Looper.getMainLooper());
    private final TapGuard guard = new TapGuard(this::resetOptions);
    private final List<MaterialCardView> optionCards = new ArrayList<>(2);
    private final List<FlowStep.Option> options = new ArrayList<>(2);

    private NativeAdSlot nativeSlot;
    private BannerHandle banner;

    @LayoutRes
    protected abstract int layoutRes();

    protected abstract String nativePlacement();

    protected abstract String bannerPlacement();

    /** Bind the step's texts and both answers; call {@link #registerOption} once per answer view. */
    protected abstract void onBindStep(@NonNull FlowStep step);

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Insets.enableEdgeToEdge(this);

        stepIndex = getIntent().getIntExtra(StepNavigator.EXTRA_STEP_INDEX, 0);
        if (stepIndex < 0 || stepIndex >= ScanFlow.size()) {
            finish();
            return;
        }
        step = ScanFlow.get(stepIndex);

        setContentView(layoutRes());
        Insets.apply(findViewById(R.id.root));

        int total = ScanFlow.size();
        StepHeader.bind(this, stepIndex + 1, total, getString(R.string.step_label, stepIndex + 1, total));
        onBindStep(step);
        FactCard.bind(findViewById(R.id.fact_card), step.factRes);

        nativeSlot = AdsManager.get().natives().bind(this, findViewById(R.id.native_slot), nativePlacement());
        banner = BannerController.attachAnchored(this, findViewById(R.id.banner_container), bannerPlacement());
    }

    /** Makes {@code card} choose {@code option} when tapped. */
    protected final void registerOption(@NonNull MaterialCardView card, @NonNull FlowStep.Option option) {
        optionCards.add(card);
        options.add(option);
        card.setOnClickListener(v -> choose(card, option));
    }

    private void choose(MaterialCardView card, FlowStep.Option option) {
        if (!guard.tryLock()) return; // double tap / second option while moving on
        for (MaterialCardView c : optionCards) {
            c.setEnabled(false);
            if (c != card) c.setAlpha(OptionViews.UNSELECTED_ALPHA);
        }
        OptionViews.setSelected(card, true);
        handler.postDelayed(() -> onOptionChosen(option), SELECT_FEEDBACK_MS);
    }

    /** Saves the answer, then shows the right ad for it (if any) and opens the next step. */
    protected void onOptionChosen(@NonNull FlowStep.Option option) {
        if (isFinishing() || isDestroyed()) return;
        final ScanSession session = ScanSession.get();
        session.put(step.id, option.key);
        final int next = stepIndex + 1;

        if (option.rewarded && !session.isDetailedUnlocked()) {
            AdsManager.get().rewarded().show(this, AdUnits.PL_REWARDED_DETAILED, rewarded -> {
                session.setDetailedUnlocked(rewarded);
                if (!rewarded) {
                    // Not earned (closed early, or no ad and GRANT_REWARD_IF_NO_AD=false): continue with the free answer.
                    session.put(step.id, step.other(option).key);
                    Toast.makeText(getApplicationContext(), R.string.detail_not_unlocked, Toast.LENGTH_LONG).show();
                }
                goTo(next); // no interstitial right after a rewarded ad
            });
        } else {
            AdsManager.get().interstitials().onNavigation(this, AdUnits.PL_STEP_INTERSTITIAL, () -> goTo(next));
        }
    }

    private void goTo(int index) {
        if (isFinishing() || isDestroyed()) return;
        guard.doneAway();
        StepNavigator.open(this, index);
    }

    /** TapGuard unlocked: the user is back on this screen (Back from the next step) and may change the answer. */
    private void resetOptions() {
        for (int i = 0; i < optionCards.size(); i++) {
            MaterialCardView c = optionCards.get(i);
            c.setEnabled(true);
            c.setAlpha(1f);
            OptionViews.setSelected(c, false);
            OptionViews.updateRewardChip(c, options.get(i));
        }
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
        handler.removeCallbacksAndMessages(null);
        guard.onDestroy();
        if (nativeSlot != null) nativeSlot.release();
        if (banner != null) banner.destroy();
        super.onDestroy();
    }
}
