package com.apkglobal.test.flow;

import androidx.annotation.DrawableRes;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.StringRes;

/** One onboarding step: a question with exactly two answers. Immutable. */
public final class FlowStep {

    public enum Type { QUESTION, CHOICE }

    /** One answer of a step. */
    public static final class Option {
        public final String key;
        @StringRes public final int titleRes;
        @StringRes public final int descRes;
        @DrawableRes public final int iconRes;
        /** True if picking this option opens a rewarded ad (shows the "Watch ad" pill). */
        public final boolean rewarded;
        /** Short label for the summary chips on the Ready screen. */
        @StringRes public final int summaryRes;

        public Option(@NonNull String key, @StringRes int titleRes, @StringRes int descRes,
                      @DrawableRes int iconRes, boolean rewarded, @StringRes int summaryRes) {
            this.key = key;
            this.titleRes = titleRes;
            this.descRes = descRes;
            this.iconRes = iconRes;
            this.rewarded = rewarded;
            this.summaryRes = summaryRes;
        }
    }

    public final String id;
    public final Type type;
    @StringRes public final int titleRes;
    @StringRes public final int subtitleRes;
    @StringRes public final int factRes;
    public final Option optionA;
    public final Option optionB;

    public FlowStep(@NonNull String id, @NonNull Type type, @StringRes int titleRes, @StringRes int subtitleRes,
                    @StringRes int factRes, @NonNull Option optionA, @NonNull Option optionB) {
        this.id = id;
        this.type = type;
        this.titleRes = titleRes;
        this.subtitleRes = subtitleRes;
        this.factRes = factRes;
        this.optionA = optionA;
        this.optionB = optionB;
    }

    /** The option with this key, or null. */
    @Nullable
    public Option option(@Nullable String key) {
        if (key == null) return null;
        if (key.equals(optionA.key)) return optionA;
        if (key.equals(optionB.key)) return optionB;
        return null;
    }

    /** The other answer of this step. */
    @NonNull
    public Option other(@NonNull Option option) {
        return option == optionA ? optionB : optionA;
    }
}
