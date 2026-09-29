package com.apkglobal.test.ads;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * MAX ad unit ids and placement names.
 *
 * <p>Create one ad unit per FORMAT in the MAX dashboard (not one per screen): a single unit per format gets
 * more auction volume per unit, which the bidders learn from faster. Screens are told apart with placements,
 * which show up in MAX reporting as a breakdown per placement.
 */
public final class AdUnits {

    // Paste the 16-character MAX ad unit ids here (MAX dashboard -> Ad Units).
    public static final String INTERSTITIAL = "YOUR_INTERSTITIAL_AD_UNIT_ID";
    public static final String REWARDED = "YOUR_REWARDED_AD_UNIT_ID";
    public static final String APP_OPEN = "YOUR_APP_OPEN_AD_UNIT_ID";
    public static final String NATIVE_MEDIUM = "YOUR_NATIVE_AD_UNIT_ID";
    public static final String BANNER = "YOUR_BANNER_AD_UNIT_ID";

    /** Every unit the app may load. Used for selective SDK init. */
    public static final List<String> ALL = Collections.unmodifiableList(
            Arrays.asList(INTERSTITIAL, REWARDED, APP_OPEN, NATIVE_MEDIUM, BANNER));

    /**
     * Advertising ids (GAID) of your own test phones: they get test ads, so you never click your own live ads
     * (self-clicks are invalid traffic and get the account limited). Settings -> Google -> Ads shows the id.
     */
    public static final List<String> TEST_DEVICE_GAIDS = Collections.emptyList();

    // Placement names (MAX reporting per placement).
    public static final String PL_HOME_NATIVE = "home_native";
    public static final String PL_HOME_BANNER = "home_banner";
    public static final String PL_QUESTION_NATIVE = "question_native";
    public static final String PL_QUESTION_BANNER = "question_banner";
    public static final String PL_CHOICE_NATIVE = "choice_native";
    public static final String PL_CHOICE_BANNER = "choice_banner";
    public static final String PL_READY_NATIVE = "ready_native";
    public static final String PL_STEP_INTERSTITIAL = "step_next";
    public static final String PL_READY_INTERSTITIAL = "scan_start";
    public static final String PL_REWARDED_DETAILED = "unlock_detailed";
    public static final String PL_REWARDED_HD = "unlock_hd";
    public static final String PL_APP_OPEN = "app_open";

    private static final int MAX_AD_UNIT_ID_LENGTH = 16;

    private AdUnits() {
    }

    /**
     * True if {@code adUnitId} looks like a real MAX ad unit id.
     *
     * <p>Stricter than "not blank": MAX ad unit ids are exactly 16 alphanumeric characters, and the SDK's
     * selective init silently drops any other value (that unit then serves no ads for the session, and debug
     * builds may even throw). Rejecting it here makes the app treat it as "not configured" instead.
     */
    public static boolean isConfigured(String adUnitId) {
        if (adUnitId == null) return false;
        String id = adUnitId.trim();
        if (id.length() != MAX_AD_UNIT_ID_LENGTH || id.startsWith("YOUR_")) return false;
        for (int i = 0; i < id.length(); i++) {
            char c = id.charAt(i);
            boolean ok = (c >= '0' && c <= '9') || (c >= 'a' && c <= 'z') || (c >= 'A' && c <= 'Z');
            if (!ok) return false;
        }
        return true;
    }

    /** The id exactly as passed to the SDK (trimmed, so a pasted trailing space does not break it). */
    static String clean(String adUnitId) {
        return adUnitId == null ? "" : adUnitId.trim();
    }

    /** Configured (and cleaned) ids, for AppLovinSdkInitializationConfiguration.setAdUnitIds. */
    public static List<String> configuredUnits() {
        List<String> out = new ArrayList<>(ALL.size());
        for (String id : ALL) {
            if (isConfigured(id)) {
                String clean = clean(id);
                if (!out.contains(clean)) out.add(clean);
            }
        }
        return Collections.unmodifiableList(out);
    }
}
