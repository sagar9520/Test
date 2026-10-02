package com.apkglobal.test.onboarding;

import android.content.Context;
import android.content.SharedPreferences;

/** Stores the option chosen on each onboarding screen. */
public final class OnboardingPrefs {

    public static final String KEY_LANGUAGE = "language";
    public static final String KEY_SCAN_MODE = "scan_mode";
    public static final String KEY_DISPLAY_STYLE = "display_style";
    public static final String KEY_SCAN_QUALITY = "scan_quality";
    public static final String KEY_RESULT_VIEW = "result_view";

    private static final String FILE = "onboarding";

    private OnboardingPrefs() {
    }

    public static String get(Context context, String key, String fallback) {
        return prefs(context).getString(key, fallback);
    }

    public static void put(Context context, String key, String value) {
        prefs(context).edit().putString(key, value).apply();
    }

    private static SharedPreferences prefs(Context context) {
        return context.getApplicationContext().getSharedPreferences(FILE, Context.MODE_PRIVATE);
    }
}
