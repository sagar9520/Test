# Prompt for Claude Code — Xray Body Scanner Camera: new UI flow

> Paste everything below into Claude Code, opened in the app's repository.
> Attach the design images with it: the 5 onboarding screens, `6_home.png`, the 4 tool flow overviews (`tool1..4_flow_overview.png`), the 4 tool screens (`7..10_*.png`), the 3 `unlock_popup_*.png` and `premium_screen.png`.

---

You are working in my existing Android app "Xray Body Scanner Camera". I am giving you new UI designs (attached images). Your job is to rebuild the app's UI flow to match these designs **without breaking the existing business logic**.

## 0. Before you write any code
1. Explore the whole project first: every Activity/Fragment, layouts, navigation, ad code (AdMob native, banner, interstitial, rewarded), the **Cashfree** payment integration, the premium/pro logic and any JSON models or JSON files (API requests/responses, remote config, plan lists, etc.).
2. Then reply with:
   - a short summary of the current structure (screens in order, where ads, Cashfree and premium checks live),
   - your implementation plan, step by step, mapped to the sections below,
   - **an estimate of how long the whole job will take** (per section and total).
3. **Wait for my "OK" before starting the implementation.**

## 1. Hard rules
- **Do NOT push to GitHub** (no `git push`, no PR). Local commits are fine, one commit per section with a clear message.
- **Cashfree payment gateway: keep it exactly as it works today.** Same SDK, same order/session creation, same callbacks, same verification. Only the UI around it (the premium screen and the popup) changes.
- **JSON must stay the same.** Do not rename or remove keys, do not change model classes' serialized names, request/response formats, or any JSON files/remote-config keys. You may *add* optional fields only if strictly necessary, and tell me.
- **Keep all existing ad unit IDs, the rewarded-ad logic and the premium (pro) entitlement checks.** If a user is premium, no ads anywhere (respect the existing flag).
- Keep the app's package name, existing permissions and SDK versions unless a change is strictly needed (tell me first).
- Match the designs closely: spacing, colors, typography, card styles, ad positions. Support small and large phones, light status/nav bar icons where the background is light, edge-to-edge insets, RTL-safe (`start`/`end`).
- Use the **app's real launcher logo** everywhere a logo appears in the designs (home header, unlock popup, premium screen). Do not use the placeholder logo drawn in the images.

## 2. Design system (from the images)
- Font: Poppins (Regular 400, Medium 500, SemiBold 600) bundled in `res/font`.
- Colors: primary teal `#0FB5A3` (gradient `#16C9AF → #0B9C94`), page background gradient `#F8FBFE → #ECF2F8`, text `#0F1B2D` / secondary `#5B6B7F`, card white with `#E2E9F1` 1dp border, selected card `#EAF9F6` bg + 1.5dp `#0FB5A3` border, ad card `#EEF3F8` with `#DFE7F0` border, ad CTA blue gradient `#5490FF → #3366EE`, "Ad" badge `#3B7BF6`.
- Option card: 16dp radius, ~70–76dp tall, thumbnail 64×46dp on the left, title 15sp SemiBold, description 12sp, radio on the right.
- Continue button: 52dp, full pill, teal gradient, white 16sp SemiBold, soft teal shadow.
- Thumbnails: dark X-ray style artwork (neon/glow skeletons, flags, quality icons) as in the images; implement them as VectorDrawables (no bitmaps where possible).

## 3. Onboarding: replace the old 18 start screens with these 5
The app currently shows **18 screens at start** (language, gender, face shape, eye colour, chest type, beard, etc.). **Remove all 18 from the flow** and replace them with exactly these 5 selection screens, in this order:

| Step | Screen | Options (in order) |
|---|---|---|
| 1/5 | Select Language | English, Hindi, Spanish, Portuguese, Arabic, Indonesian, Bengali, Turkish |
| 2/5 | Select Scan Mode | Classic X-Ray, Green Scan, Blue Scan, Gray Scale, Neon Scan, Thermal View, Edge Highlight, Deep Scan |
| 3/5 | Select Display Style | Medical Film, Glow Outline, Night Contrast, Soft Transparent, Neon Outline, Heat Map Style, Bone Highlight, Classic Report |
| 4/5 | Select Scan Quality | Quick Scan, Standard Scan, HD Scan, Deep Scan, Smooth Mode, High Detail, Auto Quality, Battery Saver |
| 5/5 | Select Result View | Full Screen, Split View, Zoom View, Compare View, Detail View, Highlight View, Before/After, Auto Rotate |

