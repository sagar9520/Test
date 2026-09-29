package com.apkglobal.test.ads;

import java.util.Locale;
import java.util.Map;

/**
 * Ad tunables. Defaults are the recommended starting point; every field can be changed remotely
 * (Firebase Remote Config or your own JSON) through {@link #applyRemote(Map)} to A/B test pacing
 * without shipping an update.
 */
public final class AdConfig {

    /** Global kill switch (e.g. for a policy review or a user who bought "remove ads"). */
    public static volatile boolean ADS_ENABLED = true;

    // Interstitial pacing (see FrequencyCapper for why pacing raises eCPM).
    /** Never on the very first tap of a session: first impressions decide if the user stays. */
    public static volatile int INTERSTITIAL_SKIP_FIRST_ACTIONS = 1;
    /** At most every 2nd navigation tap. */
    public static volatile int INTERSTITIAL_EVERY_N_ACTIONS = 2;
    /** Minimum gap after the previous interstitial. */
    public static volatile long INTERSTITIAL_MIN_INTERVAL_MS = 30_000L;
    public static volatile int INTERSTITIAL_MAX_PER_SESSION = 6;
    /** Minimum gap after ANY fullscreen ad (rewarded / app-open) closes: no ad right after an ad. */
    public static volatile long FULLSCREEN_COOLDOWN_MS = 15_000L;

    // App open
    public static volatile boolean APP_OPEN_ENABLED = true;
    /** Skip the first-ever launch: an ad before the user has seen the app kills day-1 retention. */
    public static volatile int APP_OPEN_MIN_LAUNCHES = 2;
    /** Only after >= 30 s in background (not when the user just glanced at a notification). */
    public static volatile long APP_OPEN_MIN_BACKGROUND_MS = 30_000L;
    /** At cold start, show only if the ad is loaded within this window; never make the user wait. */
    public static volatile long APP_OPEN_COLD_START_WINDOW_MS = 4_000L;
    /** Background longer than this starts a new ad session (resets interstitial counters). */
    public static volatile long SESSION_TIMEOUT_MS = 30L * 60_000L;

    // Inline units
    public static volatile boolean NATIVE_ON_HOME = true;
    public static volatile boolean NATIVE_ON_QUESTION = true;
    public static volatile boolean NATIVE_ON_CHOICE = true;
    public static volatile boolean NATIVE_ON_READY = true;
    public static volatile boolean BANNER_ON_HOME = true;
    public static volatile boolean BANNER_ON_QUESTION = true;
    public static volatile boolean BANNER_ON_CHOICE = true;
    /** Natives kept ready so a screen renders instantly (an ad seen at once is a viewable impression). */
    public static volatile int NATIVE_PRELOAD_COUNT = 2;
    /** Collapse a native slot if nothing is available by then (no empty boxes). */
    public static volatile long NATIVE_SLOT_TIMEOUT_MS = 6_000L;

    // Rewarded
    /** Never punish the user when no ad is available: they still get what they asked for. */
    public static volatile boolean GRANT_REWARD_IF_NO_AD = true;

    /** Upper bound for NATIVE_PRELOAD_COUNT: pooled natives expire, so a big pool wastes fill. */
    static final int NATIVE_PRELOAD_MAX = 5;

    private AdConfig() {
    }

    /**
     * Applies remote values. Keys are the lower_snake field names, e.g. {@code "interstitial_min_interval_ms"}.
     * Unknown keys, unparsable values and negative numbers are ignored (the current value stays).
     * Booleans accept true/false/1/0.
     */
    public static void applyRemote(Map<String, String> values) {
        if (values == null) return;
        for (Map.Entry<String, String> e : values.entrySet()) {
            if (e.getKey() == null || e.getValue() == null) continue;
            apply(e.getKey().trim().toLowerCase(Locale.ROOT), e.getValue().trim());
        }
    }

