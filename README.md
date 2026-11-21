# Media Button Relay

An offline, minimal-permission Android utility (`se.mediabuttonrelay.app`, API 26–36) that remaps hardware volume button presses from paired external Bluetooth media remotes/presenters into standard media playback commands while leaving your phone's physical side volume buttons untouched.

## Features

- **Customizable Multi-Press Actions:** Map **Single Press**, **Double Press**, and **Triple Press** independently to *Play / Pause*, *Next Track*, *Previous Track*, *Stop*, *Mute / Unmute*, or *Do Nothing*.
- **Adjustable Click Speed:** Tune the multi-press detection window (`250 ms`–`1000 ms`, default `650 ms`). Fires immediately with zero extra wait once the highest configured click count is reached.
- **Trigger on Release Option:** Built for remotes (such as CheerPlay) that emit key events on button release.
- **External Remotes Only Option:** Filters events to connected external input devices so your phone's physical volume buttons behave normally.
- **Live Button Test:** Real-time readout of incoming remote keys and dispatched media commands inside the settings screen.

## Privacy & Accessibility Compliance

- **100% Offline:** Declares zero `INTERNET`, location, storage, or other sensitive permissions.
- **Minimal Accessibility Scope:** Uses `flagRequestFilterKeyEvents` (`canRequestFilterKeyEvents="true"`) solely to intercept hardware volume keys from external remotes. Screen content retrieval is disabled (`canRetrieveWindowContent="false"`), and no UI events (`typeAllMask`) are requested.
- **Prominent In-App Disclosure:** Displays a clear disclosure modal before navigating to Android Accessibility Settings.
- See [docs/privacy-policy.html](docs/privacy-policy.html) for the full privacy policy.

## Building Locally

Requirements: JDK 17, Gradle 9.x, Android SDK Platform 36 (`compileSdk = 36`, `targetSdk = 36`, `minSdk = 26`).

```bash
gradle testDebugUnitTest assembleDebug
```

To build a signed release APK and Play Store App Bundle (`.aab`), create your own upload keystore and `keystore.properties` (never commit signing keys or `local.properties`):

```properties
storeFile=keystore/upload-keystore.jks
storePassword=YOUR_STORE_PASSWORD
keyAlias=upload
keyPassword=YOUR_KEY_PASSWORD
```

Then run:

```bash
gradle assembleRelease bundleRelease
```

See [PLAY_STORE_GUIDE.md](PLAY_STORE_GUIDE.md) for Google Play Console Internal Testing steps.