Use the titles, subtitles and option descriptions exactly as in the images. Header: back button, step progress (dots on a track, current dot with halo) and "n/5". First option preselected; save each selection (SharedPreferences or the app's existing storage); the language choice must use the app's existing language/locale mechanism if one exists.

**Ad placement per screen (exact):**
- Every screen: a **native ad with the Continue button directly below it**, plus a **fixed banner at the bottom** of the screen.
- 1 Language: in-list banner below **Spanish**, native ad (+ Continue) below **Portuguese**.
- 2 Scan Mode: native ad (+ Continue) below **Gray Scale**, in-list banner below **Thermal View**.
- 3 Display Style: native ad (+ Continue) directly above **Heat Map Style**; bottom banner only (no in-list banner).
- 4 Scan Quality: in-list banner below **Quick Scan**, native ad (+ Continue) directly above **Auto Quality**.
- 5 Result View: native ad (+ Continue) below **Zoom View**, in-list banner below **Highlight View**.

**Ad sizes:**
- Native ad card ~270dp tall: "Ad" badge, 48dp icon + headline + advertiser "• Sponsored" + body, 120–122dp MediaView, 44–46dp blue CTA. AdChoices in the corner opposite the badge.
- Show a same-size loading skeleton until the ad loads, so the Continue button never jumps.
- Banners are anchored adaptive banners. **In-list banners must be exactly the same size as the bottom banner** (fix the slot height before loading).
- If an ad fails, never shift content the user is looking at: collapse the slot only if it is off-screen below, otherwise keep the space.

After step 5 → Home.

## 4. Home screen (see `6_home.png`)
- Header: dark navy/teal premium gradient, **app logo on the left**, "Welcome to" + app name **"Xray Body Scanner Camera"** on the right of the logo, gold **PRO** chip on the far right (opens the Premium screen), tagline below.
- Top features, renamed and restyled (keep the same underlying functionality they open today):
  - "Full Body Scan For Fun" → **Full Body X-Ray Scan**: big teal hero card with a "Start Scan" button.
  - "Your Old Body Simulator" → **Old Age Body Simulator**: purple card.
  - "All Body Part Information" → **Body Parts Guide**: orange card.
- Native ad below these cards.
- Section **"X-Ray Tools"**, a 2×2 grid replacing the old "Other Features" (Body Filter, Human Species, Energy Level Detector, Open the Gallery…):
  1. Live Skeleton Camera (LIVE tag)
  2. X-Ray Photo Editor (NEW tag)
  3. Bone Zoom Scan
  4. My Scan Reports
- Fixed bottom banner.
- If the old "Other Features" screens are no longer reachable, they count as unused (see section 8).

## 5. X-Ray Tools: 3 selection screens before each tool screen
Tapping any of the 4 tools opens **3 selection screens** (same component and style as onboarding, with a small tool-name chip above the title and a "1/3, 2/3, 3/3" progress), then the tool's final screen.

**Ads:** a native ad with Continue directly below it, one in-list banner, and the fixed bottom banner. Same rules as onboarding.

| Tool | Step 1 | Step 2 | Step 3 | Final screen |
|---|---|---|---|---|
| Live Skeleton Camera | Select Body Area: Full Body, Upper Body, Chest & Ribs, Spine, Hands, Legs & Knees | Select Overlay Effect: Classic X-Ray, Auto Effect, Green Night Vision, Neon Glow, Thermal Live, Gray Film | Select Capture Mode: Photo, Video Record, Timer 3s, Burst Shots, Back Camera, Selfie Camera | `7_live_skeleton_camera.png` |
| X-Ray Photo Editor | Select Photo Source: Gallery, Take a Photo, Recent Photos, Selfie, Sample Photo | Select X-Ray Effect: Medical Film, Glow Outline, Night Contrast, Soft Transparent, Neon Outline, Heat Map, Bone Highlight | Select Result Layout: Before/After Slider, Side by Side, X-Ray Only, Compare Effects, Highlight Bones | `8_xray_photo_editor.png` |
| Bone Zoom Scan | Select Bone Area: Skull, Rib Cage, Spine, Shoulders, Pelvis, Hands | Select Zoom Level: 1.5×, 2×, 3×, 4×, Auto Zoom | Select Detail Mode: High Detail, Bone Highlight, Edge Highlight, Night Contrast, Deep Scan, Smooth Mode | `9_bone_zoom_scan.png` |
| My Scan Reports | Select Report Type: All Reports, Full Body Scans, Bone Zoom, Photo Edits, Live Captures | Select Time Range: Today, This Week, This Month, All Time | Select Report Format: Image (PNG), PDF Report, Report Card, Share Card, Comparison Sheet | `10_my_scan_reports.png` |

The selections must be passed to, and used by, the final tool screen.

**Final tool screens (match the images):**
- **Live Skeleton Camera:** dark camera UI with skeleton overlay, LIVE tag, flash/flip, HUD chips, effect chips, shutter, gallery thumbnail. Bottom banner only.
- **X-Ray Photo Editor:** before/after slider, effect thumbnails, "Bone visibility" slider, New photo / Save, native ad, bottom banner.
- **Bone Zoom Scan:** X-ray with magnifier lens + zoom ± buttons, focus chips, info card with facts, native ad, bottom banner.
- **My Scan Reports:** stats cards, filter tabs, report list with share/more, native ad after the 3rd report, bottom banner.

Reuse the app's existing scanning, camera, gallery and storage logic wherever it already exists. Where a feature is new (for example report saving), build it with simple local storage and tell me.

## 6. Unlock popup (on every locked feature)
Wherever a feature is locked (home cards, tool cards, tool steps, tool actions), show the popup from `unlock_popup_*.png` as a dialog over a dimmed/blurred screen. It must reuse the app's **existing** premium and rewarded-ad logic.
- The top art area shows the **app's own launcher logo** with a gold crown and a "PREMIUM" badge (not the placeholder in the image).
- Everything else stays as in the current popup:
  - Title "Unlock Scanner".
  - Text "Go ad-free instantly, or watch a short ad to continue for free."
  - Left button **Cancel / Watch ad · Free**: shows the existing **rewarded ad** and unlocks on reward.
  - Right button **₹99 · 5 Scan / No ads · Instant**: starts the existing **Cashfree** flow for that plan.
  - Footer "Watching the ad is completely optional."
- Do not show the popup to premium users.

## 7. Premium (Pro) screen (see `premium_screen.png`)
Opened from the PRO chip and from the popup's paid path.
- Dark purple design: "Restore" and close (X), app logo with crown, "Go **Premium**" title.
- 2×2 perk cards: Offline Scan, Faster Scan, All Features, 100% No Ads.
- Plans as selectable cards, middle plan preselected:

| Plan | Scans | Price | Badge |
|---|---|---|---|
| Starter | 5 Scans · No Ads | ₹99 | – |
| Plus | 15 Scans · No Ads | ₹199 | MOST POPULAR |
| Unlimited | Unlimited · No Ads | ₹299 | BEST VALUE |

- "Continue with ₹X" button, which uses the **existing Cashfree code** with the existing plan IDs and amounts from the same JSON/config as today.
- "100% secure payment powered by Cashfree" with UPI / VISA / Mastercard / RuPay / Net Banking chips.
- The auto-renew text and Terms / Privacy links.
- A **native ad** and the **fixed bottom banner** (hidden for premium users like all ads).
- After a successful payment, update the entitlement exactly as today.

## 8. Clean-up: remove the old code that is no longer used
After the new flow works, delete everything that is no longer reachable or used:
- the 18 old onboarding screens,
- the old home sections and their screens if they are replaced,
- old layouts, drawables, strings, styles, adapters and models used only by removed screens,
- dead navigation entries and manifest `<activity>` entries.

Before deleting, search for every usage (code, XML, manifest, navigation graphs, reflection or string-based class names). **Do not delete** anything used by Cashfree, the JSON models, ads, premium logic or any screen still in the app. Give me a list of what you removed.

## 9. Final full review (after everything is done)
Do one complete review pass of the whole app and fix what you find:
1. Build the debug and release variants (lint included) with no errors.
2. Walk the full flow in code:
   - 5 onboarding screens → Home → each of the 4 tools (3 steps + final screen) → locked feature popup (rewarded path and Cashfree path) → Premium screen → back navigation everywhere.
3. Ads:
   - every placement listed above is present, with the correct order and the Continue button directly under the native ad;
   - in-list banners are the same size as the bottom banner;
   - lifecycle is correct (pause/resume/destroy, native ad destroy);
   - no ads for premium users;
   - UMP consent is respected if the app uses it.
4. Cashfree and JSON: unchanged behaviour. Diff the payment classes and JSON models against the original and confirm.
5. Correct behaviour on configuration change and process death, plus RTL, edge-to-edge insets and small screens.
6. No leftover unused code or resources (run lint `UnusedResources` and check).
7. Report back:
   - a list of every screen and feature with its status (works / needs my input),
   - what was removed,
   - any assumptions you made,
   - anything I still need to provide (real ad unit IDs, plan IDs, etc.).

Remember: **no GitHub push.** Tell me the time estimate first and wait for my OK.
