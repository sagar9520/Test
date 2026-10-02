# Claude Code ke liye Prompt (Hindi) — Xray Body Scanner Camera: naya UI flow

> Neeche ka poora text Claude Code mein paste karein (app ka repo khol kar).
> Saath mein `design_pack.zip` ki images attach karein:
> - 5 onboarding screens,
> - `6_home.png`,
> - 4 tool flow overviews (`tool1..4_flow_overview.png`),
> - 4 tool screens (`7..10_*.png`),
> - 3 `unlock_popup_*.png`,
> - `premium_screen.png`.

---

Tum meri existing Android app "Xray Body Scanner Camera" par kaam kar rahe ho. Main naye UI designs (attached images) de raha hoon. Tumhe app ka UI flow in designs ke hisaab se dobara banana hai, **lekin existing business logic nahi todna hai**.

## 0. Code likhne se pehle
1. Pehle poora project explore karo:
   - har Activity/Fragment, layouts aur navigation;
   - ads ka code (AdMob native, banner, interstitial, rewarded);
   - **Cashfree** payment integration;
   - premium/pro logic;
   - saare JSON models ya JSON files (API request/response, remote config, plan list waghera).
2. Phir mujhe batao:
   - abhi ka structure short mein: screens kis order mein hain, ads, Cashfree aur premium check kahan hain;
   - implementation plan, step by step, neeche ke sections ke hisaab se;
   - **poora kaam kitne time mein hoga**, har section ka aur total.
3. **Mere "OK" ke baad hi implementation shuru karna.**

## 1. Zaroori rules (inhe todna nahi hai)
- **GitHub par push NAHI karna.** Na `git push`, na PR. Local commits theek hain, har section ka alag commit, saaf message ke saath.
- **Cashfree payment gateway bilkul waise hi rahe jaise abhi chal raha hai.**
  - Wahi SDK, wahi order/session creation, wahi callbacks, wahi verification.
  - Sirf uske aas-paas ka UI badlega: premium screen aur popup.
- **JSON same rehna chahiye.**
  - Koi key rename ya remove nahi karni.
  - Model classes ke serialized names, request/response format, JSON files aur remote-config keys nahi badalne.
  - Bahut zaroori ho to sirf optional field *add* kar sakte ho, aur mujhe batana.
- **Saare existing ad unit IDs, rewarded ad logic aur premium (pro) check waise hi rakhne hain.** User premium hai to kahin bhi ad nahi dikhna chahiye (existing flag use karo).
- Package name, existing permissions aur SDK versions mat badlo. Bahut zaroori ho to pehle mujhe batao.
- Designs se close match karna hai: spacing, colors, typography, card style, ad positions.
  - Chhote aur bade dono phones par sahi dikhe.
  - Edge-to-edge insets sahi hon.
  - RTL-safe rahe (`start`/`end` use karo).
- Designs mein jahan bhi logo hai (home header, unlock popup, premium screen), wahan **app ka asli launcher logo** lagana. Images wala placeholder logo nahi.

## 2. Design system (images se)
- **Font:** Poppins (Regular 400, Medium 500, SemiBold 600), `res/font` mein bundle karo.
- **Colors:**

| Kahan | Color |
|---|---|
| Primary teal | `#0FB5A3` (gradient `#16C9AF → #0B9C94`) |
| Page background | gradient `#F8FBFE → #ECF2F8` |
| Text | `#0F1B2D` |
| Secondary text | `#5B6B7F` |
| Card | white, `#E2E9F1` 1dp border |
| Selected card | `#EAF9F6` background + 1.5dp `#0FB5A3` border |
| Ad card | `#EEF3F8`, `#DFE7F0` border |
| Ad CTA | blue gradient `#5490FF → #3366EE` |
| "Ad" badge | `#3B7BF6` |

- **Option card:**
  - 16dp radius, ~70–76dp height;
  - left mein 64×46dp thumbnail;
  - title 15sp SemiBold, description 12sp;
  - right mein radio.
