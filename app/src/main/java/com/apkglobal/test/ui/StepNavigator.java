package com.apkglobal.test.ui;

import android.app.Activity;
import android.content.Intent;

import androidx.annotation.NonNull;

import com.apkglobal.test.MainActivity;
import com.apkglobal.test.flow.FlowStep;
import com.apkglobal.test.flow.ScanFlow;
import com.apkglobal.test.flow.ScanSession;

/** Opens the screen for a flow step (question or two-choice), or the Ready screen after the last step. */
public final class StepNavigator {

    public static final String EXTRA_STEP_INDEX = "step_index";

    private StepNavigator() {
    }

    public static void open(@NonNull Activity from, int index) {
        int size = ScanFlow.size();
        int i = Math.max(0, index);
        Intent intent;
        if (i >= size) {
            intent = new Intent(from, ScanReadyActivity.class);
        } else {
            FlowStep step = ScanFlow.get(i);
            Class<? extends Activity> target =
                    step.type == FlowStep.Type.CHOICE ? ChoiceActivity.class : QuestionActivity.class;
            intent = new Intent(from, target).putExtra(EXTRA_STEP_INDEX, i);
        }
        from.startActivity(intent);
    }

    /** Clears the answers and returns to the home screen (closing every step screen above it). */
    public static void restart(@NonNull Activity from) {
        ScanSession.get().clear();
        Intent intent = new Intent(from, MainActivity.class)
                .addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
        from.startActivity(intent);
    }
}
