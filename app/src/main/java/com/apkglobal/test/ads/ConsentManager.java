package com.apkglobal.test.ads;

import android.app.Activity;
import android.content.Context;

import com.google.android.ump.ConsentForm;
import com.google.android.ump.ConsentInformation;
import com.google.android.ump.ConsentRequestParameters;
import com.google.android.ump.FormError;
import com.google.android.ump.UserMessagingPlatform;

/**
 * Google User Messaging Platform (GDPR / US-states consent), as in Google's official AdMob
 * samples. Ads may only be requested once {@link #canRequestAds()} is true.
 * The consent messages themselves are configured in AdMob > Privacy &amp; messaging.
 */
public final class ConsentManager {

    public interface OnConsentGatheringCompleteListener {
        void consentGatheringComplete(FormError error);
    }

    private static ConsentManager instance;
    private final ConsentInformation consentInformation;

    private ConsentManager(Context context) {
        consentInformation = UserMessagingPlatform.getConsentInformation(context);
    }

    public static synchronized ConsentManager getInstance(Context context) {
        if (instance == null) {
            instance = new ConsentManager(context.getApplicationContext());
        }
        return instance;
    }

    /** True when consent (if needed) was obtained, possibly in a previous session. */
    public boolean canRequestAds() {
        return consentInformation.canRequestAds();
    }

    /** True when the app must offer a way to change the privacy choices. */
    public boolean isPrivacyOptionsRequired() {
        return consentInformation.getPrivacyOptionsRequirementStatus()
                == ConsentInformation.PrivacyOptionsRequirementStatus.REQUIRED;
    }

    /** Refreshes consent info (call on every app launch) and shows the form if required. */
    public void gatherConsent(Activity activity, OnConsentGatheringCompleteListener listener) {
        ConsentRequestParameters params = new ConsentRequestParameters.Builder().build();
        consentInformation.requestConsentInfoUpdate(
                activity,
                params,
                () -> UserMessagingPlatform.loadAndShowConsentFormIfRequired(
                        activity, listener::consentGatheringComplete),
                listener::consentGatheringComplete);
    }

    public void showPrivacyOptionsForm(Activity activity, ConsentForm.OnConsentFormDismissedListener listener) {
        UserMessagingPlatform.showPrivacyOptionsForm(activity, listener);
    }
}
