# Prompt: Background service (opt-in) + notification → rewarded ad → 24 ghante free

> Abhi jo kaam chal raha hai, wo pehle poora karo. Ye uske baad karna.

**Rules:**
- Shuru karne se pehle plan aur time batao, aur mera OK lo.
- Har hisse ke baad local commit karo. **GitHub push mat karna.**
- Cashfree, ads ke unit IDs aur purane JSON keys mat badalna.

## Maqsad
Mere users Tier-3 ke hain. Wahan ad load hone mein bahut time lagta hai, aur log na subscription lete hain na baar-baar video ad dekhna chahte hain. Isliye:
1. User ki **marzi se** (opt-in) app ki ek background service chalegi. Isse ads app ke andar pehle se loaded rahenge aur jaldi dikhenge.
2. Service chalu ho to user ko notification aayenge. Notification se app khulega, user khud "Watch ad" dabayega, rewarded ad chalega, aur **app 24 ghante ke liye free** ho jayega.
3. 24 ghante free hone par service band ho jayegi.

## Pakke rules (inhe todna nahi hai)
- **Ad sirf app ke andar**, jab app screen par khula ho. Service, notification, home screen ya kisi dusre app ke upar kabhi ad nahi.
- App ko Recents se **mat chhupana** (`excludeFromRecents` nahi).
- `SYSTEM_ALERT_WINDOW` (overlay) nahi. Battery optimization band karne ki permission nahi.
- Service sirf user ke "Allow" dabane ke baad shuru ho. Mana karne par kabhi zabardasti nahi.
- Service ka notification hamesha dikhe, aur usme **"Stop"** button ho. Settings mein bhi on/off ka option ho.
- Rewarded ad **apne aap shuru nahi hoga**. User "Watch ad · Get 24h free" button dabayega tabhi chalega.

## Hissa 1: Consent popup (disclaimer)
1. **Kab dikhe:**
   - non-premium user ko;
   - onboarding ke baad, ya pehla scan karne ke baad;
   - sirf ek baar. User mana kare to 3 din baad ek baar aur, phir kabhi nahi.
2. **Popup ka text** (app ke naye popup design mein, logo ke saath):
   - Title: "Get the app FREE for 24 hours"
   - Points:
     - "Keep Prank X-Ray Scanner ready in the background so ads load faster inside the app."
     - "No ads will ever show outside the app."
     - "You'll get a few notifications. Open one and watch 1 short ad to use the app free for 24 hours."
     - "You can stop this anytime from the notification or Settings."
   - Buttons: **Allow** / **Not now**
3. **Allow** dabane par:
   - Android 13+ par `POST_NOTIFICATIONS` permission maango.
   - Permission mili to service shuru karo.
   - Permission nahi mili to service shuru mat karo.

## Hissa 2: Foreground service
1. `ForegroundService` banao. Manifest mein:
   - `FOREGROUND_SERVICE` permission;
   - Android 14+ ke liye `FOREGROUND_SERVICE_SPECIAL_USE` permission;
   - `android:foregroundServiceType="specialUse"`;
   - `<property android:name="android.app.PROPERTY_SPECIAL_USE_FGS_SUBTYPE" android:value="..."/>` jisme saaf likho ki service kya karti hai.
2. **Service ka notification** (permanent, low priority, silent):
   - Text: "X-Ray Scanner is ready · Tap to open"
   - Action button: **Stop**
   - Small icon: safed silhouette wala icon.
3. **Service kya karegi:**
   - App ke ad manager ke through interstitial aur rewarded ad loaded rakhegi.
   - Loaded ad expire hota hai (lagbhag 1 ghante mein), isliye **har 55 minute mein** sirf tab naya load kare jab purana use na hua ho. Isse zyada baar load mat karo.
   - Network na ho to load try mat karo.
4. **Service band karo jab:**
   - user Stop dabaye;
   - 24h free mil jaye;
   - user premium ho jaye;
   - JSON se ads band ho jayein.
5. Phone restart hone par service apne aap **dobara shuru mat karo**. Agli baar user app kholega tab chalegi, agar consent ON hai.

## Hissa 3: Notifications (sirf service ON hone par)
1. **Frequency:**
   - default: **1 ghante mein 2 notification**;
   - raat 10 baje se subah 9 baje tak koi notification nahi;
   - din mein zyada se zyada 20 notification.
   - Ye teeno values ek constant/config mein rakho, taaki main baad mein kam-zyada kar sakun.
2. **Text:** alag-alag aur mazedaar ho. Jaise:
   - "💀 Your free X-ray pass is waiting! Tap to unlock 24h free"
   - "New: Bone Zoom Scan. Try it free today"
3. **Tap karne par:**
   - app khule (splash, phir ek "Unlock 24h free" screen);
   - us screen par button "Watch ad · Get 24h free";
   - **reward mil jaye** (ad poora dekha) → 24 ghante free: koi interstitial nahi, koi unlock popup nahi, saare features khule. Service aur notifications band;
   - reward na mile (ad beech mein band, ya load nahi hua) → "Try again" button, aur kuch free nahi hoga.
4. **24 ghante khatam hone par:**
   - ek notification: "Your free day ended. Get another 24h free?";
   - user dobara Allow kare to service phir se shuru ho sakti hai.
5. 24h free ka time SharedPreferences mein save karo, taaki app band hone ya phone restart ke baad bhi sahi rahe.

## Hissa 4: Settings / control
- App ke Settings mein "Background ready mode" ka toggle ho (on/off).
- Premium user ke liye ye poora feature band.
- Cashfree, premium aur JSON ka logic wahi rahe.

## Aakhir mein
- **Test:**
  - Allow aur Not now dono;
  - Android 13, Android 14 aur Android 15;
  - Stop button;
  - notification tap → rewarded → 24h free → service band;
  - 24h baad wapas normal;
  - raat ke time notification na aaye;
  - koi ad app ke bahar na aaye.
- **Play Console ke liye** ek chhota document banao: service kya karti hai, user kaise allow aur stop karta hai, aur screen recording ke steps. Ye foreground service declaration aur video proof ke kaam aayega.
- Report do. **GitHub push mat karna.**
