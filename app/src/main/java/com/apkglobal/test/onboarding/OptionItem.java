package com.apkglobal.test.onboarding;

import androidx.annotation.DrawableRes;
import androidx.annotation.StringRes;

/** One selectable row: thumbnail, title, description. {@code key} is what gets saved. */
public final class OptionItem {

    public final String key;
    @DrawableRes
    public final int thumbnail;
    @StringRes
    public final int title;
    @StringRes
    public final int description;

    public OptionItem(String key, @DrawableRes int thumbnail, @StringRes int title, @StringRes int description) {
        this.key = key;
        this.thumbnail = thumbnail;
        this.title = title;
        this.description = description;
    }
}
