package com.apkglobal.test.onboarding;

import com.apkglobal.test.MainActivity;
import com.apkglobal.test.R;

/**
 * Step 5/5 - Select Result View.
 * Ads: native ad (+ Continue) below "Zoom View", banner below "Highlight View", banner at the bottom.
 */
public class ResultViewActivity extends BaseSelectionActivity {

    @Override
    protected ScreenSpec createSpec() {
        return new ScreenSpec.Builder(5, R.string.view_title, R.string.view_subtitle,
                OnboardingPrefs.KEY_RESULT_VIEW)
                .option("full_screen", R.drawable.thumb_view_full_screen, R.string.view_full_screen, R.string.view_full_screen_desc)
                .option("split", R.drawable.thumb_view_split, R.string.view_split, R.string.view_split_desc)
                .option("zoom", R.drawable.thumb_view_zoom, R.string.view_zoom, R.string.view_zoom_desc)
                .option("compare", R.drawable.thumb_view_compare, R.string.view_compare, R.string.view_compare_desc)
                .option("detail", R.drawable.thumb_view_detail, R.string.view_detail, R.string.view_detail_desc)
                .option("highlight", R.drawable.thumb_view_highlight, R.string.view_highlight, R.string.view_highlight_desc)
                .option("before_after", R.drawable.thumb_view_before_after, R.string.view_before_after, R.string.view_before_after_desc)
                .option("auto_rotate", R.drawable.thumb_view_auto_rotate, R.string.view_auto_rotate, R.string.view_auto_rotate_desc)
                .nativeAdAfter("zoom")
                .bannerAfter("highlight")
                .next(MainActivity.class)
                .build();
    }
}
