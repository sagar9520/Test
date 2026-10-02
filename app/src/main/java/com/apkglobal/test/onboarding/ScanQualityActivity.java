package com.apkglobal.test.onboarding;

import com.apkglobal.test.R;

/**
 * Step 4/5 - Select Scan Quality.
 * Ads: banner below "Quick Scan", native ad (+ Continue) above "Auto Quality", banner at the bottom.
 */
public class ScanQualityActivity extends BaseSelectionActivity {

    @Override
    protected ScreenSpec createSpec() {
        return new ScreenSpec.Builder(4, R.string.quality_title, R.string.quality_subtitle,
                OnboardingPrefs.KEY_SCAN_QUALITY)
                .option("quick", R.drawable.thumb_quality_quick, R.string.quality_quick, R.string.quality_quick_desc)
                .option("standard", R.drawable.thumb_quality_standard, R.string.quality_standard, R.string.quality_standard_desc)
                .option("hd", R.drawable.thumb_quality_hd, R.string.quality_hd, R.string.quality_hd_desc)
                .option("deep", R.drawable.thumb_quality_deep, R.string.quality_deep, R.string.quality_deep_desc)
                .option("smooth", R.drawable.thumb_quality_smooth, R.string.quality_smooth, R.string.quality_smooth_desc)
                .option("high_detail", R.drawable.thumb_quality_high_detail, R.string.quality_high_detail, R.string.quality_high_detail_desc)
                .option("auto", R.drawable.thumb_quality_auto, R.string.quality_auto, R.string.quality_auto_desc)
                .option("battery_saver", R.drawable.thumb_quality_battery_saver, R.string.quality_battery_saver, R.string.quality_battery_saver_desc)
                .bannerAfter("quick")
                .nativeAdBefore("auto")
                .next(ResultViewActivity.class)
                .build();
    }
}
