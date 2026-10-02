package com.apkglobal.test.onboarding;

import com.apkglobal.test.R;

/**
 * Step 1/5 - Select Language.
 * Ads: banner below "Spanish", native ad (+ Continue) below "Portuguese", banner at the bottom.
 */
public class LanguageActivity extends BaseSelectionActivity {

    @Override
    protected ScreenSpec createSpec() {
        return new ScreenSpec.Builder(1, R.string.language_title, R.string.language_subtitle,
                OnboardingPrefs.KEY_LANGUAGE)
                // Keys are BCP-47 language tags, ready to be used for the app locale.
                .option("en", R.drawable.thumb_lang_english, R.string.language_english, R.string.language_english_desc)
                .option("hi", R.drawable.thumb_lang_hindi, R.string.language_hindi, R.string.language_hindi_desc)
                .option("es", R.drawable.thumb_lang_spanish, R.string.language_spanish, R.string.language_spanish_desc)
                .option("pt", R.drawable.thumb_lang_portuguese, R.string.language_portuguese, R.string.language_portuguese_desc)
                .option("ar", R.drawable.thumb_lang_arabic, R.string.language_arabic, R.string.language_arabic_desc)
                .option("id", R.drawable.thumb_lang_indonesian, R.string.language_indonesian, R.string.language_indonesian_desc)
                .option("bn", R.drawable.thumb_lang_bengali, R.string.language_bengali, R.string.language_bengali_desc)
                .option("tr", R.drawable.thumb_lang_turkish, R.string.language_turkish, R.string.language_turkish_desc)
                .bannerAfter("es")
                .nativeAdAfter("pt")
                .next(ScanModeActivity.class)
                .build();
    }
}
