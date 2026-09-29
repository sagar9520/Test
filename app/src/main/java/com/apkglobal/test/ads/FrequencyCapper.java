package com.apkglobal.test.ads;

/**
 * Pacing rules for interstitials (and cooldown after any fullscreen ad).
 *
 * <p>Pure Java on purpose (no android.* imports) so it can be unit-tested on the JVM with a fake clock.
 *
 * <p>Why pacing raises eCPM: an interstitial on every tap trains users to tap blindly / close fast and
 * produces short, accidental views. Networks measure that (low view time, no post-click installs) and bid
 * less for the whole app. Fewer, well-timed interstitials sell at a higher price and keep users in the app.
 */
public final class FrequencyCapper {

    /** Time source in milliseconds. Use a monotonic clock in production (SystemClock.elapsedRealtime). */
    public interface Clock {
        long nowMs();
    }

    /** Immutable snapshot of the pacing configuration. Negative values are treated as 0. */
    public static final class Rules {
        /** No interstitial while actionsThisSession &lt;= this value. */
        public final int skipFirstActions;
        /** Minimum number of actions since the previous interstitial. &lt;= 1 means every eligible action. */
        public final int everyNActions;
        /** Minimum gap after the previous interstitial was shown. */
        public final long minIntervalMs;
        /** Session cap. 0 disables interstitials. */
        public final int maxPerSession;
        /** Minimum gap after ANY fullscreen ad (interstitial, rewarded, app-open) was dismissed. */
        public final long fullscreenCooldownMs;

        public Rules(int skipFirstActions, int everyNActions, long minIntervalMs, int maxPerSession,
                     long fullscreenCooldownMs) {
            this.skipFirstActions = Math.max(0, skipFirstActions);
            this.everyNActions = Math.max(0, everyNActions);
            this.minIntervalMs = Math.max(0L, minIntervalMs);
            this.maxPerSession = Math.max(0, maxPerSession);
            this.fullscreenCooldownMs = Math.max(0L, fullscreenCooldownMs);
        }

        @Override
        public String toString() {
            return "Rules{skipFirst=" + skipFirstActions + ", everyN=" + everyNActions
                    + ", minIntervalMs=" + minIntervalMs + ", maxPerSession=" + maxPerSession
                    + ", fullscreenCooldownMs=" + fullscreenCooldownMs + '}';
        }
    }

    private final Clock clock;

    private int actionsThisSession;
    private int actionsSinceLastInterstitial;
    private int shownThisSession;

    // Timestamps survive resetSession(): they are real-time rules, not per-session counters.
    private boolean hasShownInterstitial;
    private long lastInterstitialAt;
    private boolean hasFullscreenClosed;
    private long lastFullscreenClosedAt;

    public FrequencyCapper(Clock clock) {
        if (clock == null) throw new IllegalArgumentException("clock == null");
        this.clock = clock;
    }

    /** A navigation tap happened. Call before {@link #canShowInterstitial(Rules)}. */
    public synchronized void recordAction() {
        actionsThisSession++;
        actionsSinceLastInterstitial++;
    }

    /** Whether the pacing rules allow an interstitial right now. Does not change state. */
    public synchronized boolean canShowInterstitial(Rules r) {
        if (r == null) return false;
        final long now = clock.nowMs();
        if (actionsThisSession <= r.skipFirstActions) return false;
        if (actionsSinceLastInterstitial < r.everyNActions) return false;
        if (hasShownInterstitial && now - lastInterstitialAt < r.minIntervalMs) return false;
        if (shownThisSession >= r.maxPerSession) return false;
        if (hasFullscreenClosed && now - lastFullscreenClosedAt < r.fullscreenCooldownMs) return false;
        return true;
    }

    /** An interstitial impression happened (call from onAdDisplayed). */
    public synchronized void onInterstitialShown() {
        actionsSinceLastInterstitial = 0;
        lastInterstitialAt = clock.nowMs();
        hasShownInterstitial = true;
        shownThisSession++;
    }

    /** Any fullscreen ad (interstitial / rewarded / app-open) was closed. */
    public synchronized void onFullscreenDismissed() {
        lastFullscreenClosedAt = clock.nowMs();
        hasFullscreenClosed = true;
    }

    /** True while less than {@code cooldownMs} has passed since the last fullscreen ad closed. */
    public synchronized boolean isInFullscreenCooldown(long cooldownMs) {
        return hasFullscreenClosed && clock.nowMs() - lastFullscreenClosedAt < Math.max(0L, cooldownMs);
    }

    /** New session (e.g. app returned after a long time in background): counters restart, timestamps stay. */
    public synchronized void resetSession() {
        actionsThisSession = 0;
        actionsSinceLastInterstitial = 0;
        shownThisSession = 0;
    }

    public synchronized int shownThisSession() {
        return shownThisSession;
    }

    public synchronized int actionsThisSession() {
        return actionsThisSession;
    }
}
