# eCPM Growth Plan: Prank X-Ray Scanner (India, AppLovin MAX)

Ye doc app owner / developer ke liye hai. Isme 4 cheezein hain: abhi eCPM kyun low hai, is repo mein kya
naya bana hai (UI + ads code), MAX dashboard mein kya karna hai, aur 2-3 hafte ka safe rollout plan.

**Number labels (har number ke saath ek label hai):**

- **(research se Rn)**: research notes se liya gaya number. Source neeche [§11 Sources](#11-sources) mein hai.
  Dhyan do: zyada tar sources "search-snippet" level ke hain, yaani page khud nahi khul paaya, sirf search
  result ka excerpt dekha gaya. Isliye inhe direction samjho, exact truth nahi.
- **(estimate)**: hamara andaaza ya illustrative number. Apne MAX / Firebase data se verify karo.
- **(code default)**: is repo ke `AdConfig` ka starting value. Ye bhi ek estimate hai, jo policy limits ke
  andar chuna gaya hai. Sab remote se badle ja sakte hain.

**Koi result guaranteed nahi hai.** Har change pehle A/B test karo, phir 100% users pe le jao.

---

## 1. TL;DR: top 10 actions (expected impact ke hisaab se ranked, ranking bhi estimate hai)

1. **Har tap pe interstitial band karo, paced interstitial lagao.** AdMob guidance kehti hai "no more than one
   interstitial ad after every two user actions" (research se R20). Naya code pehla tap skip karta hai, zyada se
   zyada har 2nd tap pe ad dikhata hai, do ads ke beech kam se kam 30s rakhta hai, aur ek session mein max 6 (code default).
   Isse policy strike ka sabse bada risk hat jaata hai aur har interstitial ki price bhi sudharti hai.
2. **App ko "X-ray skeleton simulator" ki tarah present karo. "See through clothing" jaisi koi baat nahi.**
   Google Play aise apps ban karta hai, "prank" label hone par bhi (research se R24). Isliye "Select Gender" ki
   jagah ab "Select skeleton model" hai. Ye poore revenue ko bachata hai, kyunki app remove hua to revenue $0.
3. **India ke liye aur bidders jodo, ek-ek karke, MAX A/B test ke saath.** Candidates: InMobi, Liftoff (Vungle),
   DT Exchange, Mintegral, Unity, Moloco, BidMachine, PubMatic. Abhi sirf Meta + Google hain, to auction patla hai.
   Ek blog South Asia mein 15-30% eCPM lift claim karta hai (research se R18, source kamzor hai). Final faisla apna A/B test karega.
4. **Rewarded opt-in lagao:** step 4 ka "Yes, detailed" aur Ready screen ka "HD scan". H1-2025 mein India mein sirf
   Android rewarded video grow hua tha (+3.60%) (research se R5). Planning ke liye range $0.80-3.00 maano (estimate, R8 working range).
5. **Ad Review ON karo aur suggestive dating/video-chat creatives report/block karo.** Play policy: app ke ads
   app ke content se "significantly more mature" nahi hone chahiye (research se R26). Poori category block mat karo,
   sirf specific advertiser/creative block karo, taaki auction pressure bacha rahe (research se R28).
6. **Layout theek karo:** har screen pe sirf 1 native, content ke NEECHE, kam se kam 24dp gap ke saath (code mein 32dp hai),
   saath mein "Sponsored" label aur "Ad" badge. Banner sirf bottom pe anchored adaptive. Button ke paas koi ad nahi.
   Isse accidental clicks kam honge, aur unke saath Confirmed Click / invalid-traffic discount bhi kam hoga (research se R27, R19).
7. **App Open ad sirf returning users ko dikhao:** 30s se zyada background ke baad, first-ever launch pe kabhi nahi (code default).
   India mein app-open eCPM ka trend upar ki taraf hai (research se R7).
8. **KPI badlo: blended eCPM nahi, balki ARPDAU + D1/D7 retention dekho.** Screens kam karne se eCPM apne aap badh jaata hai,
   lekin revenue gir sakta hai ([§2.5](#25-worked-example-blended-ecpm-ka-math) ka math dekho). `RevenueTracker` ka Firebase
   `ad_impression` code enable karo.
9. **Content upgrade karo:** har screen pe sachcha X-ray fact, har answer ka kahin use ho, Hindi UI (ho chuka hai),
   result/share screen, rewarded extras (3D, no-watermark). Jab users genuinely engage karte hain to clicks aur conversions
   better hote hain, aur bidders zyada bid karte hain. Ye asar dheere aur indirectly aata hai.
10. **Geo mix aur listing sudharo.** Jis app ke zyada users Tier-1 countries se hain, wo same user count pe ~3x ad revenue
    kamata hai (research se R7). Isliye store listing en-US/es/pt-BR/id/de mein localize karo. App ko child-directed mark
    mat karo, warna eCPM 6-30%+ gir sakta hai (research se R29).

Iske alawa Play ka rule: 31 Aug 2026 se naye apps aur updates ko targetSdk 36 chahiye (research se R32, ye source
khud padha gaya tha). Is repo mein targetSdk 36 already set hai.

---

## 2. Abhi kya galat hai (diagnosis)

### 2.1 Screen A: "Question" screen (funnel mein 7 baar aati hai)

Screenshot mein upar se neeche ye dikh raha hai:

- Toolbar ke neeche bada **native** (video + Install button). Ye question se bhi upar hai.
- Question "Would you like detailed analysis of scanned documents?". X-ray app mein "documents" ka sawaal irrelevant hai, yaani filler hai.
- Dark purple subtitle box, phir "Yes, detailed" button.
- **Meta banner dono buttons ke beech mein hai, unko touch karta hua.**
- "No, simple copy is enough" button.
- Neeche ek aur bada **native**, jo aadha screen ke bahar kata hua hai.
- **Har button click pe interstitial.**

Ek screen pe: 2 native + 1 banner = 3 inline ads, plus 1 interstitial attempt. 7 screens ka total: 21 inline + 7 interstitial attempts.

### 2.2 Screen B: "Select Gender" screen (funnel mein 5 baar aati hai)

- Sirf 2 buttons hain: Male / Female.
- Ek **native dono buttons ke beech mein** ("AD" label ke saath).
- Upar ek native, jiska sirf CTA dikh raha hai. Neeche bhi ek native. Kul 2-3 natives.
- Bottom pe banner.
- **Har click pe interstitial.**
- Upar se "Select Gender" wording X-ray app mein "kapdon ke aar-paar dekhna" jaisa lagta hai. Ye Play policy ka risk hai
  aur adult-leaning demand ko bhi khinchta hai.

Ek screen pe: 2-3 native + 1 banner, plus 1 interstitial attempt. 5 screens ka total: 15-20 inline + 5 interstitial attempts.

### 2.3 Poora funnel (12 screens)

- Ek run mein **~36-41 banner/native impressions + 12 interstitial attempts** (screenshots se gine hain).
- 12 sawaal hain, aur unme se kisi ka jawab kahin use nahi hota. Content kam hai, ads zyada. Google ki policy
  "don't place more ads than content" ke khilaaf hai (research se R21).
- Jo ads dikh rahe hain (Meto, Toki, Fita jaise dating/video-chat installs, suggestive creatives) wo low-quality demand ki
  nishani hain (mechanism neeche §2.4.6 mein).

### 2.4 Har problem eCPM kyun girati hai

| # | Problem (kahan) | Mechanism: price kyun girti hai | Label |
|---|---|---|---|
| 1 | **Natives aadhe screen ke bahar** (A ka neeche wala, B ka upar wala) | Viewability kam, ad dikhne ka time kam, aur user ka dhyan nahi jaata. Advertiser ko installs kam milte hain, isliye predicted value aur bid girte hain. | mechanism: industry knowledge (R8 notes, unverified) |
| 2 | **Banner dono buttons ke beech, touching** (A) aur **native buttons ke beech** (B) | Accidental clicks badhte hain. Google aisi placement pe "Confirmed Click" laga deta hai, jisse CPC aur eCPM girta hai. Invalid clicks ka revenue deduct ya withhold ho sakta hai (research se R27). Meta ~2s ke andar bounce-back wala accidental click charge hi nahi karta (research se R38). Google kehta hai banner ka elements ke paas hona "biggest causes of accidental clicks" mein se ek hai (research se R39). | research se R27, R38, R39 |
| 3 | **Har tap pe interstitial** (A + B, ek run mein 12) | (a) Policy violation hai (research se R20). (b) Impression depth decay: session ke 8th-12th interstitial ki value 1st se bahut kam hoti hai, kyunki user fast-close karta hai aur advertiser frequency cap laga dete hain (estimate). (c) Ready ad kam milta hai, to fill/show rate girta hai (estimate). | research se R20 + estimate |
| 4 | **Bidders sirf Meta + Google** | Auction mein jitne kam bidders, clearing price utni kam. Har naya bidder floor/competition badhata hai. | mechanism: industry knowledge; lift claim research se R18 (kamzor) |
| 5 | **Saste formats ki bharmaar** (36-41 banner/native vs 12 interstitial) | MAX ka "eCPM" = total revenue / total impressions. Banner/native India mein bahut saste hain, isliye jitne zyada honge, blended average utna neeche jaayega ([§2.5](#25-worked-example-blended-ecpm-ka-math)). | math (estimate) |
| 6 | **Suggestive dating/video-chat ads** | Ye symptom hai. Jab accidental clicks ki wajah se premium advertisers placement ki bid gira dete hain, to bachi hui demand mostly arbitrage/adult-leaning hoti hai (industry knowledge, unverified). Upar se Play ka rule hai ki ads app ke content se zyada mature nahi hone chahiye (research se R26). | research se R26 + industry knowledge |
| 7 | **"Select Gender" + "see-through" positioning** | Play policy risk (research se R24). Aur advertisers/brand-safety filters aisi app se door rehte hain, jisse demand patli hoti hai (estimate). | research se R24 + estimate |

### 2.5 Worked example: blended eCPM ka math

**Ye poori table ASSUMED numbers pe bani hai (estimate).** Per-format eCPM research notes ki India working ranges ke
andar chune gaye hain: banner $0.02-0.10, native $0.10-0.60, interstitial $0.40-1.50, app-open $0.30-1.20,
rewarded $0.80-3.00 (R8, jo khud "industry-knowledge, unverified" hai). Ek public data point bhi hai: India Android
interstitial 2023 mein ~$0.98-1.04 tha (research se R2), aur Q4 2025 mein India Android interstitials +5.57% badhe (research se R6). Fill rates bhi assumed hain.

**Old funnel (12 screens), ek run:**

| Format | Requests / run | Fill (assumed) | Impressions / run | eCPM (assumed) | Revenue per 1,000 runs |
|---|---|---|---|---|---|
| Native | 24 (12 screens x 2) | 80% | 19.2 | $0.18 | $3.46 |
| Banner | 12 | 90% | 10.8 | $0.05 | $0.54 |
| Interstitial | 12 (har tap) | 70% | 8.4 | $0.90 | $7.56 |
| **Total** | | | **38.4** | **$0.30 blended** | **$11.56** |

Yahan interstitial sirf 22% impressions hain, lekin revenue ka 65% wahi laate hain. Natives + banners 78% impressions
hain aur sirf 35% revenue. Yahi saste impressions blended eCPM ko $0.30 pe kheench laate hain, jo aapke dashboard wale
number se match karta hai (estimate).

**New funnel (Home + 6 steps + Ready), ek run, teen scenarios:**

| Format | Impressions / run (estimate) | A: same prices | B: prices sudhre | C: B + ~15% bidders lift |
|---|---|---|---|---|
| Native (8 slots x 85% fill) | 6.8 | $0.18 -> $1.22 | $0.30 -> $2.04 | $2.35 |
| Banner (7 screens x 90%) | 6.3 | $0.05 -> $0.32 | $0.07 -> $0.44 | $0.51 |
| Interstitial (paced, 2-4 per run, liya 3) | 3.0 | $0.90 -> $2.70 | $1.20 -> $3.60 | $4.14 |
| Rewarded (40% + 20% opt-in, 90% fill) | 0.54 | $1.50 -> $0.81 | $1.80 -> $0.97 | $1.12 |
| App open (returning users) | 0.3 | $0.60 -> $0.18 | $0.80 -> $0.24 | $0.28 |
| **Total impressions / run** | **16.9** | | | |
| **Revenue per 1,000 runs** | | **$5.23** | **$7.29** | **$8.39** |
| **Blended eCPM** | | **$0.31** | **$0.43** | **$0.50** |

(Scenario B ke saare prices estimate hain. C mein B ko 1.15x kiya hai, kyunki blog ne 15-30% claim kiya (R18); ye bhi estimate hai.)

**Is math ka honest nateeja:**

1. Blended eCPM $0.30 se $0.43-0.50 tak ja sakta hai (estimate). Lekin scenario A dikhata hai ki agar sirf ads kam kiye
   aur prices nahi sudhre, to eCPM lagbhag wahi rahega aur revenue per run aadhe se bhi kam ho jaayega ($11.56 -> $5.23).
2. **Revenue per funnel run $11.56 se $7.29-8.39 (per 1,000 runs) pe aa sakta hai, yaani 27-37% kam (estimate).**
   Break-even ke liye 1.4-1.6x zyada runs per DAU chahiye, ya zyada DAU (retention), ya real scanner/result screen pe extra
   compliant inventory (§7). Ye teeno cheezein A/B test mein measure karni hain. Maan ke mat chalo.
3. Is math mein ek bada risk shamil nahi hai: old layout policy ke bahar hai. Meta 14 din mein 70k impressions wali property
   ko 90-day click-quality review mein daalta hai, aur enforcement placement se app level tak escalate hota hai (research se R23).
   5M users pe ye threshold aaram se cross hota hai, to maan ke chalo ki review lagu hai (estimate). Google ya Meta ne demand limit ki,
   to old layout ka revenue achanak gir sakta hai.
4. Aapke analytics mein check karo ki old funnel mein kitne users 12th screen tak pahunchte hain. Agar drop-off zyada hai,
   to old "per run" numbers asal mein aur bhi kam hain.

**Isliye:** naya design "safe + price sudharne wala" base hai, jeet ki guarantee nahi. Asli judge ARPDAU A/B test hai ([§9](#9-rollout-plan-2-3-hafte)).

---

## 3. eCPM vs ARPDAU: kya optimise karna hai, kya track karna hai

### 3.1 Formulas

```
eCPM (format)   = revenue / impressions x 1000
ARPDAU          = din ka total ad revenue / DAU
                = SUM over formats [ (impressions per DAU)_f x eCPM_f ] / 1000
Ad LTV (approx) = SUM over days [ ARPDAU_day x retention_day ]        (estimate method)
```

### 3.2 eCPM badhana aur revenue badhana ek cheez kyun nahi hai

eCPM do tarike se badh sakta hai:

- **(a) Har impression ki price badhe.** Better viewability, kam accidental clicks, zyada bidders, rewarded. Ye asli growth hai.
- **(b) Saste impressions hata do.** Banner/native kam kar do to average mechanically upar jaata hai, lekin total revenue gir sakta hai.

Dashboard pe dono ek jaise dikhte hain: "eCPM up". Isliye hamesha **ARPDAU aur imp/DAU saath mein** dekho.
Rule of thumb (estimate): agar eCPM +40% hua aur imp/DAU -50% hua, to ARPDAU -30% hai. Ye jeet nahi hai.

Doosri taraf, agar ARPDAU flat hai lekin D7 retention +3pp hai, to LTV badha hai, aur ye jeet hai.

### 3.3 Kya track karna hai

| Metric | Kahan se | Kitni baar | Kyun |
|---|---|---|---|
| **ARPDAU** (total + per format) | MAX revenue / DAU (Firebase ya Play Console) | Roz, 7-day avg | Primary KPI |
| **Impressions per DAU, per format** | MAX reporting | Roz | eCPM change "(a)" hai ya "(b)", ye yahi batata hai |
| **eCPM per format per placement** | MAX reporting, placement breakdown (`step_next`, `unlock_hd`, `home_native`, ...) | Roz | Kaun si screen paisa banati hai, kaun sirf impression bharti hai |
| Fill rate, show rate | MAX | Roz | Pacing ke baad interstitial ready milta hai ya nahi |
| **Rewarded opt-in rate** | `unlock_detailed` impressions / step-4 views (analytics event chahiye) | Weekly | Reward kitna "wanted" hai |
| CTR per placement | MAX | Weekly | Redesign ke baad CTR girna aksar **achha** sign hai: accidental clicks kam hue |
| **D1 / D7 retention** | Firebase ya Play Console | Weekly cohorts | Guardrail |
| Session length, sessions/DAU, funnel completion (Home -> Ready) | Firebase screen_view / custom events | Roz | Content ka asar |
| Cumulative ad revenue per install (D0-D7) | Firebase `ad_impression` (ILRD, `RevenueTracker`) | Cohort | UA / LTV decisions |
| Policy health | AdMob Policy center, Meta Monetization Manager, Play Console | Weekly | Ek strike sab kuch zero kar sakta hai |

Note: native ki placement reporting approximate hai. MAX native ad pe placement LOAD time pe stamp karta hai, render time
pe nahi (SDK bytecode se verify kiya, R37). Agar exact per-screen native eCPM chahiye, to har screen ka alag native ad unit banao.

---

## 4. Naya screen design + ad placement map

### 4.1 Design rules jo layouts mein baked hain

1. Har screen pe **max 1 native**, options ke NEECHE, 32dp gap ke saath (`@dimen/xr_gap_ad`). "Sponsored" label card ke
   bahar hai, "Ad" badge aur AdChoices andar. Native kabhi buttons ke beech ya unko touch karta hua nahi.
2. Native slot ki **fixed height reserve** hai (356dp placeholder). Ad late load ho to bhi content finger ke neeche se nahi
   khiskta (research se R39: fixed space reserve karo).
3. **Banner sirf anchored adaptive**, screen ke bilkul neeche, scroll view ke bahar, nav bar ke upar (edge-to-edge insets handle kiye).
   Height 50-90dp, screen ka 15% se zyada nahi (SDK Javadoc, R37).
4. Options bade aur alag cards hain (question card min 88dp, choice tile min 176dp, 12dp gap). Ads ka style bhi alag hai.
5. Har screen pe real content hai: meaningful sawaal, sachcha X-ray fact card, disclaimer.
6. Naya theme: dark "x-ray" look (bg `#0B1220`, cyan `#22D3EE`), brand purple `#A78BFA` secondary mein. Hindi strings `values-hi` mein hain.

### 4.2 Screen A: QuestionActivity (`activity_question.xml`)

```
+--------------------------------------------+
| <-  Prank X-Ray Scanner                    |  toolbar
| [#######...............]   Step 1 of 6     |  step_progress + step_label
+--------------------------------------------+  -- scroll start --
| What do you want to scan?                  |  question_title
| Pick the X-ray effect to show...           |  question_subtitle
|                                            |
| +----------------------------------------+ |
| | (icon)  Hand                        >  | |  option_a  (card, min 88dp)
| |         27 bones, wrist to fingertips  | |
| +----------------------------------------+ |
|                 12dp                       |
| +----------------------------------------+ |
| | (icon)  Full body skeleton          >  | |  option_b
| |         All 206 bones, head to toe     | |
| |         [> Watch ad]  (sirf rewarded)  | |  option_chip (GONE jab tak rewarded nahi)
| +----------------------------------------+ |
|                 32dp  (koi ad nahi)        |
| Sponsored                                  |  native_ad_label (card ke bahar)
| +----------------------------------------+ |
| | [icon] Title           [Ad] [i]        | |  NATIVE (placeholder 356dp reserved)
| | Body text...                           | |
| | +------------------------------------+ | |
| | |          media 180dp               | | |
| | +------------------------------------+ | |
| | [            Install              ]    | |
| +----------------------------------------+ |
| +-- Did you know? -----------------------+ |  fact_card
| | Each human hand has 27 bones...        | |
| +----------------------------------------+ |
| For entertainment only. This app...        |  disclaimer
+--------------------------------------------+  -- scroll end --
| [   anchored adaptive BANNER 50-90dp   ]   |  banner_container (scroll ke bahar)
+--------------------------------------------+
|              (system nav bar)              |
+--------------------------------------------+
```

Pehli screen (fold) pe user ko sawaal + dono options + native ka upar wala hissa dikhta hai. Native poora dekhne ke liye
user fact card padhte hue scroll karta hai. Ye natural hai, trap nahi.

### 4.3 Screen B: ChoiceActivity (`activity_choice.xml`)

```
+--------------------------------------------+
| <-  Prank X-Ray Scanner                    |
| [##########............]   Step 2 of 6     |
+--------------------------------------------+
| Select skeleton model                      |  choice_title
| Male and female skeletons differ mainly    |  choice_subtitle
| in the pelvis.                             |
| +------------------+  +------------------+ |
| |      (icon)      |  |      (icon)      | |  choice_a | choice_b
| |       Male       |  |      Female      | |  tiles side by side,
| |  Narrower,       |  |  Wider, rounder  | |  min 176dp, 12dp gap
| |  heart-shaped    |  |  pelvis          | |
| |  pelvis          |  |                  | |
| +------------------+  +------------------+ |
|                 32dp                       |
| Sponsored                                  |
| +--- NATIVE (same template, 356dp) ------+ |  ONE native (pehle 2-3 the)
| +----------------------------------------+ |
| +-- Did you know? -----------------------+ |
| | A female pelvis is usually wider...    | |
| +----------------------------------------+ |
| disclaimer                                 |
+--------------------------------------------+
| [   anchored adaptive BANNER   ]           |
+--------------------------------------------+
```

"Select Gender" ab "Select skeleton model" hai, aur descriptions sirf haddiyon (pelvis) ki baat karte hain. Kapde ya body
ka koi zikr nahi (reason: [§8](#8-policy-risks)).

### 4.4 Home aur Ready screens

```
HOME (activity_main)                        READY (activity_scan_ready)
+-------------------------------+           +-------------------------------+
| Prank X-Ray Scanner           |           | <-  [##########] All set: ready|
| +---------------------------+ |           | Your scan is ready            |
| |  hero: hand bones on grid | |           | +-- Summary ---------------+  |
| |  X-Ray Scanner            | |           | | [Hand] [Male] [Neon] ... |  |  answers as chips
| |  6 easy steps  ...        | |           | +--------------------------+  |
| +---------------------------+ |           | +-- HD scan ---------------+  |
| [        Start scan         ] |           | | Sharper bones + glow     |  |
| For entertainment only...     |           | | [> Watch ad to unlock]   |  |  REWARDED (unlock_hd)
| Sponsored                     |           | +--------------------------+  |
| +--- NATIVE (home_native) --+ |           | [        Start scan        ]  |  INTERSTITIAL maybe (scan_start)
| +---------------------------+ |           |        Start over             |
| Did you know? ...             |           | Sponsored                     |
+-------------------------------+           | +--- NATIVE (ready_native) -+  |
| [ BANNER (home_banner) ]      |           | Did you know? / disclaimer    |
+-------------------------------+           +-------------------------------+   (NO banner)
```

### 4.5 Ad placement map (placement names MAX reporting mein dikhenge)

| Screen | Native | Banner | Fullscreen trigger |
|---|---|---|---|
| Home | `home_native` (1) | `home_banner` | "Start scan" tap -> `step_next` (pehla tap hamesha skip) |
| Question (A) | `question_native` (1) | `question_banner` | Answer tap -> `step_next` (paced), ya rewarded `unlock_detailed` |
| Choice (B) | `choice_native` (1) | `choice_banner` | Answer tap -> `step_next` (paced) |
| Ready | `ready_native` (1) | none | "Start scan" -> `scan_start` (major action), "HD" -> rewarded `unlock_hd` |
| App return (background se) | - | - | App open `app_open` |

Har format ka **ek hi ad unit** hai (5 units: interstitial, rewarded, app-open, native, banner). Screens placements se alag
hoti hain. Ek unit pe zyada volume aata hai to bidders jaldi seekhte hain (estimate / industry practice).

### 4.6 Naya 6-step flow (purane 7 + 5 = 12 filler screens ki jagah)

| # | Type | Step id | Sawaal | Option A | Option B | Jawab ka use | Fact card |
|---|---|---|---|---|---|---|---|
| 1 | A Question | `target` | What do you want to scan? | Hand | Full body skeleton | Scanner kya dikhaye | Haath mein 27 haddiyaan |
| 2 | B Choice | `model` | Select skeleton model | Male | Female | Skeleton model (sirf pelvis ka farq) | Female pelvis wider hota hai |
| 3 | A Question | `style` | Choose scan style | Classic X-ray | Neon glow | Visual style | Haddiyaan X-ray pe safed kyun dikhti hain (calcium) |
| 4 | A Question | `detail` | Would you like a detailed bone analysis? | **Yes, detailed (Watch ad, REWARDED)** | No, quick scan | Bone names + extra facts | Adult mein ~206 haddiyaan, newborn mein ~270 |
| 5 | B Choice | `camera` | Which camera? | Back camera | Front camera | Scanner camera | X-rays ki wavelength 0.01-10 nm |
| 6 | A Question | `sound` | Scanner sound effects? | On (beep + hum) | Off (silent) | Scanner sound | Rontgen, 1895, patni ke haath ki photo |
| - | Ready | - | Summary + HD unlock + Start scan | | | Sab answers chips mein | Stapes: sabse chhoti haddi, ~3mm |

(Facts ke numbers standard anatomy/physics facts hain. Ye app content hai, eCPM data nahi.)

**Per run ad count, old vs new:**

| | Old (12 screens) | New (Home + 6 + Ready) |
|---|---|---|
| Native | 24-29 (kai aadhe off-screen) | 8 (har screen pe 1, fully reachable) |
| Banner | 12 (buttons ke beech bhi) | 7 (sirf bottom anchored) |
| Interstitial | 12 attempts (har tap) | 2-4 (paced, estimate, [§5.1](#51-interstitial) walkthrough) |
| Rewarded | 0 | 0-2 (sirf opt-in) |
| App open | 0 | 0-1 (sirf returning user) |

### 4.7 Steps kam/zyada kaise karein

- **Sirf `flow/ScanFlow.java` ki `STEPS` list edit karo.** Progress bar, "Step X of N", navigation aur Ready screen sab
  automatically follow karte hain.
- Naye step ke liye: `FlowStep(id, Type.QUESTION|CHOICE, titleRes, subtitleRes, factRes, optionA, optionB)`. Strings
  `values/strings.xml` **aur** `values-hi/strings.xml` dono mein daalo. Fact `flow/XRayFacts.java` ke `STEP_FACTS` mein daalo.
  Option ka `rewarded=true` sirf tab karo jab wo option user ko kuch EXTRA de.
- Recommendation (estimate): **6 se 8 steps.** Agar A/B mein ARPDAU gire, to 1-2 meaningful steps jodo. Examples: "Bone labels
  on/off", "Scan speed slow/fast", "Grid colour blue/green". Har jawab scanner mein use hona chahiye. 12 filler screens pe wapas mat jao.
- Step count abhi remote-config se nahi badalta, uske liye code change chahiye. Agar step count ka A/B chahiye, to do lists
  ship karo aur ek remote flag se choose karo. Ye abhi bana nahi hai.

---

## 5. Fullscreen ads ke rules

### 5.1 Interstitial

Code: `InterstitialController.onNavigation()` har navigation tap pe call hota hai. Ad tabhi dikhta hai jab
`FrequencyCapper` allow kare **aur** ad pehle se ready ho. User ko ad load hone ka kabhi wait nahi karna padta. Rewarded ad
ke turant baad kabhi interstitial nahi aata. 5s watchdog bhi hai, taaki ad na khule to bhi user aage badh jaaye.

| `AdConfig` field (remote key) | Default (code default) | Matlab |
|---|---|---|
| `INTERSTITIAL_SKIP_FIRST_ACTIONS` (`interstitial_skip_first_actions`) | 1 | Session ka pehla tap kabhi ad nahi |
| `INTERSTITIAL_EVERY_N_ACTIONS` (`interstitial_every_n_actions`) | 2 | Zyada se zyada har 2nd tap |
| `INTERSTITIAL_MIN_INTERVAL_MS` (`interstitial_min_interval_ms`) | 30000 | Pichle interstitial se kam se kam 30s |
| `INTERSTITIAL_MAX_PER_SESSION` (`interstitial_max_per_session`) | 6 | Session cap |
| `FULLSCREEN_COOLDOWN_MS` (`fullscreen_cooldown_ms`) | 15000 | Koi bhi fullscreen (rewarded/app-open/interstitial) band hone ke 15s tak koi naya nahi |
| `SESSION_TIMEOUT_MS` (`session_timeout_ms`) | 1800000 (30 min) | Itna background = naya session (counters reset) |

Ready screen ka "Start scan" `onMajorAction()` use karta hai. Ye every-N counter ignore karta hai, lekin 30s gap, cooldown
aur session cap follow karta hai.

**Walkthrough (estimate: har screen pe ~8s, interstitial ~15s):**
Home tap (skip) -> S1 tap = **Ad #1** -> S2 (pichle ad ke baad sirf 1 tap, no) -> S3 (2 taps + 31s ho gaye) = **Ad #2** -> S4 "Yes, detailed" = rewarded
(interstitial nahi) -> S5 (no) -> S6 = **Ad #3** -> Ready "Start scan" (30s nahi hue, no).
Agar S4 pe "quick" chuna, to ~4 interstitials. Isliye per run 2-4 (estimate).

### 5.2 Rewarded (opt-in only)

- **Step 4 "Yes, detailed"** (`unlock_detailed`): option pe "Watch ad" chip dikhti hai. Reward mila to detailed analysis unlock.
  Agar user ne ad beech mein band kiya, to jawab "quick scan" ho jaata hai aur toast dikhta hai.
- **Ready "HD scan"** (`unlock_hd`): button se rewarded chalta hai, fir "HD unlocked" dikhta hai.
- `GRANT_REWARD_IF_NO_AD = true` (code default): agar ad available nahi, to user ko reward phir bhi milta hai. User ko kabhi punish mat karo.
- Rules: basic flow kabhi ad ke peeche lock mat karo. Reward wahi do jo user sach mein chahta hai. Research ke hisaab se
  utility apps mein rewarded tabhi chalta hai jab reward "wanted" ho (research se R30).

### 5.3 App open

| Field (remote key) | Default (code default) | Matlab |
|---|---|---|
| `APP_OPEN_ENABLED` (`app_open_enabled`) | true | |
| `APP_OPEN_MIN_LAUNCHES` (`app_open_min_launches`) | 2 | First-ever launch pe kabhi nahi |
| `APP_OPEN_MIN_BACKGROUND_MS` (`app_open_min_background_ms`) | 30000 | Sirf 30s+ background ke baad lautne pe |
| `APP_OPEN_COLD_START_WINDOW_MS` (`app_open_cold_start_window_ms`) | 4000 | Cold start pe sirf tab dikhao jab ad 4s ke andar load ho jaaye, user ko wait nahi karwana |

Code mein ye bhi hai: ad click karke bahar gaya user jab lautta hai, to app-open nahi dikhta. Kisi aur fullscreen ke upar
bhi app-open nahi dikhta.

### 5.4 Kabhi nahi (policy)

Ye sab Play Ads policy aur AdMob guidance se hain (research se R26, R20):

- Loading screen se pehle full-screen video interstitial nahi.
- App exit ya back-to-exit pe ad nahi.
- Ek ad band hote hi turant doosra ad nahi.
- Jab user kuch aur karne ja raha ho, tab achanak interstitial nahi.
- Har tap pe ad nahi.

### 5.5 A/B test ranges (sab estimate, policy limits ke andar)

| Remote key | Default | Test range | Note |
|---|---|---|---|
| `interstitial_every_n_actions` | 2 | 2-3 | **2 se neeche mat jao.** 1 ka matlab har tap, jo AdMob guidance ke khilaaf hai (R20) |
| `interstitial_min_interval_ms` | 30000 | 20000-60000 | |
| `interstitial_max_per_session` | 6 | 4-8 | |
| `interstitial_skip_first_actions` | 1 | 1-2 | 0 mat karo |
| `fullscreen_cooldown_ms` | 15000 | 10000-30000 | |
| `app_open_min_background_ms` | 30000 | 15000-60000 | |
| `app_open_min_launches` | 2 | 1-3 | 1 = pehle launch pe bhi (retention risk) |
| `native_preload_count` | 2 | 1-3 | Max 5 (code limit). Pooled natives expire ho jaate hain |
| `banner_on_question` / `banner_on_choice` | true | true/false | Banner hatane se native viewability/CTR pe kya asar padta hai |

Backstop: MAX dashboard mein bhi fullscreen units pe session ya time-based frequency cap lagao (research se R10).

---

## 6. AppLovin MAX dashboard checklist

Label ka matlab: "(research se Rn)" = source mila, lekin mostly search-snippet level ka. "(verify karo)" = exact
click-path official docs ya AppLovin account manager se confirm karo.

- [ ] **Ad units:** interstitial, rewarded, app open, banner, native. Native ke liye **Manual** integration use karo, template
      nahi. MAX native templates hataye ja rahe hain ("ads will start no-filling at the end of Q2 2025") (research se R12).
      Code manual `MaxNativeAdViewBinder` hi use karta hai. Har ad unit ID exactly 16 characters ka hona chahiye. Warna selective
      init us ID ko silently drop kar deta hai (SDK bytecode se verify, R37).
- [ ] **Bidders add karo (India):** MAX 20+ SDK bidders support karta hai (research se R18). InMobi MAX pe real-time bidder
      hai (research se R17). Pangle ko India ke liye mat gino, kyunki uski available-locations list mein India nahi hai (research se R16).
      Liftoff, DT Exchange, Mintegral, Unity, Moloco, BidMachine, PubMatic India ke liye usual candidates hain (industry knowledge,
      unverified). **Ek baar mein 1-2 network**, MAX A/B test ke saath.
- [ ] **Google bidding + AdMob:** MAX mein adapter ka naam "Google bidding and Google AdMob" hai. Google bidding serve ho, iske
      liye har ad unit pe partner bidding enable karni padti hai (research se R14). Kuch publishers hybrid setup bhi chalate hain:
      Google bidding + kuch AdMob waterfall line items, jinki country CPM bidding clear price se upar ho. Iska faayda unverified
      hai, sirf A/B se decide karo (verify karo).
- [ ] **Floors / segments:** bid floors per country set ho sakte hain. Jis country pe floor nahi, wahan floor nahi lagta.
      Auto-CPM waterfall prices ko network CPM ke saath align rakhta hai (research se R15). **India mein floors sirf fullscreen
      units pe aur sirf A/B mein test karo, banner pe kabhi nahi** (estimate). Aggressive floor = fill girega. SDK mein
      `MaxSegment`/`MaxSegmentCollection` hain (R37). Dashboard mein segment targeting ka exact UI verify karo.
- [ ] **Frequency caps (dashboard):** fullscreen/rewarded units pe session-based ya time-based cap (research se R10). Ye client
      pacing ke upar safety net hai.
- [ ] **Banner refresh:** per ad unit 10-120s, default "Optimized by MAX" (research se R11). Default se start karo, fir 30s
      vs optimized ka A/B karo (estimate). Code background mein refresh pause karta hai.
- [ ] **Ad Review:** creative gallery, risky creatives flag karta hai, aur report karne pe network block karta hai (research se R13).
      Setup ke liye Gradle plugin `com.applovin.quality:AppLovinQualityServiceGradlePlugin` chahiye, jo
      `https://artifacts.applovin.com/android` se aata hai, plus Ad Review key (Account > General > Keys). Ye plugin is repo
      mein **abhi nahi** joda gaya: key aapke account ki hai, aur build ke waqt `artifacts.applovin.com` reachable hona
      chahiye. Apni machine/CI pe jodo.
- [ ] **Brand safety:** Ad Review se specific dating/adult creatives report karo. AdMob mein "Significant Skin Exposure" blocking
      control 30 Jul 2025 se hat gaya hai (research se R28). Isliye advertiser URL block aur creative reporting use karo. Meta
      Monetization Manager ka block list bhi dekho (verify karo).
- [ ] **Placements reporting:** code har show/load pe placement bhejta hai (`step_next`, `scan_start`, `unlock_detailed`,
      `unlock_hd`, `app_open`, `*_native`, `*_banner`). Reporting mein placement breakdown dekho. Native placement approximate hai ([§3.3](#33-kya-track-karna-hai)).
- [ ] **A/B testing:** Ad unit > "..." > Create A/B Test. Result "Analyze > A/B Tests" mein aata hai, test group ke kam se kam
      10,000 impressions ke baad. Phir Promote ya Deprecate karo (research se R9).
- [ ] **Mediation Debugger:** debug build mein Home ke toolbar title pe long-press karo. Dekho har network "green" hai aur test ads aate hain.
- [ ] **Test devices:** apne phone ka GAID `AdUnits.TEST_DEVICE_GAIDS` mein daalo. Apne live ads pe click karna invalid traffic hai.
      Test mode mein selective init ka koi effect nahi hota (research se R40).
- [ ] **Terms & Privacy flow / CMP:** EEA/UK traffic ke liye Google ko certified CMP chahiye (research se R34). MAX ka flow
      Google UMP automate karta hai. AdMob > Privacy & messaging mein GDPR message publish karna padta hai (research se R36).
      Code mein `AdsManager.TERMS_FLOW_ENABLED` hai (default false). Kyun false: ise on karne pe India ke naye users ko bhi ek
      alert dikhta hai (SDK Javadoc, R37).

---

## 7. Content se eCPM kaise badhe (honest mechanism)

**Mechanism:** content khud bid nahi badhata. Bidders har impression ki "expected value" predict karte hain: user click
karega? install karega? app use karega? Content se ye chain chalti hai:

1. User genuinely engaged hai, to wo ad sach mein dekhta hai. Viewability aur time on screen badhte hain.
2. Clicks accidental ki jagah interested hote hain. CTR shayad kam ho, lekin CVR (install/purchase) upar jaata hai.
3. Network ka model placement/app ko "valuable" seekhta hai, aur agle din/hafte bids badhte hain.
4. Longer, retained sessions matlab zyada natural breaks, yaani zyada compliant impressions per DAU.

Ye **dheere (din-hafte) aur indirectly** hota hai. Iska koi fixed % promise nahi hai.

| Lever | Is repo mein status | Aage kya | Label |
|---|---|---|---|
| **Meaningful sawaal jinke jawab use hote hain** | Done: 6 steps, answers Ready screen pe chips mein | Asli scanner screen `ScanSession` padhe (style, camera, sound, target) | - |
| **Sachche X-ray facts** | Done: 9 facts (EN + HI), har step pe relevant fact | 30+ facts, "Bone of the day", rotation | - |
| **Hindi localisation** | Done: saare UI strings `values-hi` mein | Hindi store listing + screenshots. Baad mein Marathi/Bengali/Tamil/Telugu (estimate) | India mein 600M+ Hindi speakers, "50% more engagement" jaisa vendor claim (research se R31, marketing number) |
| **Rewarded value exchange** | Done: detailed analysis + HD | 3D skeleton rotate, bina watermark save, extra styles (neon colours), slow-mo scan video | Games mein rewarded se +15-30% ARPDAU reported (research se R30). Utility app ke liye kam ho sakta hai (estimate) |
| **Result / share screen** | Nahi bana (asli scanner aapke app mein hai) | Scan ke baad result image + bone labels + Share. Result screen interstitial ke liye natural break hai, aur native/MREC ke liye lamba dwell time. Share se organic installs aate hain | MREC aam taur pe banner ka 2-5x (research notes, unverified, R8) |
| **Scanner screen pe inventory** | Nahi (aapka existing screen) | Scanner screen pe bottom anchored banner (user wahan 30-60s rukta hai, refresh se 1-2 impressions). Ye §2.5 ke revenue gap ko bharne ka sabse compliant tarika hai | estimate |
| **Retention hooks** | - | Daily fact notification (opt-in, Android 13+ pe POST_NOTIFICATIONS permission), scan collection/gallery, weekly naye styles remote config se | estimate |
| **Geo mix** | - | en-US/es/pt-BR/id/de listing. Tier-1 majority apps ~3x revenue kamate hain same users pe | research se R7 |
| **Content rating** | - | General audience (13+/Teen) rakho, child-directed mark mat karo | eCPM 6-30%+ girta hai (research se R29) |

---

## 8. Policy risks

### 8.1 Google Play: "see through clothing" wale apps banned hain (sabse bada risk)

Play Developer Policy, Inappropriate Content > Sexual Content, "not allowed" list mein ye hai:

> "Apps that degrade or objectify people, such as apps that claim to undress people or see through clothing, even if
> labeled as prank or entertainment apps."
> https://support.google.com/googleplay/android-developer/answer/9878810 (research se R24, search-snippet: ye wording 3 alag
> search excerpts mein mili, page khud nahi khula)

- 2026 mein "nudify" apps pe media reports aayi hain (Jan 2026, 55 apps) (research se R35). Iska matlab enforcement ka risk aur bada hai.
- **Isliye "Select Gender" ko skeleton-only hona chahiye.** X-ray app mein "gender select karo" ka implicit matlab ban jaata hai
  "is insaan ke kapdon/body ke andar dekho". Naya step "Select skeleton model" hai: Male / Female **pelvis** ka farq, bas.
  App name, listing, screenshots, notifications, kahin bhi clothing/body/"see through" wording nahi honi chahiye.
- **Prank disclaimer protection nahi deta.** Play "Deceptive Behavior" policy un apps ko bhi rokti hai jo aisi functionality claim
  karein jo possible nahi hai, "even if represented as a prank, fake, joke". Metadata ko asli functionality batani chahiye (research se R25).
  Isliye app ko "simulated X-ray effect / X-ray simulator" ki tarah present karo. Har screen pe ye disclaimer hai (EN + HI):
  *"For entertainment only. This app simulates an X-ray effect; it cannot see through objects, bodies or clothing."*
- **Ads app ke content se mature nahi hone chahiye:** *"Any ads that appear in the app must not be significantly more mature in
  content than the primary content within the app itself."* https://support.google.com/googleplay/android-developer/answer/9859655
  (research se R26). Suggestive dating creatives isi wajah se risk hain.

### 8.2 AdMob / Google placement policies (research se R19-R22, R39; search-snippet)

- Banner interactive buttons (jaise "next") ke paas nahi. Late-load ke liye fixed space reserve karo.
- Interstitial: "no more than one interstitial ad after every two user actions". Har click pe interstitial non-compliant hai.
  Ek interstitial band hote hi doosra nahi, achanak nahi, app load ya exit pe nahi.
- Ads content se zyada nahi.
- Native: "Ad"/"Sponsored" attribution (localized), AdChoices overlay dikhna chahiye. Ad aur content saaf alag dikhne chahiye.

### 8.3 Meta Audience Network (research se R23, search-snippet)

- Native ko apne CTA buttons se door rakho. Border aur alag background do, beech mein space chhodo. White space clickable nahi hona chahiye.
- **14 din mein 70k impressions wali property 90-day click-quality review mein jaati hai.** Enforcement pehle placement pe hota
  hai, repeat violation pe poore app pe.

### 8.4 Consequences

- **Play:** update reject, app removal, repeat violations pe developer account termination (Play enforcement ka general pattern, verify karo).
- **AdMob / Google:** invalid-traffic deductions, Confirmed Click (research se R27), limited ad serving ya account disable
  (general AdMob enforcement, verify karo). Google aksar sabse bada bidder hota hai (estimate, apni MAX network report mein dekho), to uska jaana bahut bada nuksaan hai.
- **Meta:** placement disable, fir app-level (research se R23).

---

## 9. Rollout plan (2-3 hafte)

Sabse bada rule: **ek baar mein ek variable.** Client changes (layout, pacing) Firebase Remote Config se test hote hain. Dashboard
changes (bidders, floors, refresh) MAX A/B se. Dono ek hi hafte same ad unit pe mat chalao, warna result mix ho jaayega.

### 9.1 Remote config wiring (repo mein abhi Firebase nahi hai)

`AdConfig.applyRemote(Map<String,String>)` ready hai. Galat values ignore hoti hain. Keys ye hain:

```
ads_enabled, interstitial_skip_first_actions, interstitial_every_n_actions, interstitial_min_interval_ms,
interstitial_max_per_session, fullscreen_cooldown_ms, app_open_enabled, app_open_min_launches,
app_open_min_background_ms, app_open_cold_start_window_ms, session_timeout_ms,
native_on_home, native_on_question, native_on_choice, native_on_ready,
banner_on_home, banner_on_question, banner_on_choice,
native_preload_count, native_slot_timeout_ms, grant_reward_if_no_ad
```

Firebase jodne ke baad `XRayApp.onCreate()` mein ye pattern lagao (sketch hai, exact API Firebase docs se confirm karo):

```java
FirebaseRemoteConfig rc = FirebaseRemoteConfig.getInstance();
AdConfig.applyRemote(toStringMap(rc.getAll()));      // pichle launch pe activate hue values
AdsManager.get().initialize(this);
rc.fetchAndActivate().addOnCompleteListener(t ->      // naye values: pacing turant, init-time values next launch se
        AdConfig.applyRemote(toStringMap(rc.getAll())));
// toStringMap: har FirebaseRemoteConfigValue ka asString()
```

Firebase A/B Testing mein revenue ko goal banane ke liye `RevenueTracker` ka commented `ad_impression` code enable karo.

### 9.2 Plan

| Hafta | Kya | Kaise measure |
|---|---|---|
| **Week 0** (2-3 din) | Dashboard setup (§6): Manual native unit, Ad Review, test devices. Jo 2 bidders Week 2 mein test karne hain (jaise InMobi + Liftoff), unke accounts banao aur adapters `app/build.gradle` mein abhi uncomment karo. Adapter APK mein na ho to MAX A/B mein wo network test nahi hoga. Play listing text skeleton-only karo. Release build internal testing track pe daalo. Mediation Debugger sab green. | Checklist |
| **Week 1** | Play Console **staged rollout 5% -> 20%** (naya version). Iske andar Firebase RC experiment, 3 arms (estimate values): **A** defaults (every 2, 30s, max 6); **B** conservative (every 3, 45s, max 4); **C** aggressive-but-compliant (every 2, 20s, max 8). | ARPDAU per arm (7-day), D1, session length, crash-free, ANR. Naya version cohort vs purana version cohort ARPDAU (ye perfect A/B nahi hai, kyunki early updaters alag ho sakte hain) |
| **Week 2** | Guardrails theek hain to rollout 50%. Best pacing arm ko default banao. **MAX A/B #1** interstitial + rewarded units pe: test group mein 1-2 naye bidders. | MAX A/B: eCPM + revenue per test group (>=10k impressions, R9). Saath mein ARPDAU |
| **Week 3** | 100% rollout. **MAX A/B #2:** banner refresh 30s vs optimized, YA fullscreen India floor test (ek hi). RC test: `banner_on_question/choice` false vs true. | Same |

### 9.3 Success metrics

- **Primary:** ARPDAU (7-day avg) aur cumulative ad revenue per install D0-D7, control ke muqable.
- **Guardrails:** D1 aur D7 retention, sessions/DAU, avg session length, crash-free users, ANR rate, policy center status (AdMob, Meta, Play).
- **Diagnostic:** eCPM per format per placement, imp/DAU per format, fill/show rate, rewarded opt-in, CTR.

### 9.4 Rollback rule (thresholds estimate hain, apne volume ke hisaab se adjust karo)

- Kisi RC arm ka ARPDAU, 7+ din baad, control se **5%+ kam** hai **aur** D7 retention +2pp se zyada better nahi hai, to wo arm band karo.
- Crash-free users 99.5% se neeche jaaye ya ANR spike aaye, to Play Console mein **staged rollout halt** karo.
- AdMob policy email ya Meta warning aaye, to us placement ka `native_on_*` ya `banner_on_*` false karo (remote se turant).
  `ads_enabled=false` sirf emergency ke liye hai.
- Sach ye hai ki layout remote se wapas purana nahi hota. Jo users update le chuke hain, unke liye fix = naya patch release.
  Isliye staged rollout chhote % se shuru karo.

---

## 10. Setup steps (is code ke liye)

1. **SDK key + AdMob app id**: `local.properties` mein daalo (ye file git-ignored hai):
   ```
   applovin.sdk.key=YOUR_REAL_SDK_KEY
   admob.app.id=ca-app-pub-XXXXXXXXXXXXXXXX~YYYYYYYYYY
   ```
   CI pe `-PAPPLOVIN_SDK_KEY=... -PADMOB_APP_ID=...` pass karo. Agar key nahi hai, to app bina ads ke chalta hai (crash nahi).
   Default AdMob id Google ka public **sample** id hai. Release se pehle zaroor badlo. Google adapter ko ye meta-data chahiye,
   warna app start pe crash hota hai.
2. **Ad unit IDs**: `app/src/main/java/com/apkglobal/test/ads/AdUnits.java` mein `INTERSTITIAL`, `REWARDED`, `APP_OPEN`,
   `NATIVE_MEDIUM` (Manual native), `BANNER` bharo. Har ID exactly 16 alphanumeric characters ka hona chahiye, warna
   `isConfigured()` usko "not configured" maanta hai aur wo unit load nahi hota.
3. **Test device**: apna GAID `AdUnits.TEST_DEVICE_GAIDS` mein daalo.
4. **Privacy URL**: `AdsManager.PRIVACY_POLICY_URL` mein apni asli privacy policy daalo. Play listing ke liye bhi zaroori hai.
   India DPDP Rules 2025 phased lagu ho rahe hain, full obligations ~May 2027 tak (research se R33). EEA/UK traffic hai to
   `TERMS_FLOW_ENABLED = true` karo aur `com.google.android.ump:user-messaging-platform` dependency jodo.
5. **Extra bidders**: pehle MAX dashboard mein network enable karo, fir `app/build.gradle` mein uska adapter uncomment karo.
   Version AppLovin Integration Manager se match karo. Mintegral ke liye `settings.gradle` mein Mintegral maven repo bhi uncomment karo.
   Pangle India users ke liye kaam ka nahi, kyunki wahan available nahi (R16).
6. **Asli scanner jodo**: `ui/ScanReadyActivity.startScan()` mein Toast ki jagah apna scanner activity start karo. Answers `ScanSession.get()` se padho.
7. **Build/test**: `./gradlew assembleDebug`, `./gradlew test` (unit tests: `FrequencyCapperTest`, `AdConfigTest`, `AdUnitsTest`).
   Gradle wrapper 8.14.3, AGP 8.13.0, JDK 17, compile/target SDK 36, minSdk 24 (AppLovin 13.6.4 ki requirement).
8. **Debug**: debug build mein Home toolbar title pe long-press karo, Mediation Debugger khulega. Logcat tag `XRayAds` mein har
   paid impression ka revenue/network/placement dikhta hai.
9. **Analytics (recommended)**: Firebase Analytics + Remote Config jodo. `RevenueTracker` ka `ad_impression` code uncomment karo, aur §9.1 wiring lagao.
10. **Listing**: title, description, screenshots skeleton-only aur "simulator" framing mein. Kapde/body wala koi text ya image nahi. Content rating general audience.
11. **Apne live app se match karo**: `app/build.gradle` mein `applicationId` (abhi `com.apkglobal.test`) ko apne Play app ke package
    se match karo, aur `versionCode` (abhi 2) ko live version se bada rakho. Warna Play update accept nahi karega.

---

## 11. Sources

Tag meaning: **snippet** = search-result excerpt, page nahi khula. **confirmed** = page ya SDK khud padha. **unverified** = research team ka industry experience, koi source nahi.

| # | Claim | URL | Tag |
|---|---|---|---|
| R1 | Tier-3 (India/Indonesia) eCPM aam taur pe $1-3 (format nahi bataya, shayad fullscreen) | https://maf.ad/en/blog/mobile-ads-ecpm/ | snippet |
| R2 | India Android interstitial $1.04 (Mar) -> $0.98 (Jun 2023) | https://appodeal.com/blog/mobile-ecpm-report-app-ad-monetization-worldwide-performance/ | snippet |
| R3 | Appodeal Q4 2024: banner sub-$1 har jagah (US $0.68), interstitial US $14.08, rewarded US $16.49 | https://appodeal.com/wp-content/uploads/2025/03/Appodeal-The-Latest-eCPM-Report-2025.pdf | snippet |
| R4 | Global AdMob averages: banner $0.20-0.80, interstitial $2.50-5.00, rewarded $8-18 | https://www.playwire.com/blog/admob-ecpm-benchmarks-what-publishers-should-expect | snippet |
| R5 | H1-2025: India mein sirf Android rewarded video grow hua (+3.60%), banners 20%+ gire | https://bidlogic.io/2025/07/25/ecpm-growth-in-mobile-apps-q1-q2-2025-analysis-and-insights/ | snippet |
| R6 | Q4 2025: India Android interstitials +5.57%, banners -0.63% | https://bidlogic.io/2026/01/30/what-happened-to-mobile-app-ecpms-in-q4-2025/ | snippet |
| R7 | Tier-1 majority apps ~3x revenue. Emerging markets: fill + frequency optimise karo. App-open India trending up | https://revenueflex.com/blog/app-ad-revenue-benchmarks-2026/ , https://www.monetizemore.com/blog/ecpm-insights/ | snippet |
| R8 | India working ranges: banner $0.02-0.10, MREC $0.08-0.40, native $0.10-0.60, interstitial $0.40-1.50, app-open $0.30-1.20, rewarded $0.80-3.00 | research notes (no public source) | unverified |
| R9 | MAX A/B test flow, >=10,000 test-group impressions | https://support.applovin.com/en/max/advanced-features/a-b-testing | snippet |
| R10 | MAX frequency caps: session / time based | https://support.axon.ai/en/max/max-dashboard/ad-units/create-an-ad-unit | snippet |
| R11 | Banner refresh 10-120s, default "Optimized by MAX" | https://support.applovin.com/en/max/android/overview/advanced-settings | snippet |
| R12 | Native templates: "no-filling at the end of Q2 2025", manual use karo | https://support.applovin.com/en/max/android/ad-formats/native-ads | snippet |
| R13 | Ad Review features + Gradle plugin setup | https://support.applovin.com/en/max/ad-review/overview | snippet |
| R14 | "Google bidding and Google AdMob" adapter, per ad unit partner bidding | https://support.applovin.com/en/max/android/preparing-mediated-networks | snippet |
| R15 | Country bid floors, Auto-CPM | https://support.applovin.com/en/max/max-dashboard/networks/auto-cpm | snippet |
| R16 | Pangle available locations mein India nahi | https://ads.tiktok.com/help/article/available-locations-for-pangle-ads | snippet |
| R17 | InMobi MAX pe bidder | https://support.inmobi.com/monetize/integrating-inmobi-with-mediation/audience-bidding/applovin-max | snippet |
| R18 | MAX 20+ bidders. Blog: South Asia mein 15-30% eCPM lift vs AdMob alone | https://ottomancoder.medium.com/flutter-app-monetization-admob-vs-applovin-max-in-2026-with-real-numbers-from-south-asian-b428eee10196 , https://www.applovin.com/blog/a-closer-look-at-how-in-app-bidding-can-increase-monetization/ | snippet (lift number ka source kamzor) |
| R19 | Banner buttons ke paas nahi | https://support.google.com/admob/answer/6128877 , https://support.google.com/admob/answer/6275345 | snippet |
| R20 | "no more than one interstitial ad after every two user actions", har click pe interstitial non-compliant | https://support.google.com/admob/answer/6201362 , https://support.google.com/admob/answer/6201350 | snippet |
| R21 | Ads content se zyada nahi | https://support.google.com/publisherpolicies/answer/11169917 | snippet |
| R22 | Native attribution + AdChoices | https://support.google.com/admob/answer/6239795 | snippet |
| R23 | Meta AN best practices. 70k imp / 14 din -> 90-day click-quality review | https://www.facebook.com/audiencenetwork/resources/blog/meta-audience-network-policy-top-5-best-practices | snippet |
| R24 | Play: "see through clothing ... even if labeled as prank" | https://support.google.com/googleplay/android-developer/answer/9878810 | snippet (3 excerpts) |
| R25 | Play Deceptive Behavior: impossible functionality "even if ... prank" | https://play.google.com/about/privacy-security-deception/deceptive-behavior/dishonest-behavior/ | snippet |
| R26 | Play Ads policy: interstitial rules. Ads app se zyada mature nahi | https://support.google.com/googleplay/android-developer/answer/12271244 , https://support.google.com/googleplay/android-developer/answer/9859655 | snippet |
| R27 | Invalid traffic deductions. Confirmed Click se CPC/eCPM girta hai | https://support.google.com/admob/answer/3342054 , https://support.google.com/admob/answer/10094971 | snippet |
| R28 | Category blocking auction pressure ghatata hai. "Significant Skin Exposure" control 30 Jul 2025 se hata | https://support.google.com/admob/answer/3150176 , https://support.google.com/admob/answer/16368266 | snippet |
| R29 | Child-directed tagging se eCPM 6-30%+ girta hai | https://www.gamebizconsulting.com/newsletter/admon-newsletter-11-the-shrinking-island-ad-monetization-underage-users | snippet |
| R30 | Rewarded: games mein +15-30% ARPDAU. Reward "wanted" hona chahiye | https://adjoe.io/blog/increase-arpdau-guide/ | snippet |
| R31 | Hindi 600M+ speakers, localisation engagement (vendor claim) | https://www.transperfect.com/blog/how-multilingual-marketing-drives-higher-engagement-indian-markets | snippet |
| R32 | 31 Aug 2026 se targetSdk 36 zaroori | https://developer.android.com/google/play/requirements/target-sdk | confirmed |
| R33 | DPDP Rules 13 Nov 2025 notified, phased, full ~13 May 2027 | https://www.pib.gov.in/PressReleasePage.aspx?PRID=2190014 | snippet |
| R34 | EEA/UK/CH: Google-certified CMP (IAB TCF) chahiye | https://support.google.com/admob/answer/13554116 | snippet |
| R35 | Jan 2026: 55 "nudify" apps report | https://www.cnbc.com/2026/01/27/apple-google-host-dozens-of-ai-nudify-apps-like-grok-report-finds.html | snippet |
| R36 | MAX Terms & Privacy flow Google UMP automate karta hai | https://support.applovin.com/en/max/android/overview/terms-and-privacy-policy-flow | snippet |
| R37 | AppLovin SDK 13.6.4 API/behaviour (16-char ids, native placement at load, creative debugger default on, terms alert outside GDPR, banner height) | javap + sources of `applovin-sdk 13.6.4` | confirmed (SDK) |
| R38 | Meta ~2s bounce accidental click charge nahi karta | https://techcrunch.com/?p=1524391 | snippet |
| R39 | Fixed ad space reserve karo. Banner proximity = accidental clicks ka bada cause | https://support.google.com/admob/answer/2936217 , https://blog.google/products/admob/admob-banner-ad-implementation-guidance/ | snippet |
| R40 | Selective init (`setAdUnitIds`), test mode mein effect nahi | https://support.applovin.com/en/max/android/overview/advanced-settings | snippet |
