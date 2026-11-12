# حجره‌چی Android WebView 1.2.0

Official source project for a fullscreen Android WebView wrapper of https://hojrechi.com.
Splash and icon are generated from user-supplied branded images stored in branding/*.b64.
The app uses no native toolbar, supports Android system Back gestures and pull-to-refresh, file picking, cookies, downloads and payment deep links.

Build via GitHub Actions on branch hojrechi-android-build, then download app-debug.apk from the workflow artifact.

Note: The debug APK is signed with a workflow debug key; it is for testing, not distribution.
Do not disable Play Protect. Test sign-in, installments, PDF upload and bank payment return before release.
