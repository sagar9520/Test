package com.apkglobal.test.ui;

import android.app.Activity;
import android.widget.TextView;

import androidx.annotation.NonNull;

import com.apkglobal.test.R;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.progressindicator.LinearProgressIndicator;

/** Binds the shared {@code view_step_header}: toolbar, "Step n of N" label and progress bar. */
final class StepHeader {

    private StepHeader() {
    }

    /**
     * Toolbar back arrow closes the screen; progress animates from the previous step to {@code progress}.
     */
    static void bind(@NonNull Activity activity, int progress, int max, @NonNull CharSequence label) {
        MaterialToolbar toolbar = activity.findViewById(R.id.toolbar);
        if (toolbar != null) {
            toolbar.setNavigationOnClickListener(v -> activity.finish());
        }
        TextView stepLabel = activity.findViewById(R.id.step_label);
        if (stepLabel != null) stepLabel.setText(label);

        LinearProgressIndicator bar = activity.findViewById(R.id.step_progress);
        if (bar != null) {
            int safeMax = Math.max(1, max);
            int value = Math.max(0, Math.min(progress, safeMax));
            bar.setMax(safeMax);
            bar.setProgressCompat(Math.max(0, value - 1), false);
            bar.post(() -> bar.setProgressCompat(value, true));
        }
    }
}