- **Continue button:** 52dp, full pill, teal gradient, white 16sp SemiBold text, halki teal shadow.
- **Thumbnails:** images jaisi dark X-ray style artwork (neon/glow skeleton, flags, quality icons). Inhe VectorDrawable mein banao; jahan tak ho sake bitmap nahi.

## 3. Onboarding: purani 18 start screens hata kar ye 5 lagao
App mein abhi **start mein 18 screens** hain (language, gender, face shape, eye colour, chest type, beard waghera). **Ye saari 18 flow se hata do** aur unki jagah bilkul ye 5 selection screens isi order mein lagao:

| Step | Screen | Options (isi order mein) |
|---|---|---|
| 1/5 | Select Language | English, Hindi, Spanish, Portuguese, Arabic, Indonesian, Bengali, Turkish |
| 2/5 | Select Scan Mode | Classic X-Ray, Green Scan, Blue Scan, Gray Scale, Neon Scan, Thermal View, Edge Highlight, Deep Scan |
| 3/5 | Select Display Style | Medical Film, Glow Outline, Night Contrast, Soft Transparent, Neon Outline, Heat Map Style, Bone Highlight, Classic Report |
| 4/5 | Select Scan Quality | Quick Scan, Standard Scan, HD Scan, Deep Scan, Smooth Mode, High Detail, Auto Quality, Battery Saver |
| 5/5 | Select Result View | Full Screen, Split View, Zoom View, Compare View, Detail View, Highlight View, Before/After, Auto Rotate |

- Titles, subtitles aur option descriptions bilkul images jaise rakhne hain.
- Header mein back button, step progress (track par dots, current dot par halo) aur "n/5".
- Pehla option pehle se selected ho.
- Har selection save karo (SharedPreferences ya app ka existing storage).
- App mein pehle se language/locale ka system hai to language choice usi se apply karo.

**Har screen par ads (exact positions):**
- Har screen par **native ad, jiske turant neeche Continue button**, aur **screen ke bottom mein fixed banner**.

| Screen | In-list banner | Native ad (+ Continue) |
|---|---|---|
| 1 Language | **Spanish** ke neeche | **Portuguese** ke neeche |
| 2 Scan Mode | **Thermal View** ke neeche | **Gray Scale** ke neeche |
| 3 Display Style | nahi (sirf bottom banner) | **Heat Map Style** ke theek upar |
| 4 Scan Quality | **Quick Scan** ke neeche | **Auto Quality** ke theek upar |
| 5 Result View | **Highlight View** ke neeche | **Zoom View** ke neeche |

**Ads ka size:**
- **Native ad card** ~270dp ka:
  - "Ad" badge;
  - 48dp icon + headline + advertiser "• Sponsored" + body;
  - 120–122dp MediaView;
  - 44–46dp blue CTA;
  - AdChoices badge ke opposite corner mein.
- Ad load hone tak usi size ka loading skeleton dikhao, taaki Continue button upar-neeche na hile.
- **Banners** anchored adaptive hon. **List ke andar wale banner bilkul bottom banner jitne size ke hon** (load se pehle slot ki height fix karo).
- Ad fail ho to user jo content dekh raha hai wo nahi hilna chahiye: slot sirf tab collapse karo jab wo screen se neeche (off-screen) ho, warna jagah bani rehne do.

Step 5 ke baad → Home.

## 4. Home screen (`6_home.png` dekho)
- **Header:**
  - dark navy/teal premium gradient;
  - **left mein app logo**, logo ke right mein "Welcome to" + app ka naam **"Xray Body Scanner Camera"**;
  - sabse right mein gold **PRO** chip, jo Premium screen kholega;
  - neeche ek tagline.
- **Upar ke features**, naye naam aur naye style ke saath (andar ki functionality wahi rahe jo abhi khulti hai):
  - "Full Body Scan For Fun" → **Full Body X-Ray Scan**: bada teal hero card, "Start Scan" button ke saath.
  - "Your Old Body Simulator" → **Old Age Body Simulator**: purple card.
  - "All Body Part Information" → **Body Parts Guide**: orange card.
