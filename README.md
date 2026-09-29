# X-Ray Scanner (Prank X-Ray Scanner): redesign + AppLovin MAX ads

Android app (Java, package `com.apkglobal.test`). Ye ek **X-ray skeleton simulator** hai. Ye sirf entertainment ke liye
X-ray jaisa effect dikhata hai, asli scan nahi karta. Is version mein 3 cheezein nayi hain:

- Purane 12 filler screens (7 question + 5 "Select Gender") ki jagah **6 meaningful steps**.
- **Policy-safe ad placements:** 1 native per screen, sirf bottom pe anchored banner, paced interstitials, opt-in rewarded, app-open.
- Hindi (`values-hi`) + English UI.

eCPM badhane ka poora plan (diagnosis, math, MAX dashboard checklist, rollout) yahan hai:
**[docs/ECPM_GROWTH_PLAN.md](docs/ECPM_GROWTH_PLAN.md)**

## Screen map

```
Home --Start scan--> 1 Target (Q) --> 2 Skeleton model (C) --> 3 Style (Q) --> 4 Detail (Q, rewarded opt-in)
     --> 5 Camera (C) --> 6 Sound (Q) --> Ready (summary, HD rewarded, Start scan) --> [your scanner screen]

Q = QuestionActivity (2 big option cards)     C = ChoiceActivity (2 side-by-side tiles)
```

| Screen | Native | Banner | Fullscreen |
|---|---|---|---|
| Home | `home_native` | `home_banner` | Interstitial `step_next` (session ke pehle tap pe kabhi nahi) |
| Question / Choice | `question_native` / `choice_native`, options ke neeche | `question_banner` / `choice_banner` (anchored) | Paced interstitial `step_next`, ya rewarded `unlock_detailed` |
| Ready | `ready_native` | - | Interstitial `scan_start`, rewarded `unlock_hd` |
| Background se wapas aane pe | - | - | App open `app_open` |

## File map

```
app/src/main/java/com/apkglobal/test/
  XRayApp.java                 Application: AdsManager.initialize()
  MainActivity.java            Home (debug build: toolbar title long-press = Mediation Debugger)
  ads/
    AdUnits.java               MAX ad unit ids (16 chars) + placement names  <- APNE IDs YAHAN
    AdConfig.java              Saare ad tunables + applyRemote(Map) for Remote Config / A/B
    FrequencyCapper.java       Interstitial pacing (pure Java, unit-tested)
    AdsManager.java            SDK init (selective init), privacy URL, facade
    InterstitialController     Paced interstitial, user kabhi wait nahi karta
    RewardedController         Opt-in rewarded, ad na ho to bhi reward
    AppOpenController          App open: returning users, first launch pe nahi
    NativeAdController/Slot    Manual native, preload pool, fixed-height slot
    BannerController/Handle    Anchored adaptive banner
    RevenueTracker             Impression-level revenue log (+ Firebase ad_impression example)
    LoadRetry, Once            Backoff retry, run-once helper
  flow/
    ScanFlow.java              6 steps ki list  <- steps kam/zyada yahan karo
    FlowStep, ScanSession      Step model, user ke answers
    XRayFacts.java             Har step ka sachcha X-ray fact
  ui/
    BaseStepActivity           Common step logic: header, fact, native, banner, answer -> ad -> next
    QuestionActivity           Screen A
    ChoiceActivity             Screen B
    ScanReadyActivity          Summary + HD unlock + Start scan (apna scanner yahan jodo)
    StepNavigator, StepHeader, FactCard, OptionViews, TapGuard, Insets
app/src/main/res/layout/       activity_* screens, view_native_ad_medium (MAX manual template), view_* includes
app/src/main/res/values(-hi)/  English + Hindi strings
app/src/test/.../ads/          FrequencyCapperTest, AdConfigTest, AdUnitsTest
docs/ECPM_GROWTH_PLAN.md       eCPM / ARPDAU plan (Hinglish)
```

## Setup

1. `local.properties` mein ye daalo (file git-ignored hai):
   ```
   applovin.sdk.key=YOUR_REAL_SDK_KEY
   admob.app.id=ca-app-pub-XXXXXXXXXXXXXXXX~YYYYYYYYYY
   ```
   Key nahi hai to app bina ads ke chalega. Default AdMob id Google ka **sample** id hai, release se pehle badlo.
2. `ads/AdUnits.java` mein 5 MAX ad unit IDs daalo: interstitial, rewarded, app open, native (**Manual**), banner.
   Apna test phone ka GAID `TEST_DEVICE_GAIDS` mein daalo.
3. `ads/AdsManager.java` mein `PRIVACY_POLICY_URL` set karo. EEA/UK traffic hai to `TERMS_FLOW_ENABLED = true` karo
   aur UMP dependency jodo.
4. Extra bidders (InMobi, Liftoff, DT Exchange, Mintegral, ...): pehle MAX dashboard mein enable karo, fir
   `app/build.gradle` mein adapter uncomment karo (Mintegral ke liye `settings.gradle` mein repo bhi).
5. `applicationId` / `versionCode` ko apne live Play app ke hisaab se set karo.
6. `ScanReadyActivity.startScan()` mein apna asli scanner screen start karo. Answers `ScanSession.get()` se milenge.
7. Build: `./gradlew assembleDebug`. Tests: `./gradlew test`.
   Toolchain: Gradle 8.14.3, AGP 8.13.0, JDK 17, compileSdk/targetSdk 36, minSdk 24, AppLovin SDK 13.6.4.

Policy note: app, listing aur screenshots mein clothing/body/"see through" wording kahin nahi honi chahiye. Google Play
aise apps ban karta hai, prank label ke saath bhi. Details [plan ke §8](docs/ECPM_GROWTH_PLAN.md#8-policy-risks) mein hain.
