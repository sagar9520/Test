package com.apkglobal.test.onboarding;

import com.apkglobal.test.R;

/**
 * Step 2/5 - Select Scan Mode.
 * Ads: native ad (+ Continue) below "Gray Scale", banner below "Thermal View", banner at the bottom.
 */
public class ScanModeActivity extends BaseSelectionActivity {

    @Override
    protected ScreenSpec createSpec() {
        return new ScreenSpec.Builder(2, R.string.mode_title, R.string.mode_subtitle,
                OnboardingPrefs.KEY_SCAN_MODE)
                .option("classic_xray", R.drawable.thumb_mode_classic_xray, R.string.mode_classic_xray, R.string.mode_classic_xray_desc)
                .option("green_scan", R.drawable.thumb_mode_green_scan, R.string.mode_green_scan, R.string.mode_green_scan_desc)
                .option("blue_scan", R.drawable.thumb_mode_blue_scan, R.string.mode_blue_scan, R.string.mode_blue_scan_desc)
                .option("gray_scale", R.drawable.thumb_mode_gray_scale, R.string.mode_gray_scale, R.string.mode_gray_scale_desc)
                .option("neon_scan", R.drawable.thumb_mode_neon_scan, R.string.mode_neon_scan, R.string.mode_neon_scan_desc)
                .option("thermal_view", R.drawable.thumb_mode_thermal_view, R.string.mode_thermal_view, R.string.mode_thermal_view_desc)
                .option("edge_highlight", R.drawable.thumb_mode_edge_highlight, R.string.mode_edge_highlight, R.string.mode_edge_highlight_desc)
                .option("deep_scan", R.drawable.thumb_mode_deep_scan, R.string.mode_deep_scan, R.string.mode_deep_scan_desc)
                .nativeAdAfter("gray_scale")
                .bannerAfter("thermal_view")
                .next(DisplayStyleActivity.class)
                .build();
    }
}
