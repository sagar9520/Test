# Prompt: ad jaldi dikhe (preload) + naye feature ke notification

> Abhi jo kaam chal raha hai, wo pehle poora karo. Ye uske baad karna.

**Rules:**
- Shuru karne se pehle plan aur time batao, aur mera OK lo.
- Har hisse ke baad local commit karo. **GitHub push mat karna.**
- Ads ka logic aur ad IDs, JSON aur Cashfree mat badalna, jab tak neeche saaf na likha ho.

## Ye kaam BILKUL nahi karna (Play Store / AdMob / AppLovin policy)
- App ko Recents (recent apps list) se chhupana: `excludeFromRecents` ya aisa kuch bhi.
- Sirf app ko zinda rakhne ya ad ke liye **foreground service** ya hamesha chalne wali background service lagana.
- App band hone ke baad, ya jab app screen par nahi hai, tab koi bhi ad dikhana: interstitial, app-open, ya overlay.
- Home screen par ya dusre app ke upar kuch bhi dikhana (`SYSTEM_ALERT_WINDOW`).
- Battery optimization band karne ki permission maangna.

## Hissa 1: Ad pehle se taiyar rahe (preload), sahi tareeke se
1. App khulte hi (splash / Application class) interstitial aur app-open ad **load karna shuru** karo, taaki pehla ad dikhane ke time wo ready ho.
2. Ad dikhne ke turant baad **agla ad load** kar do. Har ad ke time naya load ka intezaar na ho.
3. Loaded ad ko Application-level manager mein rakho (ek hi object). Har screen apna alag ad load na kare.
4. **App-open ad** (agar app mein hai):
   - sirf tab dikhe jab user khud app ko wapas foreground mein laaye;
   - 4 ghante se purana loaded ad mat dikhao, naya load karo;
   - splash ke turant baad interstitial ke saath dono ek saath na aayein.
5. Ad load fail ho to thodi der baad dobara try karo (jaise 30s, phir 60s, phir 120s), lekin sirf jab app khula ho.
6. App poori tarah band (process khatam) hone par loaded ad memory se chala jata hai. Ye normal hai, sab apps mein aisa hi hota hai. Agli baar app khulte hi point 1 se naya load ho jayega.
7. Premium user, ya JSON se ads band → kuch load mat karo.

## Hissa 2: Naye feature / reminder ke notification
1. **Firebase Cloud Messaging (FCM)** lagao (agar pehle se nahi hai), taaki main Firebase console se kabhi bhi notification bhej sakun. Jaise: "Naya feature aaya: Bone Zoom Scan, abhi try karo".
   - Iske liye app mein koi background service nahi chahiye. Android khud notification deliver karta hai, app band ho tab bhi.
   - Notification par tap karne se seedha wahi screen khule. Data payload mein `screen` key bhejunga, jaise `live_scan`, `photo_editor`, `bone_zoom`, `reports`, `premium`, `full_body`.
2. **Local reminder** (WorkManager se), jaise "Aaj apna X-ray prank try karo 💀".
   - Din mein zyada se zyada 1 notification.
   - Agar user us din app khol chuka hai, to us din reminder nahi.
   - Agar JSON mein notifications on/off ka setting pehle se hai, to wahi follow karo. Naya JSON key mat banao.
3. **Android 13+ par `POST_NOTIFICATIONS` permission:**
   - app khulte hi turant mat maango;
   - onboarding khatam hone ke baad, ya pehla scan save karne ke baad, ek chhoti si samjhane wali screen ya popup dikhao ("Naye features aur effects ki khabar pao"), phir system wali permission maango;
   - user mana kare to baar-baar mat maango.
4. Notification channel banao: "Updates & new features".
   - Small icon wahi safed silhouette icon ho jo logo wale kaam mein bana tha.
   - Rangeen photo small icon mein mat lagana.
5. Notification par tap karne se app normal tareeke se khule (splash ke saath), phir sahi screen. Agar user premium nahi hai aur us screen par popup ka rule hai, to wahi rule chale.

## Aakhir mein
- Test karo:
  - app khulte hi ad ready ho;
  - ad ke baad agla ad load ho;
  - app band karke dobara kholo to sab theek chale;
  - Firebase console se test notification bhejo aur tap karne par sahi screen khule;
  - Android 13 par permission wala flow chale.
- Report do: kya badla, kaunsi permissions add hui, aur Firebase setup ke liye mujhe kya karna hai (jaise `google-services.json`).
- **GitHub push mat karna.**
