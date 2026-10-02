package com.apkglobal.test.onboarding;

import com.apkglobal.test.R;

/**
 * Step 3/5 - Select Display Style.
 * Ads: native ad (+ Continue) above "Heat Map Style", banner at the bottom.
 */
public class DisplayStyleActivity extends BaseSelectionActivity {

    @Override
    protected ScreenSpec createSpec() {
        return new ScreenSpec.Builder(3, R.string.style_title, R.string.style_subtitle,
                OnboardingPrefs.KEY_DISPLAY_STYLE)
                .option("medical_film", R.drawable.thumb_style_medical_film, R.string.style_medical_film, R.string.style_medical_film_desc)
                .option("glow_outline", R.drawable.thumb_style_glow_outline, R.string.style_glow_outline, R.string.style_glow_outline_desc)
                .option("night_contrast", R.drawable.thumb_style_night_contrast, R.string.style_night_contrast, R.string.style_night_contrast_desc)
                .option("soft_transparent", R.drawable.thumb_style_soft_transparent, R.string.style_soft_transparent, R.string.style_soft_transparent_desc)
                .option("neon_outline", R.drawable.thumb_style_neon_outline, R.string.style_neon_outline, R.string.style_neon_outline_desc)
                .option("heat_map", R.drawable.thumb_style_heat_map, R.string.style_heat_map, R.string.style_heat_map_desc)
                .option("bone_highlight", R.drawable.thumb_style_bone_highlight, R.string.style_bone_highlight, R.string.style_bone_highlight_desc)
                .option("classic_report", R.drawable.thumb_style_classic_report, R.string.style_classic_report, R.string.style_classic_report_desc)
                .nativeAdBefore("heat_map")
                .next(ScanQualityActivity.class)
                .build();
    }
}
