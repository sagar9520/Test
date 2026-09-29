package com.apkglobal.test.flow;

import androidx.annotation.StringRes;

import com.apkglobal.test.R;

/**
 * True X-ray / skeleton facts shown in the "Did you know?" card. Real content on every screen keeps users
 * reading longer (more viewable ad time) and gives the app substance beyond the ads.
 */
public final class XRayFacts {

    /** One fact per flow step, in step order, each matching the step's topic. */
    private static final int[] STEP_FACTS = {
            R.string.fact_hand_bones,   // target: hand or skeleton
            R.string.fact_pelvis,       // model: male / female skeleton
            R.string.fact_white_bones,  // style: why bones look white
            R.string.fact_bone_count,   // detail
            R.string.fact_wavelength,   // camera: what X-rays are
            R.string.fact_roentgen,     // sound
    };

    /** Used for any index outside the flow (rotates). */
    private static final int[] EXTRA_FACTS = {
            R.string.fact_nobel,
            R.string.fact_stapes,
            R.string.fact_femur,
    };

    private XRayFacts() {
    }

    /** A true fact for the step at {@code stepIndex}; indexes outside the flow rotate through extra facts. */
    @StringRes
    public static int factFor(int stepIndex) {
        if (stepIndex >= 0 && stepIndex < STEP_FACTS.length) return STEP_FACTS[stepIndex];
        return EXTRA_FACTS[Math.floorMod(stepIndex, EXTRA_FACTS.length)];
    }

    @StringRes
    public static int homeFact() {
        return R.string.fact_nobel;
    }

    @StringRes
    public static int readyFact() {
        return R.string.fact_stapes;
    }
}
