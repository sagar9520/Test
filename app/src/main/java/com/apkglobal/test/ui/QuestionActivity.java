package com.apkglobal.test.ui;

import android.widget.TextView;

import androidx.annotation.NonNull;

import com.apkglobal.test.R;
import com.apkglobal.test.ads.AdUnits;
import com.apkglobal.test.flow.FlowStep;
import com.google.android.material.card.MaterialCardView;

/** Screen type A: question + two large answer cards. */
public final class QuestionActivity extends BaseStepActivity {

    @Override
    protected int layoutRes() {
        return R.layout.activity_question;
    }

    @Override
    protected String nativePlacement() {
        return AdUnits.PL_QUESTION_NATIVE;
    }

    @Override
    protected String bannerPlacement() {
        return AdUnits.PL_QUESTION_BANNER;
    }

    @Override
    protected void onBindStep(@NonNull FlowStep step) {
        TextView title = findViewById(R.id.question_title);
        TextView subtitle = findViewById(R.id.question_subtitle);
        title.setText(step.titleRes);
        subtitle.setText(step.subtitleRes);

        MaterialCardView a = findViewById(R.id.option_a);
        MaterialCardView b = findViewById(R.id.option_b);
        OptionViews.bindOptionCard(a, step.optionA);
        OptionViews.bindOptionCard(b, step.optionB);
        registerOption(a, step.optionA);
        registerOption(b, step.optionB);
    }
}
