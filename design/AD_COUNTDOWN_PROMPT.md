# Prompt: interstitial se pehle 5 second countdown loader

> Pehle jo kaam chal raha hai wo poora karo. Ye uske baad karna.

Abhi interstitial se pehle ek gol ghoomta hua loader dikhta hai. Uski jagah ek premium **5 second countdown** lagana hai. Design aur code taiyar hai.

Reference code is repo mein hai: `sagar9520/Test`, branch `claude/magical-babbage-744gai`. Ise apne app ke package aur structure ke hisaab se adapt karna, seedha copy mat karna.

**Java files:**
- `app/src/main/java/com/apkglobal/test/ads/CountdownRingView.java`: gol ring jo ghadi ki disha mein khaali hoti hai, beech mein 5…4…3…2…1.
- `app/src/main/java/com/apkglobal/test/ads/AdCountdownDialog.java`: full-screen dialog, uske andar dark card. Card mein "Ad" badge, ring, "Ad starts in N seconds", ek chhoti line, aur optional "Remove ads" button hai.

**Resources:**
- `layout/dialog_ad_countdown.xml`
- `drawable/bg_countdown_card.xml`, `drawable/bg_countdown_remove_ads.xml`, `drawable/ic_crown_small.xml`
- `values/colors.xml`: saare `countdown_*` colors.
- `values/strings.xml`: `countdown_*` strings aur `plurals/countdown_title`.
- `values/styles.xml`: `Theme.AdCountdown`.
- Font: Poppins (app mein pehle se hai).

Design preview: `design/ad_countdown_preview.png` aur `design/ad_countdown_preview.gif`.

## Kaise lagana hai
1. App mein jahan bhi interstitial dikhta hai (AppLovin MAX aur AdMob fallback dono), purane loader ki jagah ye flow lagao:
   - Ad pehle se loaded hai, ya load ho raha hai → `AdCountdownDialog.show(activity, 5, ...)` dikhao. Countdown khatam hone par ad dikhao.
   - Countdown khatam hone par bhi ad load nahi hua → bina ad ke seedha aage badho. User ko atakna nahi chahiye.
   - Ad load fail ho gaya, ya frequency logic (JSON) ke hisaab se is baar ad nahi dikhana → **countdown bhi mat dikhao**. Countdown sirf tab aaye jab ad sach mein aane wala ho.
2. Premium user, ya JSON se ads band hon → countdown aur ad dono nahi.
3. **"Remove ads"** button:
   - sirf tab dikhao jab JSON mein Cashfree ON ho;
   - click par Premium screen khule;
   - user wahan se bina kharide wapas aaye, to wahi kaam aage chale jo ad ke baad hona tha.
4. Ad band hone par wahi next step chale jo abhi chalta hai.
5. Activity band ho jaye ya app background mein chala jaye to crash ya memory leak nahi hona chahiye. Dialog mein iska handling pehle se hai, check kar lena.
6. Reward ads par ye countdown **nahi** lagana. Wahan user khud button dabata hai. Ye sirf interstitial ke liye hai.
7. Ads ka load/show logic, ad unit IDs, JSON aur Cashfree mat badalna. Sirf purana loader badalna hai.

Shuru karne se pehle time batao aur mera OK lo. Kaam ke baad local commit karo, **GitHub push mat karna**. Aakhir mein build karke ek screen recording ya screenshot do.
