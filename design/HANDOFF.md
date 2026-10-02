# Handoff: "xray app ui" chat → Claude Code

This file sums up everything decided in the design chat, so a new Claude Code session can continue without it.

## What is in this repo (`sagar9520/Test`, branch `claude/magical-babbage-744gai`)
- `design/*.png`: final design images, 1080px wide, full screen length:
  - `1_..5_*.png`: 5 onboarding selection screens (Language, Scan Mode, Display Style, Scan Quality, Result View).
  - `6_home.png`: new home screen.
  - `tool1..4_*_step1..3_*.png`: 3 selection screens before each X-Ray tool.
  - `tool1..4_flow_overview.png`: each tool's full flow on one image.
  - `7_live_skeleton_camera.png`, `8_xray_photo_editor.png`, `9_bone_zoom_scan.png`, `10_my_scan_reports.png`: final tool screens.
  - `unlock_popup_on_*.png`: unlock popup shown on any locked feature.
  - `premium_screen.png`: Pro screen with Cashfree plans.
- `design/CLAUDE_CODE_PROMPT_HINDI.md` and `design/CLAUDE_CODE_PROMPT.md`: the full implementation brief (same content, Hindi and English). **This is the spec to follow.**
- Working reference code for the 5 onboarding screens (Java, AppCompat, AdMob 25.5.0 + UMP 4.0.0, compile/target SDK 36):
  - `app/src/main/java/com/apkglobal/test/onboarding/`:
    - `BaseSelectionActivity`: builds the list and inserts the native ad + Continue and the banners from a spec.
    - `ScreenSpec`: options plus ad anchors such as `nativeAdAfter("gray_scale")`, `bannerAfter(...)`, `nativeAdBefore(...)`.
    - the 5 activities, `StepIndicatorView`, `OnboardingPrefs`.
  - `app/src/main/java/com/apkglobal/test/ads/`:
    - `NativeAdController`: same-size skeleton, collapse rules, RTL-aware AdChoices.
    - `BannerAdController`: anchored adaptive banner; in-list banners are the same size as the bottom banner.
    - `ConsentManager` (UMP) and `AdsInitializer`.
  - `app/src/main/res/`:
    - `layout/` (activity_selection, item_option, ad_native_large, ad_native_placeholder, view_banner_ad_slot, view_continue_button);
    - `drawable/thumb_*.xml`: 40 vector thumbnails;
    - `values/` (colors, dimens, styles, strings, ads.xml with Google test ad IDs);
    - `font/poppins_*`.

## Decisions taken in the chat
1. **Replace the app's 18 start screens with the 5 new onboarding screens.**
   - Every screen has a native ad with the Continue button directly below it, plus a fixed bottom banner.
   - In-list banners are exactly the size of the bottom banner.
   - Exact positions:
     - Language: banner below Spanish, native ad below Portuguese.
     - Scan Mode: native ad below Gray Scale, banner below Thermal View.
     - Display Style: native ad above Heat Map Style; bottom banner only.
     - Scan Quality: banner below Quick Scan, native ad above Auto Quality.
     - Result View: native ad below Zoom View, banner below Highlight View.
2. **Home (avoid copying the old app):**
   - Header: app logo on the left, the name "Xray Body Scanner Camera" on its right, and a PRO chip.
   - Top features renamed: Full Body X-Ray Scan, Old Age Body Simulator, Body Parts Guide.
   - Native ad below the top features.
   - "X-Ray Tools" grid: Live Skeleton Camera, X-Ray Photo Editor, Bone Zoom Scan, My Scan Reports.
   - Fixed bottom banner.
3. **Each X-Ray tool:** 3 selection screens (same component and ad pattern as onboarding), then the final tool screen. Options per step are listed in the prompt.
4. **Ads on every screen after Home** follow the same pattern as onboarding.
5. **Unlock popup** on every locked feature:
   - the **app's real logo** with a crown and a PREMIUM badge on top;
   - everything else the same as the current popup: Cancel / Watch ad · Free runs the existing rewarded ad, and ₹99 · 5 Scan runs the existing Cashfree flow.
6. **Premium screen:**
   - new plans UI: Starter ₹99 (5 scans), Plus ₹199 (15 scans, preselected), Unlimited ₹299;
   - Cashfree secure-payment line;
   - native ad and bottom banner;
   - no ads at all for premium users.
7. **Cashfree gateway and all JSON stay exactly the same.** Only the UI changes.
8. After the new flow works, **remove the old unused code** (18 screens, old home sections and their resources). Never touch Cashfree, JSON models, ads or premium logic.
9. **Process:**
   - First explain the structure and plan, and give a **time estimate**.
   - Wait for an OK.
   - Commit locally per section, and **do not push to GitHub** unless asked.
   - At the end, do one **full review** of all code and features.

## Notes
- The ad unit IDs in `values/ads.xml` are Google **test** IDs. The real app keeps its own IDs.
- The logo drawn in the images is a placeholder. Use the app's launcher logo.
- The reference code lives in a test project (package `com.apkglobal.test`). Adapt it to the real app's package and structure; do not copy it blindly.
