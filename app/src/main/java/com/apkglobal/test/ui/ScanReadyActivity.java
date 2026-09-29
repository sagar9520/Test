package com.apkglobal.test.ui;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.apkglobal.test.R;
import com.apkglobal.test.ads.AdUnits;
import com.apkglobal.test.ads.AdsManager;
import com.apkglobal.test.ads.NativeAdSlot;
import com.apkglobal.test.flow.FlowStep;
import com.apkglobal.test.flow.ScanFlow;
import com.apkglobal.test.flow.ScanSession;
import com.apkglobal.test.flow.XRayFacts;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;

/**
 * End of the setup flow: summary of the answers, optional rewarded "HD scan" unlock and "Start scan"
 * (an interstitial may show here: a natural break before the main feature). ONE native, NO banner.
 */
public final class ScanReadyActivity extends AppCompatActivity {

    private final TapGuard guard = new TapGuard(this::enableButtons);
    private NativeAdSlot nativeSlot;
    private MaterialButton startButton;
    private MaterialButton hdButton;
    private MaterialButton restartButton;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Insets.enableEdgeToEdge(this);
        setContentView(R.layout.activity_scan_ready);
        Insets.apply(findViewById(R.id.root));

        int total = ScanFlow.size();
        StepHeader.bind(this, total, total, getString(R.string.step_label_ready));
        FactCard.bind(findViewById(R.id.fact_card), XRayFacts.readyFact());
        bindSummary();

        startButton = findViewById(R.id.start_button);
        hdButton = findViewById(R.id.hd_button);
        restartButton = findViewById(R.id.restart_button);
        startButton.setOnClickListener(v -> startScan());
        hdButton.setOnClickListener(v -> unlockHd());
        restartButton.setOnClickListener(v -> StepNavigator.restart(this));
        updateHdState();

        nativeSlot = AdsManager.get().natives().bind(this, findViewById(R.id.native_slot), AdUnits.PL_READY_NATIVE);
    }

    /** One read-only chip per answered step (icon + short label). */
    private void bindSummary() {
        ChipGroup group = findViewById(R.id.summary_chips);
        group.removeAllViews();
        ScanSession session = ScanSession.get();
        LayoutInflater inflater = getLayoutInflater();
        for (FlowStep s : ScanFlow.steps()) {
            FlowStep.Option option = s.option(session.get(s.id));
            if (option == null) continue;
            Chip chip = (Chip) inflater.inflate(R.layout.view_summary_chip, group, false);
            chip.setText(option.summaryRes);
            chip.setChipIconResource(option.iconRes);
            group.addView(chip);
        }
        // Nothing answered (e.g. the process was restarted on this screen): hide the empty card.
        findViewById(R.id.summary_card).setVisibility(group.getChildCount() == 0 ? View.GONE : View.VISIBLE);
    }

    private void unlockHd() {
        if (!guard.tryLock()) return;
        setButtonsEnabled(false);
        AdsManager.get().rewarded().show(this, AdUnits.PL_REWARDED_HD, rewarded -> {
            ScanSession.get().setHdUnlocked(rewarded);
            if (!rewarded) {
                Toast.makeText(getApplicationContext(), R.string.hd_not_unlocked, Toast.LENGTH_LONG).show();
            }
            guard.doneHere();
            updateHdState();
        });
    }

    private void startScan() {
        if (!guard.tryLock()) return;
        setButtonsEnabled(false);
        AdsManager.get().interstitials().onMajorAction(this, AdUnits.PL_READY_INTERSTITIAL, () -> {
            guard.doneHere();
            // Plug the real camera / scanner screen of the app in here, e.g.
            // startActivity(new Intent(this, ScannerActivity.class)); it can read ScanSession for the answers.
            Toast.makeText(getApplicationContext(), R.string.scanner_opening, Toast.LENGTH_SHORT).show();
        });
    }

    private void updateHdState() {
        boolean unlocked = ScanSession.get().isHdUnlocked();
        hdButton.setVisibility(unlocked ? View.GONE : View.VISIBLE);
        findViewById(R.id.hd_status).setVisibility(unlocked ? View.VISIBLE : View.GONE);
    }

    private void enableButtons() {
        setButtonsEnabled(true);
    }

    private void setButtonsEnabled(boolean enabled) {
        if (startButton == null) return;
        startButton.setEnabled(enabled);
        hdButton.setEnabled(enabled);
        restartButton.setEnabled(enabled);
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
    protected void onDestroy() {
        guard.onDestroy();
        if (nativeSlot != null) nativeSlot.release();
        super.onDestroy();
    }
}