    // Explicit switch instead of reflection: keeps working if R8 minification is turned on later.
    private static void apply(String key, String v) {
        Boolean b;
        Long n;
        switch (key) {
            case "ads_enabled":
                if ((b = parseBool(v)) != null) ADS_ENABLED = b;
                break;
            case "interstitial_skip_first_actions":
                if ((n = parseCount(v)) != null) INTERSTITIAL_SKIP_FIRST_ACTIONS = n.intValue();
                break;
            case "interstitial_every_n_actions":
                if ((n = parseCount(v)) != null) INTERSTITIAL_EVERY_N_ACTIONS = n.intValue();
                break;
            case "interstitial_min_interval_ms":
                if ((n = parseMillis(v)) != null) INTERSTITIAL_MIN_INTERVAL_MS = n;
                break;
            case "interstitial_max_per_session":
                if ((n = parseCount(v)) != null) INTERSTITIAL_MAX_PER_SESSION = n.intValue();
                break;
            case "fullscreen_cooldown_ms":
                if ((n = parseMillis(v)) != null) FULLSCREEN_COOLDOWN_MS = n;
                break;
            case "app_open_enabled":
                if ((b = parseBool(v)) != null) APP_OPEN_ENABLED = b;
                break;
            case "app_open_min_launches":
                if ((n = parseCount(v)) != null) APP_OPEN_MIN_LAUNCHES = n.intValue();
                break;
            case "app_open_min_background_ms":
                if ((n = parseMillis(v)) != null) APP_OPEN_MIN_BACKGROUND_MS = n;
                break;
            case "app_open_cold_start_window_ms":
                if ((n = parseMillis(v)) != null) APP_OPEN_COLD_START_WINDOW_MS = n;
                break;
            case "session_timeout_ms":
                if ((n = parseMillis(v)) != null) SESSION_TIMEOUT_MS = n;
                break;
            case "native_on_home":
                if ((b = parseBool(v)) != null) NATIVE_ON_HOME = b;
                break;
            case "native_on_question":
                if ((b = parseBool(v)) != null) NATIVE_ON_QUESTION = b;
                break;
            case "native_on_choice":
                if ((b = parseBool(v)) != null) NATIVE_ON_CHOICE = b;
                break;
            case "native_on_ready":
                if ((b = parseBool(v)) != null) NATIVE_ON_READY = b;
                break;
            case "banner_on_home":
                if ((b = parseBool(v)) != null) BANNER_ON_HOME = b;
                break;
            case "banner_on_question":
                if ((b = parseBool(v)) != null) BANNER_ON_QUESTION = b;
                break;
            case "banner_on_choice":
                if ((b = parseBool(v)) != null) BANNER_ON_CHOICE = b;
                break;
            case "native_preload_count":
                if ((n = parseCount(v)) != null && n <= NATIVE_PRELOAD_MAX) NATIVE_PRELOAD_COUNT = n.intValue();
                break;
            case "native_slot_timeout_ms":
                if ((n = parseMillis(v)) != null) NATIVE_SLOT_TIMEOUT_MS = n;
                break;
            case "grant_reward_if_no_ad":
                if ((b = parseBool(v)) != null) GRANT_REWARD_IF_NO_AD = b;
                break;
            default:
                // Unknown key: ignore (lets the server send keys for newer app versions).
                break;
        }
    }

    /** Snapshot of the interstitial pacing fields. */
    public static FrequencyCapper.Rules interstitialRules() {
        return new FrequencyCapper.Rules(
                INTERSTITIAL_SKIP_FIRST_ACTIONS,
                INTERSTITIAL_EVERY_N_ACTIONS,
                INTERSTITIAL_MIN_INTERVAL_MS,
                INTERSTITIAL_MAX_PER_SESSION,
                FULLSCREEN_COOLDOWN_MS);
    }

    private static Boolean parseBool(String v) {
        if ("true".equalsIgnoreCase(v) || "1".equals(v)) return Boolean.TRUE;
        if ("false".equalsIgnoreCase(v) || "0".equals(v)) return Boolean.FALSE;
        return null;
    }

    private static Long parseCount(String v) {
        Long n = parseMillis(v);
        return (n == null || n > Integer.MAX_VALUE) ? null : n;
    }

    private static Long parseMillis(String v) {
        try {
            long n = Long.parseLong(v);
            return n < 0 ? null : n;
        } catch (NumberFormatException ignored) {
            return null;
        }
    }
}
