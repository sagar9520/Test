package com.apkglobal.test.ui;

import android.view.View;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.annotation.StringRes;

import com.apkglobal.test.R;

/** Binds an included {@code view_fact_card} ("Did you know?"). */
public final class FactCard {

    private FactCard() {
    }

    public static void bind(@Nullable View factCard, @StringRes int factRes) {
        if (factCard == null) return;
        TextView text = factCard.findViewById(R.id.fact_text);
        if (text != null) text.setText(factRes);
    }
}
