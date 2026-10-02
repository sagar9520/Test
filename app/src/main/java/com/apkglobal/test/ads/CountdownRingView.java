package com.apkglobal.test.ads;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.BlurMaskFilter;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.RectF;
import android.graphics.SweepGradient;
import android.graphics.Typeface;
import android.util.AttributeSet;
import android.view.View;
import android.view.animation.LinearInterpolator;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.core.content.res.ResourcesCompat;

import com.apkglobal.test.R;

/**
 * Circular countdown: a gradient ring that drains clockwise while the remaining seconds count
 * down in the centre. Replaces the usual indeterminate spinner shown before an interstitial.
 */
public class CountdownRingView extends View {

    public interface Callback {
        /** Called once per whole second, starting with the full duration (e.g. 5, 4, 3, 2, 1). */
        void onTick(int secondsLeft);

        void onFinish();
    }

    private final Paint trackPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint arcPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint glowPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint dotPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint numberPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint unitPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final RectF arcRect = new RectF();
    private final Matrix gradientMatrix = new Matrix();
    private final int colorStart;
    private final int colorEnd;
    private final float stroke;

    @Nullable private ValueAnimator animator;
    @Nullable private Callback callback;
    private float remaining = 1f;      // 1 = full ring, 0 = empty
    private int secondsLeft;
    private boolean finished;

    public CountdownRingView(Context context) {
        this(context, null);
    }

    public CountdownRingView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        float dp = getResources().getDisplayMetrics().density;
        stroke = 7 * dp;
        colorStart = ContextCompat.getColor(context, R.color.countdown_ring_start);
        colorEnd = ContextCompat.getColor(context, R.color.countdown_ring_end);

        trackPaint.setStyle(Paint.Style.STROKE);
        trackPaint.setStrokeWidth(stroke);
        trackPaint.setColor(ContextCompat.getColor(context, R.color.countdown_track));

        arcPaint.setStyle(Paint.Style.STROKE);
        arcPaint.setStrokeWidth(stroke);
        arcPaint.setStrokeCap(Paint.Cap.ROUND);

        // Soft halo under the arc. BlurMaskFilter needs a software layer.
        glowPaint.setStyle(Paint.Style.STROKE);
        glowPaint.setStrokeWidth(stroke * 1.6f);
        glowPaint.setStrokeCap(Paint.Cap.ROUND);
        glowPaint.setColor(colorEnd);
        glowPaint.setAlpha(110);
        glowPaint.setMaskFilter(new BlurMaskFilter(9 * dp, BlurMaskFilter.Blur.NORMAL));
        setLayerType(LAYER_TYPE_SOFTWARE, null);

        dotPaint.setColor(Color.WHITE);

        Typeface semibold = ResourcesCompat.getFont(context, R.font.poppins_semibold);
        Typeface regular = ResourcesCompat.getFont(context, R.font.poppins_regular);
        numberPaint.setColor(Color.WHITE);
        numberPaint.setTextAlign(Paint.Align.CENTER);
        numberPaint.setTypeface(semibold);
        unitPaint.setColor(ContextCompat.getColor(context, R.color.countdown_text_secondary));
        unitPaint.setTextAlign(Paint.Align.CENTER);
        unitPaint.setTypeface(regular);
        unitPaint.setLetterSpacing(0.12f);
    }

    /** Starts (or restarts) the countdown. */
    public void start(long durationMs, @NonNull Callback cb) {
        cancel();
        callback = cb;
        finished = false;
        remaining = 1f;
        secondsLeft = (int) Math.ceil(durationMs / 1000f);
        cb.onTick(secondsLeft);
        invalidate();

        ValueAnimator a = ValueAnimator.ofFloat(1f, 0f);
        a.setDuration(durationMs);
        a.setInterpolator(new LinearInterpolator());
        a.addUpdateListener(anim -> {
            remaining = (float) anim.getAnimatedValue();
            int s = (int) Math.ceil(remaining * durationMs / 1000f);
            if (s != secondsLeft && s > 0) {
                secondsLeft = s;
                if (callback != null) callback.onTick(s);
            }
            invalidate();
        });
        a.addListener(new AnimatorListenerAdapter() {
            private boolean cancelled;

            @Override
            public void onAnimationCancel(Animator animation) {
                cancelled = true;
            }

            @Override
            public void onAnimationEnd(Animator animation) {
                if (cancelled || finished) return;
                finished = true;
                Callback c = callback;
                callback = null;
                if (c != null) c.onFinish();
            }
        });
        animator = a;
        a.start();
    }

    /** Stops the countdown without calling {@link Callback#onFinish()}. */
    public void cancel() {
        callback = null;
        if (animator != null) {
            animator.cancel();
            animator = null;
        }
    }

    @Override
    protected void onDetachedFromWindow() {
        cancel();
        super.onDetachedFromWindow();
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        float pad = stroke * 1.8f;
        float size = Math.min(w, h);
        float left = (w - size) / 2f + pad;
        float top = (h - size) / 2f + pad;
        arcRect.set(left, top, left + size - 2 * pad, top + size - 2 * pad);

        SweepGradient g = new SweepGradient(w / 2f, h / 2f, new int[]{colorStart, colorEnd, colorStart}, null);
        gradientMatrix.setRotate(-90, w / 2f, h / 2f);
        g.setLocalMatrix(gradientMatrix);
        arcPaint.setShader(g);

        numberPaint.setTextSize(size * 0.30f);
        unitPaint.setTextSize(size * 0.085f);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        float sweep = 360f * remaining;
        canvas.drawOval(arcRect, trackPaint);
        if (sweep > 0.5f) {
            canvas.drawArc(arcRect, -90, sweep, false, glowPaint);
            canvas.drawArc(arcRect, -90, sweep, false, arcPaint);
            // White dot at the moving end of the arc.
            double rad = Math.toRadians(-90 + sweep);
            float r = arcRect.width() / 2f;
            float cx = arcRect.centerX() + (float) (r * Math.cos(rad));
            float cy = arcRect.centerY() + (float) (r * Math.sin(rad));
            canvas.drawCircle(cx, cy, stroke * 0.42f, dotPaint);
        }

        float cx = getWidth() / 2f;
        float cy = getHeight() / 2f;
        Paint.FontMetrics fm = numberPaint.getFontMetrics();
        float numberBaseline = cy - (fm.ascent + fm.descent) / 2f - unitPaint.getTextSize() * 0.55f;
        canvas.drawText(String.valueOf(Math.max(secondsLeft, 1)), cx, numberBaseline, numberPaint);
        canvas.drawText(getResources().getString(R.string.countdown_unit), cx,
                numberBaseline + fm.descent + unitPaint.getTextSize() * 1.15f, unitPaint);
    }
}