- In cards ke neeche native ad.
- **"X-Ray Tools" section:** 2×2 grid, purane "Other Features" (Body Filter, Human Species, Energy Level Detector, Open the Gallery…) ki jagah:
  1. Live Skeleton Camera (LIVE tag)
  2. X-Ray Photo Editor (NEW tag)
  3. Bone Zoom Scan
  4. My Scan Reports
- Neeche fixed bottom banner.
- Purane "Other Features" ki screens ab kahin se nahi khulti to wo unused maani jayengi (section 8 dekho).

## 5. X-Ray Tools: har tool ki final screen se pehle 3 selection screens
4 mein se kisi bhi tool par tap karne par **3 selection screens** khulengi, phir tool ki final screen.
- Style aur component onboarding jaisa hi ho.
- Title ke upar chhota tool-name chip ho.
- Progress "1/3, 2/3, 3/3" dikhe.
- **Ads:** native ad jiske turant neeche Continue ho, list mein ek banner, aur bottom mein fixed banner. Rules wahi jo onboarding mein hain.

| Tool | Step 1 | Step 2 | Step 3 | Final screen |
|---|---|---|---|---|
| Live Skeleton Camera | Select Body Area: Full Body, Upper Body, Chest & Ribs, Spine, Hands, Legs & Knees | Select Overlay Effect: Classic X-Ray, Auto Effect, Green Night Vision, Neon Glow, Thermal Live, Gray Film | Select Capture Mode: Photo, Video Record, Timer 3s, Burst Shots, Back Camera, Selfie Camera | `7_live_skeleton_camera.png` |
| X-Ray Photo Editor | Select Photo Source: Gallery, Take a Photo, Recent Photos, Selfie, Sample Photo | Select X-Ray Effect: Medical Film, Glow Outline, Night Contrast, Soft Transparent, Neon Outline, Heat Map, Bone Highlight | Select Result Layout: Before/After Slider, Side by Side, X-Ray Only, Compare Effects, Highlight Bones | `8_xray_photo_editor.png` |
| Bone Zoom Scan | Select Bone Area: Skull, Rib Cage, Spine, Shoulders, Pelvis, Hands | Select Zoom Level: 1.5×, 2×, 3×, 4×, Auto Zoom | Select Detail Mode: High Detail, Bone Highlight, Edge Highlight, Night Contrast, Deep Scan, Smooth Mode | `9_bone_zoom_scan.png` |
| My Scan Reports | Select Report Type: All Reports, Full Body Scans, Bone Zoom, Photo Edits, Live Captures | Select Time Range: Today, This Week, This Month, All Time | Select Report Format: Image (PNG), PDF Report, Report Card, Share Card, Comparison Sheet | `10_my_scan_reports.png` |

Jo selections user karega, wo final tool screen tak pahunchni chahiye aur wahan use honi chahiye.

**Final tool screens (images jaisi):**
- **Live Skeleton Camera:**
  - dark camera UI, skeleton overlay, LIVE tag, flash/flip;
  - HUD chips, effect chips, shutter, gallery thumbnail;
  - sirf bottom banner.
- **X-Ray Photo Editor:**
  - before/after slider, effect thumbnails, "Bone visibility" slider;
  - New photo / Save buttons;
  - native ad, bottom banner.
- **Bone Zoom Scan:**
  - X-ray par magnifier lens + zoom ± buttons;
  - focus chips, facts wala info card;
  - native ad, bottom banner.
- **My Scan Reports:**
  - stats cards, filter tabs;
  - report list (share/more ke saath);
  - teesri report ke baad native ad, bottom banner.

App mein jo scanning, camera, gallery aur storage logic pehle se hai, wahi use karo. Jo feature naya hai (jaise report save karna), use simple local storage se banao aur mujhe batao.

