package com.apkglobal.test.onboarding;

import android.app.Activity;

import androidx.annotation.DrawableRes;
import androidx.annotation.StringRes;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Everything that differs between the 5 onboarding screens: texts, options and where the ads go.
 *
 * <p>Ad positions are expressed relative to option keys, e.g. {@code nativeAdAfter("gray_scale")}
 * puts the native ad (and the Continue button right below it) under the "Gray Scale" card, and
 * {@code bannerAfter("thermal_view")} puts an in-list banner under "Thermal View". Every screen
 * also has the fixed bottom banner.
 */
public final class ScreenSpec {

    /** Total number of onboarding steps shown in the progress indicator. */
    public static final int TOTAL_STEPS = 5;

    /** Position value meaning "before the first option". */
    static final int TOP = -1;

    final int step;
    @StringRes
    final int title;
    @StringRes
    final int subtitle;
    final String prefKey;
    final List<OptionItem> options;
    /** Index of the option the native ad + Continue button follow ({@link #TOP} = above all options). */
    final int nativeAdAfterIndex;
    /** Indexes of the options an in-list banner follows. */
    final Set<Integer> bannerAfterIndexes;
    final Class<? extends Activity> next;

    private ScreenSpec(Builder b, int nativeAdAfterIndex, Set<Integer> bannerAfterIndexes) {
        this.step = b.step;
        this.title = b.title;
        this.subtitle = b.subtitle;
        this.prefKey = b.prefKey;
        this.options = Collections.unmodifiableList(new ArrayList<>(b.options));
        this.nativeAdAfterIndex = nativeAdAfterIndex;
        this.bannerAfterIndexes = Collections.unmodifiableSet(bannerAfterIndexes);
        this.next = b.next;
    }

    public static final class Builder {
        private final int step;
        private final int title;
        private final int subtitle;
        private final String prefKey;
        private final List<OptionItem> options = new ArrayList<>();
        private final List<String> bannerAfterKeys = new ArrayList<>();
        private String nativeAnchorKey;
        private boolean nativeBefore;
        private Class<? extends Activity> next;

        public Builder(int step, @StringRes int title, @StringRes int subtitle, String prefKey) {
            this.step = step;
            this.title = title;
            this.subtitle = subtitle;
            this.prefKey = prefKey;
        }

        public Builder option(String key, @DrawableRes int thumbnail, @StringRes int title, @StringRes int description) {
            options.add(new OptionItem(key, thumbnail, title, description));
            return this;
        }

        /** Native ad (followed by the Continue button) directly below the given option. */
        public Builder nativeAdAfter(String optionKey) {
            nativeAnchorKey = optionKey;
            nativeBefore = false;
            return this;
        }

        /** Native ad (followed by the Continue button) directly above the given option. */
        public Builder nativeAdBefore(String optionKey) {
            nativeAnchorKey = optionKey;
            nativeBefore = true;
            return this;
        }

        /** In-list banner directly below the given option (same size as the bottom banner). */
        public Builder bannerAfter(String optionKey) {
            bannerAfterKeys.add(optionKey);
            return this;
        }

        /** Screen opened by Continue. */
        public Builder next(Class<? extends Activity> next) {
            this.next = next;
            return this;
        }

        public ScreenSpec build() {
            if (options.isEmpty()) {
                throw new IllegalStateException("A selection screen needs at least one option");
            }
            if (next == null) {
                throw new IllegalStateException("Missing next screen for step " + step);
            }
            int nativeIndex;
            if (nativeAnchorKey == null) {
                nativeIndex = options.size() - 1; // default: after the last option
            } else {
                nativeIndex = indexOf(nativeAnchorKey) - (nativeBefore ? 1 : 0);
            }
            Set<Integer> bannerIndexes = new HashSet<>();
            for (String key : bannerAfterKeys) {
                bannerIndexes.add(indexOf(key));
            }
            return new ScreenSpec(this, nativeIndex, bannerIndexes);
        }

        private int indexOf(String key) {
            for (int i = 0; i < options.size(); i++) {
                if (options.get(i).key.equals(key)) {
                    return i;
                }
            }
            throw new IllegalStateException("Unknown option key '" + key + "' on step " + step);
        }
    }
}
