# CineHD TV - Personal Android TV App

This is a dedicated Android TV application (WebView Wrapper) configured specifically for Android TV / Google TV.

### Key TV Features Included:
- **Leanback Launcher Support**: App icon displays directly on the Android TV home screen banner.
- **TV Remote Navigation**: Handles DPAD navigation, OK button clicks, and BACK button behavior.
- **HTML5 Video Fullscreen**: Full support for `WebChromeClient.onShowCustomView` so video expands full screen on TV without crashing.
- **Ad/Redirect Protection**: Blocks rogue third-party popup tabs/redirects.
- **No Gesture Requirement**: Media playback starts smoothly without touch gesture restrictions.

---

### How to Build the APK:

#### Method 1: Using Android Studio (Local PC)
1. Open Android Studio.
2. Click **Open** and select the folder: `c:\Users\sumit\Desktop\New folder\CineHD_TV_App`.
3. Wait for Gradle sync to complete.
4. Go to top menu: **Build** -> **Build Bundle(s) / APK(s)** -> **Build APK(s)**.
5. Once finished, click **locate** to get `app-debug.apk`.
6. Transfer to your TV using a USB Drive or the **Send Files to TV** app.

#### Method 2: GitHub Actions (Cloud Build - Zero Software Needed)
1. Create a free GitHub repository (private or public).
2. Upload this `CineHD_TV_App` folder to your repo.
3. Go to the **Actions** tab on your GitHub repository.
4. Run the **Build Android TV APK** workflow.
5. In 2 minutes, download the compiled `.apk` directly from GitHub Artifacts!

