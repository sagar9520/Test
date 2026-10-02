package com.apkglobal.test.onboarding;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.LinearGradient;
import android.graphics.Paint;
import android.graphics.Shader;
import android.util.AttributeSet;
import android.view.View;

import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;

import com.apkglobal.test.R;

/** Onboarding progress: a track with one dot per step, filled up to the current step. */
public class StepIndicatorView extends View {

    private final Paint trackPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint progressPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint dotPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint haloPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint centerPaint = new Paint(Paint.ANTI_ALIAS_FLAG);

    private final int trackColor;
    private final int doneColor;
    private final int gradientStart;
    private final int gradientEnd;
    private final float dotRadius;
    private final float currentRadius;
    private final float haloRadius;
    private final float centerRadius;

    private int totalSteps = ScreenSpec.TOTAL_STEPS;
    private int currentStep = 1;

    public StepIndicatorView(Context context) {
        this(context, null);
    }

    public StepIndicatorView(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public StepIndicatorView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        float density = getResources().getDisplayMetrics().density;
        trackColor = ContextCompat.getColor(context, R.color.progress_track);
        doneColor = ContextCompat.getColor(context, R.color.brand_teal);
        gradientStart = ContextCompat.getColor(context, R.color.brand_teal_start);
        gradientEnd = ContextCompat.getColor(context, R.color.brand_teal_end);
        dotRadius = 4.5f * density;
        currentRadius = 6f * density;
        haloRadius = 9.5f * density;
        centerRadius = 2.4f * density;

        float lineWidth = 4f * density;
        trackPaint.setColor(trackColor);
        trackPaint.setStrokeWidth(lineWidth);
        trackPaint.setStrokeCap(Paint.Cap.ROUND);
        progressPaint.setStrokeWidth(lineWidth);
        progressPaint.setStrokeCap(Paint.Cap.ROUND);
        haloPaint.setColor(ContextCompat.getColor(context, R.color.brand_teal_halo));
        centerPaint.setColor(ContextCompat.getColor(context, R.color.surface));
        setImportantForAccessibility(IMPORTANT_FOR_ACCESSIBILITY_YES);
    }

    public void setStep(int current, int total) {
        totalSteps = Math.max(1, total);
        currentStep = Math.max(1, Math.min(current, totalSteps));
        setContentDescription(getContext().getString(R.string.cd_step_progress, currentStep, totalSteps));
        invalidate();
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int desiredHeight = (int) Math.ceil(haloRadius * 2) + getPaddingTop() + getPaddingBottom();
        setMeasuredDimension(
                getDefaultSize(getSuggestedMinimumWidth(), widthMeasureSpec),
                resolveSize(desiredHeight, heightMeasureSpec));
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        progressPaint.setShader(new LinearGradient(0, 0, w, 0, gradientStart, gradientEnd, Shader.TileMode.CLAMP));
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        float start = getPaddingLeft() + haloRadius;
        float end = getWidth() - getPaddingRight() - haloRadius;
        float cy = getPaddingTop() + (getHeight() - getPaddingTop() - getPaddingBottom()) / 2f;
        boolean rtl = getLayoutDirection() == LAYOUT_DIRECTION_RTL;

        canvas.drawLine(start, cy, end, cy, trackPaint);
        float currentX = dotX(currentStep - 1, start, end, rtl);
        float firstX = dotX(0, start, end, rtl);
        if (currentStep > 1) {
            canvas.drawLine(firstX, cy, currentX, cy, progressPaint);
        }

        for (int i = 0; i < totalSteps; i++) {
            float x = dotX(i, start, end, rtl);
            if (i < currentStep - 1) {
                dotPaint.setColor(doneColor);
                canvas.drawCircle(x, cy, dotRadius, dotPaint);
            } else if (i == currentStep - 1) {
                canvas.drawCircle(x, cy, haloRadius, haloPaint);
                dotPaint.setColor(doneColor);
                canvas.drawCircle(x, cy, currentRadius, dotPaint);
                canvas.drawCircle(x, cy, centerRadius, centerPaint);
            } else {
                dotPaint.setColor(trackColor);
                canvas.drawCircle(x, cy, dotRadius, dotPaint);
            }
        }
    }

    private float dotX(int index, float start, float end, boolean rtl) {
        if (totalSteps == 1) {
            return rtl ? end : start;
        }
        float step = (end - start) / (totalSteps - 1);
        return rtl ? end - index * step : start + index * step;
    }
}
