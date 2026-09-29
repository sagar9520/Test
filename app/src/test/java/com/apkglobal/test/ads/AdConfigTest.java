package com.apkglobal.test.ads;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.util.HashMap;
import java.util.Map;

public class AdConfigTest {

    private boolean adsEnabled;
    private long minInterval;
    private int everyN;
    private int maxPerSession;
    private int preloadCount;
    private boolean nativeOnChoice;
    private long cooldown;
    private int skipFirst;

    @Before
    public void saveDefaults() {
        adsEnabled = AdConfig.ADS_ENABLED;
        minInterval = AdConfig.INTERSTITIAL_MIN_INTERVAL_MS;
        everyN = AdConfig.INTERSTITIAL_EVERY_N_ACTIONS;
        maxPerSession = AdConfig.INTERSTITIAL_MAX_PER_SESSION;
        preloadCount = AdConfig.NATIVE_PRELOAD_COUNT;
        nativeOnChoice = AdConfig.NATIVE_ON_CHOICE;
        cooldown = AdConfig.FULLSCREEN_COOLDOWN_MS;
        skipFirst = AdConfig.INTERSTITIAL_SKIP_FIRST_ACTIONS;
    }

    @After
    public void restoreDefaults() {
        AdConfig.ADS_ENABLED = adsEnabled;
        AdConfig.INTERSTITIAL_MIN_INTERVAL_MS = minInterval;
        AdConfig.INTERSTITIAL_EVERY_N_ACTIONS = everyN;
        AdConfig.INTERSTITIAL_MAX_PER_SESSION = maxPerSession;
        AdConfig.NATIVE_PRELOAD_COUNT = preloadCount;
        AdConfig.NATIVE_ON_CHOICE = nativeOnChoice;
        AdConfig.FULLSCREEN_COOLDOWN_MS = cooldown;
        AdConfig.INTERSTITIAL_SKIP_FIRST_ACTIONS = skipFirst;
    }

    @Test
    public void appliesValidValues() {
        Map<String, String> m = new HashMap<>();
        m.put("interstitial_min_interval_ms", "45000");
        m.put("interstitial_every_n_actions", "3");
        m.put(" ADS_ENABLED ", "false");
        m.put("native_on_choice", "0");
        AdConfig.applyRemote(m);
        assertEquals(45_000L, AdConfig.INTERSTITIAL_MIN_INTERVAL_MS);
        assertEquals(3, AdConfig.INTERSTITIAL_EVERY_N_ACTIONS);
        assertFalse(AdConfig.ADS_ENABLED);
        assertFalse(AdConfig.NATIVE_ON_CHOICE);
    }

    @Test
    public void ignoresBadValues() {
        Map<String, String> m = new HashMap<>();
        m.put("interstitial_min_interval_ms", "soon");
        m.put("interstitial_max_per_session", "-1");
        m.put("ads_enabled", "maybe");
        m.put("native_preload_count", "50");
        m.put("fullscreen_cooldown_ms", null);
        m.put("unknown_key", "1");
        m.put(null, "1");
        AdConfig.applyRemote(m);
        assertEquals(minInterval, AdConfig.INTERSTITIAL_MIN_INTERVAL_MS);
        assertEquals(maxPerSession, AdConfig.INTERSTITIAL_MAX_PER_SESSION);
        assertEquals(adsEnabled, AdConfig.ADS_ENABLED);
        assertEquals(preloadCount, AdConfig.NATIVE_PRELOAD_COUNT);
        assertEquals(cooldown, AdConfig.FULLSCREEN_COOLDOWN_MS);
        AdConfig.applyRemote(null); // no crash
    }

    @Test
    public void interstitialRulesSnapshotMatchesFields() {
        AdConfig.INTERSTITIAL_SKIP_FIRST_ACTIONS = 2;
        AdConfig.INTERSTITIAL_EVERY_N_ACTIONS = 4;
        AdConfig.INTERSTITIAL_MIN_INTERVAL_MS = 12_345L;
        AdConfig.INTERSTITIAL_MAX_PER_SESSION = 7;
        AdConfig.FULLSCREEN_COOLDOWN_MS = 999L;
        FrequencyCapper.Rules r = AdConfig.interstitialRules();
        assertEquals(2, r.skipFirstActions);
        assertEquals(4, r.everyNActions);
        assertEquals(12_345L, r.minIntervalMs);
        assertEquals(7, r.maxPerSession);
        assertEquals(999L, r.fullscreenCooldownMs);
    }

    @Test
    public void shippedDefaultsAreTheRecommendedPacing() {
        FrequencyCapper.Rules r = AdConfig.interstitialRules();
        assertEquals(1, r.skipFirstActions);
        assertEquals(2, r.everyNActions);
        assertEquals(30_000L, r.minIntervalMs);
        assertEquals(6, r.maxPerSession);
        assertEquals(15_000L, r.fullscreenCooldownMs);
        assertTrue(AdConfig.GRANT_REWARD_IF_NO_AD);
    }
}
