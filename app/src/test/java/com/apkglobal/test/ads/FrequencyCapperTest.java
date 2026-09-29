package com.apkglobal.test.ads;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Before;
import org.junit.Test;

public class FrequencyCapperTest {

    /** Manually advanced clock. */
    private static final class FakeClock implements FrequencyCapper.Clock {
        long now = 1_000_000L;

        @Override
        public long nowMs() {
            return now;
        }

        void advance(long ms) {
            now += ms;
        }
    }

    private static final long MIN_INTERVAL = 30_000L;
    private static final long COOLDOWN = 15_000L;

    /** The shipped defaults: skip 1, every 2nd, 30 s apart, 6 per session, 15 s after any fullscreen. */
    private static final FrequencyCapper.Rules DEFAULTS =
            new FrequencyCapper.Rules(1, 2, MIN_INTERVAL, 6, COOLDOWN);

    /** Only the rule under test is active. */
    private static FrequencyCapper.Rules open() {
        return new FrequencyCapper.Rules(0, 0, 0, Integer.MAX_VALUE, 0);
    }

    private FakeClock clock;
    private FrequencyCapper capper;

    @Before
    public void setUp() {
        clock = new FakeClock();
        capper = new FrequencyCapper(clock);
    }

    /** recordAction() then ask, like InterstitialController does on every tap. */
    private boolean tap(FrequencyCapper.Rules rules) {
        capper.recordAction();
        return capper.canShowInterstitial(rules);
    }

    @Test
    public void nullRulesNeverAllow() {
        capper.recordAction();
        assertFalse(capper.canShowInterstitial(null));
    }

    @Test(expected = IllegalArgumentException.class)
    public void nullClockRejected() {
        new FrequencyCapper(null);
    }

    @Test
    public void skipFirstActions() {
        FrequencyCapper.Rules rules = new FrequencyCapper.Rules(3, 0, 0, 100, 0);
        assertFalse("before any action", capper.canShowInterstitial(rules));
        assertFalse(tap(rules)); // 1
        assertFalse(tap(rules)); // 2
        assertFalse(tap(rules)); // 3 == skipFirst -> still skipped
        assertTrue(tap(rules));  // 4
        assertEquals(4, capper.actionsThisSession());
    }

    @Test
    public void skipFirstZeroAllowsFirstAction() {
        FrequencyCapper.Rules rules = open();
        assertFalse("no action yet", capper.canShowInterstitial(rules));
        assertTrue(tap(rules));
    }

    @Test
    public void everyNActions() {
        FrequencyCapper.Rules rules = new FrequencyCapper.Rules(0, 3, 0, 100, 0);
        assertFalse(tap(rules)); // since=1
        assertFalse(tap(rules)); // since=2
        assertTrue(tap(rules));  // since=3
        capper.onInterstitialShown();
        assertFalse(tap(rules)); // since=1
        assertFalse(tap(rules)); // since=2
        assertTrue(tap(rules));  // since=3
    }

    @Test
    public void everyNKeepsEligibilityWhenNoAdWasShown() {
        FrequencyCapper.Rules rules = new FrequencyCapper.Rules(0, 2, 0, 100, 0);
        assertFalse(tap(rules));
        assertTrue(tap(rules));
        // No ad was ready, so nothing was shown: the next tap is still eligible.
        assertTrue(tap(rules));
    }

    @Test
    public void everyNZeroOrOneMeansEveryEligibleAction() {
        for (int n : new int[]{0, 1}) {
            setUp();
            FrequencyCapper.Rules rules = new FrequencyCapper.Rules(0, n, 0, 100, 0);
            for (int i = 0; i < 5; i++) {
                assertTrue("n=" + n + " tap " + i, tap(rules));
                capper.onInterstitialShown();
            }
        }
    }

    @Test
    public void minInterval() {
        FrequencyCapper.Rules rules = new FrequencyCapper.Rules(0, 0, MIN_INTERVAL, 100, 0);
        assertTrue("no previous interstitial -> interval does not apply", tap(rules));
        capper.onInterstitialShown();
        clock.advance(MIN_INTERVAL - 1);
        assertFalse(tap(rules));
        clock.advance(1);
        assertTrue("exactly minInterval later", tap(rules));
    }

    @Test
    public void sessionCap() {
        FrequencyCapper.Rules rules = new FrequencyCapper.Rules(0, 0, 0, 2, 0);
        assertTrue(tap(rules));
        capper.onInterstitialShown();
        assertTrue(tap(rules));
        capper.onInterstitialShown();
        assertEquals(2, capper.shownThisSession());
        assertFalse(tap(rules));
        clock.advance(24L * 3600_000L);
        assertFalse("cap is per session, not time based", tap(rules));
    }

    @Test
    public void maxPerSessionZeroDisablesInterstitials() {
        FrequencyCapper.Rules rules = new FrequencyCapper.Rules(0, 0, 0, 0, 0);
        for (int i = 0; i < 5; i++) {
            assertFalse(tap(rules));
        }
    }

