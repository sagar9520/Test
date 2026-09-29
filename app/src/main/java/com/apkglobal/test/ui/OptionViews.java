package com.apkglobal.test.ui;

import android.content.Context;
import android.util.TypedValue;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;

import com.apkglobal.test.R;
import com.apkglobal.test.flow.FlowStep;
import com.apkglobal.test.flow.ScanSession;
import com.google.android.material.card.MaterialCardView;

/** Binds answer cards (view_option_card) and tiles (view_choice_tile) and draws their selected state. */
final class OptionViews {

    /** Alpha of the answer that was NOT picked, while the screen moves on. */
    static final float UNSELECTED_ALPHA = 0.5f;

    private OptionViews() {
    }

    static void bindOptionCard(@NonNull View card, @NonNull FlowStep.Option option) {
        ImageView icon = card.findViewById(R.id.option_icon);
        TextView title = card.findViewById(R.id.option_title);
        TextView desc = card.findViewById(R.id.option_desc);
        icon.setImageResource(option.iconRes);
        title.setText(option.titleRes);
        desc.setText(option.descRes);
        updateRewardChip(card, option);
    }

    static void bindChoiceTile(@NonNull View tile, @NonNull FlowStep.Option option) {
        ImageView icon = tile.findViewById(R.id.choice_icon);
        TextView label = tile.findViewById(R.id.choice_label);
        TextView desc = tile.findViewById(R.id.choice_desc);
        icon.setImageResource(option.iconRes);
        label.setText(option.titleRes);
        desc.setText(option.descRes);
    }

    /** "Watch ad" pill: only on rewarded answers that are not unlocked yet (never ask twice). */
    static void updateRewardChip(@NonNull View card, @NonNull FlowStep.Option option) {
        View chip = card.findViewById(R.id.option_chip);
        if (chip == null) return;
        boolean show = option.rewarded && !ScanSession.get().isDetailedUnlocked();
        chip.setVisibility(show ? View.VISIBLE : View.GONE);
    }

    static void setSelected(@NonNull MaterialCardView card, boolean selected) {
        Context c = card.getContext();
        card.setStrokeColor(ContextCompat.getColor(c, selected ? R.color.xr_primary : R.color.xr_outline));
        card.setStrokeWidth(dp(c, selected ? 2 : 1));
        card.setCardBackgroundColor(ContextCompat.getColor(c, selected ? R.color.xr_selected : R.color.xr_surface));
        card.setSelected(selected); // announced as "selected" by TalkBack
    }

    private static int dp(Context c, int dp) {
        return Math.round(TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dp,
                c.getResources().getDisplayMetrics()));
    }
}
