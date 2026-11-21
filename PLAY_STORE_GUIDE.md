# Google Play Internal Testing & Store Guide (`Media Button Relay`)

This guide walks you through uploading **`app-release.aab`** to **Google Play Console** for **Internal Testing** first (fastest path to install legitimately from the Play Store on your Pixel 9a), and covers what's needed if you later move to Production.

---

## Before You Start

1. **Back up your upload keystore!**
   * Keep safe copies of `keystore/upload-keystore.jks` and `keystore.properties` (never commit them to a public Git repo).
   * Google Play requires every future update to be signed with this same key.
2. **Host your Privacy Policy online**
   * Play Console requires a live `https://` Privacy Policy URL (even for testing / app content review).
   * Quickest free method: push this repo to GitHub and enable **GitHub Pages** serving from the `/docs` folder — your URL will be `https://<your-username>.github.io/<repo-name>/privacy-policy.html`.
3. **Create a Google Play Console account** (one-time $25 fee + ID verification at [play.google.com/console](https://play.google.com/console)).

---

## Step 1: Create the App in Play Console

1. Click **Create app**.
2. **App name:** `Media Button Relay`
3. **Default language:** English (United States)
4. **App or game:** App
5. **Free or paid:** Free
6. Accept the declarations and click **Create app**.

---

## Step 2: Complete App Content (Required Before Publishing Any Track)

Go to **Policy and programs → App content** and complete each section:

* **Privacy policy:** Paste your hosted `https://.../privacy-policy.html` URL.
* **App access:** *"All functionality is available without special access"* (no login required).
* **Ads:** *"No, my app does not contain ads."*
* **Content rating:** Fill out IARC questionnaire → select Utility/Productivity → **No** to all violence/mature questions.
* **Target audience:** 18+ (or 13+ / General utility; avoid "Under 13" unless you want Families policy review).
* **Data safety:**
  * *"Does your app collect or share any of the required user data types?"* → **No** (the app has zero network permissions and stores preferences locally only).
* **Accessibility API Declaration (Important!):**
  * Declare that your app uses the **AccessibilityService API** (specifically key-event filtering only).
  * **Do NOT claim it is an accessibility tool for disabilities** (`isAccessibilityTool` is `false`).
  * **Explanation you can paste:**
    > *Media Button Relay is a deterministic hardware key remapper for external Bluetooth media remotes/presenters. Many compact Bluetooth remotes emit Volume Up/Down key events instead of media keys; this app lets the user remap single, double, or triple presses from their external remote to standard media controls (Play/Pause, Next Track, Previous Track) without altering their phone's physical volume buttons. Screen content retrieval is disabled (`canRetrieveWindowContent="false"`), no UI automation is performed, and the app has no internet permission.*
  * **Short video link (if Play Console asks):** Record a ~20-second screen recording on your phone showing:
    1. Opening `Media Button Relay`.
    2. Tapping **Enable Relay Service** → clearly showing the **Accessibility Permission Disclosure** dialog → tapping **I Understand — Open Settings** → turning on `Media Button Relay` in Accessibility Settings.
    3. Returning to the app and pressing your Bluetooth remote button so the **Live Button Test** card updates.

---

## Step 3: Store Listing Graphics

In **Grow users → Store presence → Main store listing**:
* **App icon (512 × 512):** Upload `store-assets/icon-512.png`
* **Feature graphic (1024 × 500):** Upload `store-assets/feature-graphic-1024x500.png`
* **Phone screenshots (at least 2):** Take 2 screenshots of `Media Button Relay` on your Pixel 9a (main screen + the Prominent Disclosure dialog) and upload them.

---

## Step 4: Upload to Internal Testing & Install on Your Pixel 9a

1. Go to **Testing → Internal testing** → **Create new release**.
2. Under **App bundles**, upload:
   `app/build/outputs/bundle/release/app-release.aab`
3. Add release notes (e.g., `Initial release`) → **Save** → **Review release** → **Start rollout to Internal testing**.
4. Under the **Testers** tab, create an email list containing **your own Google Play Store email address** and check its box.
5. Copy the **Opt-in URL** ("How testers join your test") and open it on your Pixel 9a.
6. Tap **Accept invite** → tap **Download it on Google Play** → **Install**.
7. **Uninstall the old sideloaded prototype (`com.cheerplay.relay`)** from your Pixel (`Settings → Apps → CheerPlay Relay → Uninstall`), then enable **Media Button Relay** in **Settings → Accessibility**.

---

## Optional Later: Public Production Release

If you ever want anyone on Google Play to install it publicly:
* For personal Play Console accounts created after Nov 13, 2023, Google requires **12 unique testers opted into Closed Testing continuously for 14 consecutive days** before unlocking the **Production** track.
* For **Internal Testing** (just for yourself and up to 100 invited testers), there is **no 12-tester / 14-day requirement**.