    @Test
    public void fullscreenCooldown() {
        FrequencyCapper.Rules rules = new FrequencyCapper.Rules(0, 0, 0, 100, COOLDOWN);
        assertFalse(capper.isInFullscreenCooldown(COOLDOWN));
        assertTrue("no fullscreen closed yet", tap(rules));

        capper.onFullscreenDismissed(); // e.g. a rewarded ad was closed
        assertTrue(capper.isInFullscreenCooldown(COOLDOWN));
        assertFalse(tap(rules));

        clock.advance(COOLDOWN - 1);
        assertTrue(capper.isInFullscreenCooldown(COOLDOWN));
        assertFalse(tap(rules));

        clock.advance(1);
        assertFalse(capper.isInFullscreenCooldown(COOLDOWN));
        assertTrue(tap(rules));
    }

    @Test
    public void cooldownDoesNotCountAsInterstitial() {
        capper.onFullscreenDismissed();
        assertEquals(0, capper.shownThisSession());
        FrequencyCapper.Rules rules = new FrequencyCapper.Rules(0, 0, MIN_INTERVAL, 100, 0);
        assertTrue("min interval only applies after an interstitial", tap(rules));
    }

    @Test
    public void onInterstitialShownResetsActionsSinceLast() {
        FrequencyCapper.Rules rules = new FrequencyCapper.Rules(0, 2, 0, 100, 0);
        tap(rules);
        tap(rules);
        tap(rules);
        capper.onInterstitialShown();
        assertFalse(tap(rules));
        assertTrue(tap(rules));
        assertEquals(5, capper.actionsThisSession());
        assertEquals(1, capper.shownThisSession());
    }

    @Test
    public void resetSession() {
        FrequencyCapper.Rules rules = new FrequencyCapper.Rules(1, 0, 0, 1, 0);
        assertFalse(tap(rules));
        assertTrue(tap(rules));
        capper.onInterstitialShown();
        assertFalse("session cap reached", tap(rules));

        capper.resetSession();
        assertEquals(0, capper.actionsThisSession());
        assertEquals(0, capper.shownThisSession());
        assertFalse("skip-first applies again in the new session", tap(rules));
        assertTrue(tap(rules));
    }

    @Test
    public void resetSessionKeepsTimeBasedRules() {
        FrequencyCapper.Rules rules = new FrequencyCapper.Rules(0, 0, MIN_INTERVAL, 100, COOLDOWN);
        assertTrue(tap(rules));
        capper.onInterstitialShown();
        capper.onFullscreenDismissed();
        capper.resetSession();
        assertFalse(tap(rules));
        assertTrue(capper.isInFullscreenCooldown(COOLDOWN));
        clock.advance(MIN_INTERVAL);
        assertTrue(tap(rules));
    }

    @Test
    public void negativeRuleValuesAreTreatedAsZero() {
        FrequencyCapper.Rules rules = new FrequencyCapper.Rules(-5, -2, -1000L, -1, -1000L);
        assertEquals(0, rules.skipFirstActions);
        assertEquals(0, rules.everyNActions);
        assertEquals(0L, rules.minIntervalMs);
        assertEquals(0, rules.maxPerSession);
        assertEquals(0L, rules.fullscreenCooldownMs);
        // maxPerSession 0 -> never.
        assertFalse(tap(rules));

        FrequencyCapper.Rules permissive = new FrequencyCapper.Rules(-5, -2, -1000L, 10, -1000L);
        capper.onInterstitialShown();
        capper.onFullscreenDismissed();
        assertTrue("negative skip/everyN/interval/cooldown behave like 0", tap(permissive));
        assertFalse(capper.isInFullscreenCooldown(-1L));
        assertFalse(capper.isInFullscreenCooldown(0L));
    }

    @Test
    public void defaultRulesRealisticFunnel() {
        // Tap 1: skipped (first tap of the session).
        assertFalse(tap(DEFAULTS));
        // Tap 2: eligible.
        assertTrue(tap(DEFAULTS));
        capper.onInterstitialShown();
        capper.onFullscreenDismissed();

        // User taps quickly through the next screens: every-2nd rule + 30 s interval hold ads back.
        clock.advance(5_000);
        assertFalse(tap(DEFAULTS));
        clock.advance(5_000);
        assertFalse("2 taps but only 10 s since the last ad", tap(DEFAULTS));
        clock.advance(25_000);
        assertTrue("35 s later and 3 taps since", tap(DEFAULTS));
    }

    @Test
    public void defaultRulesAtMostSixPerSession() {
        int shown = 0;
        for (int i = 0; i < 100; i++) {
            clock.advance(MIN_INTERVAL);
            if (tap(DEFAULTS)) {
                capper.onInterstitialShown();
                capper.onFullscreenDismissed();
                shown++;
            }
        }
        assertEquals(6, shown);
        assertEquals(6, capper.shownThisSession());
    }

    @Test
    public void canShowDoesNotMutateState() {
        FrequencyCapper.Rules rules = open();
        capper.recordAction();
        assertTrue(capper.canShowInterstitial(rules));
        assertTrue(capper.canShowInterstitial(rules));
        assertEquals(1, capper.actionsThisSession());
        assertEquals(0, capper.shownThisSession());
    }
}
