package com.apkglobal.test.ads;

import android.app.Activity;
import android.view.View;
import android.view.ViewGroup;

import com.apkglobal.test.R;
import com.applovin.mediation.MaxAd;
import com.applovin.mediation.nativeAds.MaxNativeAdView;

import java.lang.ref.WeakReference;

/**
 * Handle for one native ad slot on a screen. Call {@link #release()} from the Activity's onDestroy.
 *
 * <p>Holds its Activity and views weakly: the controller (an app-wide singleton) keeps waiting slots in a
 * queue, and a strong reference there would leak the whole screen.
 */
public final class NativeAdSlot {

    private final NativeAdController controller; // null = no-op slot
    private final WeakReference<Activity> activityRef;
    private final WeakReference<View> rootRef;
    final String placement;
    final Runnable timeoutTask;

    private MaxAd ad;
    private boolean released;

    NativeAdSlot(NativeAdController controller, Activity activity, View slotRoot, String placement) {
        this.controller = controller;
        this.activityRef = new WeakReference<>(activity);
        this.rootRef = new WeakReference<>(slotRoot);
        this.placement = placement;
        this.timeoutTask = () -> {
            if (this.controller != null && !this.released) this.controller.onSlotTimeout(this);
        };
    }

    static NativeAdSlot noop() {
        return new NativeAdSlot(null, null, null, null);
    }

    /** Removes the ad view, destroys the ad and cancels a pending request. Safe to call more than once. */
    public void release() {
        if (released) return;
        released = true;
        if (controller == null) return;
        controller.onSlotReleased(this);
        ViewGroup container = container();
        if (container != null) container.removeAllViews();
        MaxAd current = ad;
        ad = null;
        if (current != null) controller.destroyAd(current);
    }

    boolean isAlive() {
        return !released && controller != null && rootRef.get() != null;
    }

    Activity activity() {
        return activityRef.get();
    }

    private ViewGroup container() {
        View root = rootRef.get();
        return root == null ? null : root.findViewById(R.id.native_ad_container);
    }

    /** Reserve the space up front so nothing jumps under the user's finger when the ad arrives. */
    void showPlaceholder() {
        View root = rootRef.get();
        if (root == null) return;
        root.setVisibility(View.VISIBLE);
        View placeholder = root.findViewById(R.id.native_ad_placeholder);
        if (placeholder != null) placeholder.setVisibility(View.VISIBLE);
    }

    void showAd(MaxNativeAdView view, MaxAd newAd) {
        ViewGroup container = container();
        if (container == null || released) {
            if (controller != null) controller.destroyAd(newAd);
            return;
        }
        MaxAd old = ad;
        ad = newAd;
        if (old != null && controller != null) controller.destroyAd(old);

        container.removeAllViews();
        container.addView(view, new ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
        container.setVisibility(View.VISIBLE);
        View root = rootRef.get();
        if (root != null) {
            root.setVisibility(View.VISIBLE);
            View placeholder = root.findViewById(R.id.native_ad_placeholder);
            if (placeholder != null) placeholder.setVisibility(View.GONE);
        }
    }

    /** Nothing to show in time: hide the whole slot (label included) instead of leaving an empty box. */
    void collapse() {
        View root = rootRef.get();
        if (root != null) root.setVisibility(View.GONE);
    }
}
