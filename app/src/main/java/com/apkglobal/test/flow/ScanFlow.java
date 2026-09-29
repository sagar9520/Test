package com.apkglobal.test.flow;

import com.apkglobal.test.R;
import com.apkglobal.test.flow.FlowStep.Option;
import com.apkglobal.test.flow.FlowStep.Type;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * The onboarding flow: 6 meaningful steps (replaces the old 7 + 5 = 12 filler screens). Every answer is used
 * on the Ready screen. To add or remove a step, edit {@link #STEPS} only; progress, navigation and facts follow.
 */
public final class ScanFlow {

    public static final String STEP_TARGET = "target";
    public static final String STEP_MODEL = "model";
    public static final String STEP_STYLE = "style";
    public static final String STEP_DETAIL = "detail";
    public static final String STEP_CAMERA = "camera";
    public static final String STEP_SOUND = "sound";

    private static final List<FlowStep> STEPS = Collections.unmodifiableList(Arrays.asList(
            new FlowStep(STEP_TARGET, Type.QUESTION,
                    R.string.q_target_title, R.string.q_target_subtitle, XRayFacts.factFor(0),
                    new Option("hand", R.string.opt_hand_title, R.string.opt_hand_desc,
                            R.drawable.ic_hand, false, R.string.sum_hand),
                    new Option("body", R.string.opt_body_title, R.string.opt_body_desc,
                            R.drawable.ic_skeleton, false, R.string.sum_body)),

            // Skeleton-only framing (no body/clothing wording): keeps the app clear of Play's
            // policy against apps that claim to see through clothes.
            new FlowStep(STEP_MODEL, Type.CHOICE,
                    R.string.q_model_title, R.string.q_model_subtitle, XRayFacts.factFor(1),
                    new Option("male", R.string.opt_male_title, R.string.opt_male_desc,
                            R.drawable.ic_male, false, R.string.sum_male),
                    new Option("female", R.string.opt_female_title, R.string.opt_female_desc,
                            R.drawable.ic_female, false, R.string.sum_female)),

            new FlowStep(STEP_STYLE, Type.QUESTION,
                    R.string.q_style_title, R.string.q_style_subtitle, XRayFacts.factFor(2),
                    new Option("classic", R.string.opt_classic_title, R.string.opt_classic_desc,
                            R.drawable.ic_film, false, R.string.sum_classic),
                    new Option("neon", R.string.opt_neon_title, R.string.opt_neon_desc,
                            R.drawable.ic_sparkle, false, R.string.sum_neon)),

            // Rewarded opt-in: the user asks for something extra, so the ad is welcome (highest eCPM format).
            new FlowStep(STEP_DETAIL, Type.QUESTION,
                    R.string.q_detail_title, R.string.q_detail_subtitle, XRayFacts.factFor(3),
                    new Option("detailed", R.string.opt_detailed_title, R.string.opt_detailed_desc,
                            R.drawable.ic_search, true, R.string.sum_detailed),
                    new Option("quick", R.string.opt_quick_title, R.string.opt_quick_desc,
                            R.drawable.ic_bolt, false, R.string.sum_quick)),

            new FlowStep(STEP_CAMERA, Type.CHOICE,
                    R.string.q_camera_title, R.string.q_camera_subtitle, XRayFacts.factFor(4),
                    new Option("back", R.string.opt_back_cam_title, R.string.opt_back_cam_desc,
                            R.drawable.ic_camera_rear, false, R.string.sum_back_cam),
                    new Option("front", R.string.opt_front_cam_title, R.string.opt_front_cam_desc,
                            R.drawable.ic_camera_front, false, R.string.sum_front_cam)),

            new FlowStep(STEP_SOUND, Type.QUESTION,
                    R.string.q_sound_title, R.string.q_sound_subtitle, XRayFacts.factFor(5),
                    new Option("on", R.string.opt_sound_on_title, R.string.opt_sound_on_desc,
                            R.drawable.ic_volume_on, false, R.string.sum_sound_on),
                    new Option("off", R.string.opt_sound_off_title, R.string.opt_sound_off_desc,
                            R.drawable.ic_volume_off, false, R.string.sum_sound_off))
    ));

    private ScanFlow() {
    }

    public static List<FlowStep> steps() {
        return STEPS;
    }

    public static int size() {
        return STEPS.size();
    }

    /** @throws IndexOutOfBoundsException if {@code i} is not in [0, size()) */
    public static FlowStep get(int i) {
        return STEPS.get(i);
    }
}
