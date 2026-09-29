package com.apkglobal.test.ui;

import android.graphics.Color;
import android.view.View;

import androidx.activity.ComponentActivity;
import androidx.activity.EdgeToEdge;
import androidx.activity.SystemBarStyle;
import androidx.annotation.NonNull;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

/**
 * Edge-to-edge helpers. targetSdk 35+ forces edge-to-edge on Android 15+, so every screen pads its root by the
 * system bar insets: the toolbar stays below the status bar and the anchored banner above the navigation bar
 * (a banner under the gesture bar gets accidental taps and is partly hidden).
 */
public final class Insets {

    private Insets() {
    }

    /**
     * Same look on every Android version: transparent bars with light icons over our dark background.
     * Call in onCreate after super.onCreate() and before setContentView().
     */
    public static void enableEdgeToEdge(@NonNull ComponentActivity activity) {
        EdgeToEdge.enable(activity,
                SystemBarStyle.dark(Color.TRANSPARENT),
                SystemBarStyle.dark(Color.TRANSPARENT));
    }

    /** Pads {@code root} (keeping its own XML padding) by the system bar + display cutout insets. */
    public static void apply(@NonNull View root) {
        final int left = root.getPaddingLeft();
        final int top = root.getPaddingTop();
        final int right = root.getPaddingRight();
        final int bottom = root.getPaddingBottom();
        ViewCompat.setOnApplyWindowInsetsListener(root, (v, windowInsets) -> {
            androidx.core.graphics.Insets bars = windowInsets.getInsets(
                    WindowInsetsCompat.Type.systemBars() | WindowInsetsCompat.Type.displayCutout());
            v.setPadding(left + bars.left, top + bars.top, right + bars.right, bottom + bars.bottom);
            return WindowInsetsCompat.CONSUMED;
        });
    }
}