## 6. Unlock popup (har locked feature par)
Jahan bhi koi feature locked hai (home cards, tool cards, tool steps, tool actions), wahan `unlock_popup_*.png` wala popup dikhao: dim/blur screen ke upar ek dialog. Ye app ka **existing** premium aur rewarded ad logic hi use kare.
- **Upar art area mein app ka apna launcher logo**, gold crown aur "PREMIUM" badge ke saath. Image wala placeholder logo nahi.
- Baaki sab abhi wale popup jaisa hi rahe:
  - Title "Unlock Scanner".
  - Text "Go ad-free instantly, or watch a short ad to continue for free."
  - Left button **Cancel / Watch ad · Free**: existing **rewarded ad** chalaye, reward milne par feature unlock ho.
  - Right button **₹99 · 5 Scan / No ads · Instant**: us plan ka existing **Cashfree** flow shuru kare.
  - Neeche "Watching the ad is completely optional."
- Premium users ko popup nahi dikhna chahiye.

## 7. Premium (Pro) screen (`premium_screen.png` dekho)
Ye PRO chip se aur popup ke paid wale raste se khulegi.
- Dark purple design: "Restore" aur close (X), crown ke saath app logo, title "Go **Premium**".
- 2×2 perk cards: Offline Scan, Faster Scan, All Features, 100% No Ads.
- Plans selectable cards mein, beech wala plan pehle se selected:

| Plan | Scans | Price | Badge |
|---|---|---|---|
| Starter | 5 Scans · No Ads | ₹99 | – |
| Plus | 15 Scans · No Ads | ₹199 | MOST POPULAR |
| Unlimited | Unlimited · No Ads | ₹299 | BEST VALUE |

- **"Continue with ₹X"** button: **existing Cashfree code** chalaye, wahi plan IDs aur amount ke saath jo abhi JSON/config se aate hain.
- "100% secure payment powered by Cashfree" line, saath mein UPI / VISA / Mastercard / RuPay / Net Banking chips.
- Auto-renew wala text aur Terms / Privacy links.
- **Native ad** aur **fixed bottom banner**. Premium users ke liye ye bhi baaki ads ki tarah hidden rahenge.
- Payment successful hone par entitlement bilkul abhi ki tarah update ho.

## 8. Clean-up: jo purana code ab use nahi hota use hatao
Naya flow chal jaye uske baad, jo kuch ab kahin se use nahi hota wo delete karo:
- purani 18 onboarding screens;
- purane home sections aur unki screens (agar replace ho gayi hain);
- sirf hatayi gayi screens ke layouts, drawables, strings, styles, adapters aur models;
- dead navigation entries aur manifest ki `<activity>` entries.

Delete karne se pehle har usage dhoondo: code, XML, manifest, navigation graph, reflection ya string se class names. Jo bhi Cashfree, JSON models, ads, premium logic ya kisi bachi hui screen mein use hota hai, wo **delete nahi karna**. Jo hataya uski list mujhe do.

## 9. Aakhri full review (sab ho jaane ke baad)
Poori app ka ek complete review karo aur jo galti mile use theek karo:
1. Debug aur release dono variants lint ke saath bina error build hon.
2. Poora flow code mein check karo:
   - 5 onboarding screens → Home → 4 tools mein se har ek (3 steps + final screen);
   - locked feature par popup: rewarded wala rasta aur Cashfree wala rasta;
   - Premium screen;
   - har jagah back navigation.
3. Ads:
   - upar likhi har position par ad hai, order sahi hai, aur Continue native ad ke theek neeche hai;
   - list ke banner bottom banner jitne size ke hain;
   - lifecycle sahi hai (pause/resume/destroy, native ad destroy);
   - premium user ko koi ad nahi;
   - app UMP consent use karti hai to wo follow ho raha hai.
4. Cashfree aur JSON ka behaviour bilkul same ho. Payment classes aur JSON models ka original se diff karke confirm karo.
5. Configuration change aur process death par sahi chale; RTL, edge-to-edge insets aur chhoti screens par bhi.
6. Koi unused code ya resource na bache (lint `UnusedResources` chala kar check karo).
7. Mujhe report do:
   - har screen aur feature ki list, status ke saath (chal raha hai / mera input chahiye);
   - kya-kya hataya;
   - kya assumptions liye;
   - mujhse aur kya chahiye (real ad unit IDs, plan IDs waghera).

Yaad rakhna: **GitHub push nahi karna.** Pehle time estimate batao aur mere OK ka wait karo.
