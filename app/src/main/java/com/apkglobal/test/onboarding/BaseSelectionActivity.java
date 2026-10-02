package com.apkglobal.test.onboarding;

import android.content.Intent;
import android.os.Bundle;
import android.os.SystemClock;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.AccessibilityDelegateCompat;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.core.view.WindowInsetsControllerCompat;
import androidx.core.view.accessibility.AccessibilityNodeInfoCompat;

import com.apkglobal.test.MainActivity;
import com.apkglobal.test.R;
import com.apkglobal.test.ads.BannerAdController;
import com.apkglobal.test.ads.NativeAdController;
import com.google.android.gms.ads.AdSize;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Shared implementation of the 5 onboarding selection screens.
 *
 * <p>Layout of the scrolling list (top to bottom): title, subtitle, then the option cards with
 * the ads inserted where the screen's {@link ScreenSpec} says. The native ad is always followed
 * directly by the Continue button. A fixed banner sits at the bottom of every screen.
 */
public abstract class BaseSelectionActivity extends AppCompatActivity {

    private static final String STATE_SELECTED = "selected_key";
    private static final long CONTINUE_DEBOUNCE_MS = 800;

    private final Map<String, View> optionViews = new LinkedHashMap<>();
    private ScreenSpec spec;
    private String selectedKey;
    private AdSize bannerSize;
    private long lastContinueClick;

    /** Describes this screen: texts, options, ad positions and the next screen. */
    protected abstract ScreenSpec createSpec();

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        WindowCompat.setDecorFitsSystemWindows(getWindow(), false);
        setContentView(R.layout.activity_selection);
        spec = createSpec();

        applySystemBars();
        bindHeader();

        String saved = savedInstanceState != null ? savedInstanceState.getString(STATE_SELECTED) : null;
        if (saved == null) {
            saved = OnboardingPrefs.get(this, spec.prefKey, null);
        }
        selectedKey = isKnownOption(saved) ? saved : spec.options.get(0).key;

        bannerSize = BannerAdController.adaptiveSize(this,
                getResources().getDimensionPixelSize(R.dimen.ad_padding_h));
        buildList();
        select(selectedKey);

