package com.apkglobal.test.ui;

import android.widget.TextView;

import androidx.annotation.NonNull;

import com.apkglobal.test.R;
import com.apkglobal.test.ads.AdUnits;
import com.apkglobal.test.flow.FlowStep;
import com.google.android.material.card.MaterialCardView;

/** Screen type B: two big side-by-side tiles. */
public final class ChoiceActivity extends BaseStepActivity {

    @Override
    protected int layoutRes() {
        return R.layout.activity_choice;
    }

    @Override
    protected String nativePlacement() {
        return AdUnits.PL_CHOICE_NATIVE;
    }

    @Override
    protected String bannerPlacement() {
        return AdUnits.PL_CHOICE_BANNER;
    }

    @Override
    protected void onBindStep(@NonNull FlowStep step) {
        TextView title = findViewById(R.id.choice_title);
        TextView subtitle = findViewById(R.id.choice_subtitle);
        title.setText(step.titleRes);
        subtitle.setText(step.subtitleRes);

        MaterialCardView a = findViewById(R.id.choice_a);
        MaterialCardView b = findViewById(R.id.choice_b);
        OptionViews.bindChoiceTile(a, step.optionA);
        OptionViews.bindChoiceTile(b, step.optionB);
        registerOption(a, step.optionA);
        registerOption(b, step.optionB);
    }
}
