package com.apkglobal.test.ads;

import android.app.Activity;
import android.app.Dialog;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.apkglobal.test.R;

/**
 * Full-screen "ad starts in 5…4…3" break shown right before an interstitial, instead of a plain
 * loading spinner. It tells the user an ad is coming (so it never appears by surprise) and gives
 * the interstitial time to finish loading.
 *
 * <pre>
 * interstitial.load();                         // start loading first
 * AdCountdownDialog.show(activity, 5, () -> {   // then show the countdown
 *     if (interstitial.isReady()) interstitial.show(activity, next); else next.run();
 * }).setOnRemoveAds(() -> openPremium());      // optional "Remove ads" link
 * </pre>
 */
public class AdCountdownDialog extends Dialog {

    public interface Listener {
        /** The countdown reached 0 and the dialog is already dismissed: show the ad now. */
        void onCountdownFinished();
    }

    public static final int DEFAULT_SECONDS = 5;

    private final int seconds;
    private final Listener listener;
    @Nullable private Runnable onRemoveAds;
    private CountdownRingView ring;
    private TextView title;
    private boolean done;

    private AdCountdownDialog(@NonNull Activity activity, int seconds, @NonNull Listener listener) {
        super(activity, R.style.Theme_AdCountdown);
        this.seconds = Math.max(1, seconds);
        this.listener = listener;
    }

    /**
     * Shows the countdown. If the activity is already finishing, the listener runs right away so
     * the app flow never gets stuck.
     */
    @NonNull
    public static AdCountdownDialog show(@NonNull Activity activity, int seconds, @NonNull Listener listener) {
        AdCountdownDialog d = new AdCountdownDialog(activity, seconds, listener);
        if (activity.isFinishing() || activity.isDestroyed()) {
            d.done = true;
            listener.onCountdownFinished();
            return d;
        }
        d.show();
        return d;
    }

    /** Optional: shows a "Remove ads" link that closes the countdown and runs {@code action}. */
    @NonNull
    public AdCountdownDialog setOnRemoveAds(@Nullable Runnable action) {
        onRemoveAds = action;
        View link = findViewById(R.id.countdown_remove_ads);
        if (link != null) link.setVisibility(action != null ? View.VISIBLE : View.GONE);
        return this;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.dialog_ad_countdown);
        setCancelable(false);
        setCanceledOnTouchOutside(false);

        Window w = getWindow();
        if (w != null) {
            w.setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
            w.setLayout(WindowManager.LayoutParams.MATCH_PARENT, WindowManager.LayoutParams.MATCH_PARENT);
        }

        ring = findViewById(R.id.countdown_ring);
        title = findViewById(R.id.countdown_title);
        View removeAds = findViewById(R.id.countdown_remove_ads);
        removeAds.setVisibility(onRemoveAds != null ? View.VISIBLE : View.GONE);
        removeAds.setOnClickListener(v -> {
            if (done) return;
            done = true;
            ring.cancel();
            safeDismiss();
            if (onRemoveAds != null) onRemoveAds.run();
        });

        // Card pops in softly.
        View card = findViewById(R.id.countdown_card);
        card.setAlpha(0f);
        card.setScaleX(0.92f);
        card.setScaleY(0.92f);
        card.animate().alpha(1f).scaleX(1f).scaleY(1f).setDuration(220).start();
    }

    @Override
    protected void onStart() {
        super.onStart();
        if (done) return;
        ring.start(seconds * 1000L, new CountdownRingView.Callback() {
            @Override
            public void onTick(int secondsLeft) {
                String text = getContext().getResources().getQuantityString(R.plurals.countdown_title, secondsLeft, secondsLeft);
                title.setText(text);
            }

            @Override
            public void onFinish() {
                finishCountdown();
            }
        });
    }

    @Override
    protected void onStop() {
        // App went to background: stop the ring. It starts again from the full time when the user comes back.
        if (ring != null) ring.cancel();
        super.onStop();
    }

    private void finishCountdown() {
        if (done) return;
        done = true;
        safeDismiss();
        listener.onCountdownFinished();
    }

    private void safeDismiss() {
        try {
            if (isShowing()) dismiss();
        } catch (IllegalArgumentException ignored) {
            // Window already detached (activity destroyed): nothing to dismiss.
        }
    }
}