        BannerAdController.attach(this, findViewById(R.id.bottom_banner_slot),
                getString(R.string.ad_unit_banner), bannerSize);
    }

    @Override
    protected void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putString(STATE_SELECTED, selectedKey);
    }

    private void applySystemBars() {
        WindowInsetsControllerCompat controller =
                WindowCompat.getInsetsController(getWindow(), getWindow().getDecorView());
        controller.setAppearanceLightStatusBars(true);
        controller.setAppearanceLightNavigationBars(true);

        View root = findViewById(R.id.root);
        ViewCompat.setOnApplyWindowInsetsListener(root, (v, insets) -> {
            Insets bars = insets.getInsets(
                    WindowInsetsCompat.Type.systemBars() | WindowInsetsCompat.Type.displayCutout());
            v.setPadding(bars.left, bars.top, bars.right, bars.bottom);
            return WindowInsetsCompat.CONSUMED;
        });
    }

    private void bindHeader() {
        findViewById(R.id.btn_back).setOnClickListener(v -> getOnBackPressedDispatcher().onBackPressed());
        StepIndicatorView indicator = findViewById(R.id.step_indicator);
        indicator.setStep(spec.step, ScreenSpec.TOTAL_STEPS);
        TextView counter = findViewById(R.id.step_counter);
        counter.setText(getString(R.string.step_counter, spec.step, ScreenSpec.TOTAL_STEPS));
        TextView title = findViewById(R.id.screen_title);
        title.setText(spec.title);
        TextView subtitle = findViewById(R.id.screen_subtitle);
        subtitle.setText(spec.subtitle);
    }

    private void buildList() {
        LinearLayout container = findViewById(R.id.options_container);
        LayoutInflater inflater = getLayoutInflater();

        if (spec.nativeAdAfterIndex == ScreenSpec.TOP) {
            addNativeAdWithContinue(container, inflater);
        }
        for (int i = 0; i < spec.options.size(); i++) {
            addOption(container, inflater, spec.options.get(i));
            if (spec.bannerAfterIndexes.contains(i)) {
                addInlineBanner(container, inflater);
            }
            if (spec.nativeAdAfterIndex == i) {
                addNativeAdWithContinue(container, inflater);
            }
        }
    }

    private void addOption(LinearLayout container, LayoutInflater inflater, OptionItem item) {
        View card = inflater.inflate(R.layout.item_option, container, false);
        ImageView thumb = card.findViewById(R.id.option_thumb);
        TextView title = card.findViewById(R.id.option_title);
        TextView description = card.findViewById(R.id.option_subtitle);
        thumb.setImageResource(item.thumbnail);
        title.setText(item.title);
        description.setText(item.description);

        card.setOnClickListener(v -> select(item.key));
        ViewCompat.setAccessibilityDelegate(card, new AccessibilityDelegateCompat() {
            @Override
            public void onInitializeAccessibilityNodeInfo(@NonNull View host,
                                                          @NonNull AccessibilityNodeInfoCompat info) {
                super.onInitializeAccessibilityNodeInfo(host, info);
                info.setClassName(RadioButton.class.getName());
                info.setCheckable(true);
                info.setChecked(host.isSelected());
            }
        });

        container.addView(card, marginParams(optionInset(), topSpacing(container)));
        optionViews.put(item.key, card);
    }

    private void addNativeAdWithContinue(LinearLayout container, LayoutInflater inflater) {
        FrameLayout nativeSlot = (FrameLayout) inflater.inflate(R.layout.view_native_ad_slot, container, false);
        container.addView(nativeSlot, marginParams(0, topSpacing(container)));
        NativeAdController.attach(this, nativeSlot, getString(R.string.ad_unit_native));

        View continueButton = inflater.inflate(R.layout.view_continue_button, container, false);
        continueButton.setOnClickListener(v -> onContinue());
        LinearLayout.LayoutParams params = marginParams(optionInset(), dp(12));
        params.height = getResources().getDimensionPixelSize(R.dimen.continue_height);
        params.bottomMargin = dp(4);
        container.addView(continueButton, params);
    }

    private void addInlineBanner(LinearLayout container, LayoutInflater inflater) {
        FrameLayout bannerSlot = (FrameLayout) inflater.inflate(R.layout.view_banner_ad_slot, container, false);
        container.addView(bannerSlot, marginParams(0, topSpacing(container)));
        BannerAdController.attach(this, bannerSlot, getString(R.string.ad_unit_banner), bannerSize);
    }

    private void select(String key) {
        selectedKey = key;
        for (Map.Entry<String, View> entry : optionViews.entrySet()) {
            boolean selected = entry.getKey().equals(key);
            entry.getValue().setSelected(selected);
        }
    }

    private void onContinue() {
        long now = SystemClock.elapsedRealtime();
        if (now - lastContinueClick < CONTINUE_DEBOUNCE_MS) {
            return; // ignore double taps so the next screen isn't opened twice
        }
        lastContinueClick = now;
        OnboardingPrefs.put(this, spec.prefKey, selectedKey);
        Intent intent = new Intent(this, spec.next);
        if (spec.next == MainActivity.class) {
            // Onboarding finished: don't keep the selection screens in the back stack.
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        }
        startActivity(intent);
    }

    private boolean isKnownOption(@Nullable String key) {
        if (key == null) {
            return false;
        }
        for (OptionItem item : spec.options) {
            if (item.key.equals(key)) {
                return true;
            }
        }
        return false;
    }

    private int topSpacing(ViewGroup container) {
        return container.getChildCount() == 0 ? 0 : getResources().getDimensionPixelSize(R.dimen.option_spacing);
    }

    private int optionInset() {
        return getResources().getDimensionPixelSize(R.dimen.option_inset_h);
    }

    private LinearLayout.LayoutParams marginParams(int horizontal, int top) {
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        params.setMarginStart(horizontal);
        params.setMarginEnd(horizontal);
        params.topMargin = top;
        return params;
    }

    private int dp(int value) {
        return Math.round(value * getResources().getDisplayMetrics().density);
    }
}
