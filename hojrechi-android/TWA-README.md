# Hojrechi Trusted Web Activity (TWA) v1.5.1
This branch is a TWA migration of the Android release using the same Android package: com.hojrechi.app.
**Existing release SHA256**: 5E:03:4F:EA:96:CC:B0:8B:8E:F3:2B:25:5C:75:AE:CE:49:F4:AB:D3:89:53:46:5E:EA:2F:F1:5E:EE:BC:20:B8
Unsigned release must be signed offline with the existing private Bazaar production keystore; NEVER commit the key or password.

## Site requirements
Install `Hojrechi-TWA-WordPress-Plugin.zip` on hojrechi.com so https://hojrechi.com/.well-known/assetlinks.json returns 200 JSON with the exact package and release certificate fingerprint.
The WordPress plugin additionally serves a PWA manifest and icons.
Without Digital Asset Links, Chrome falls back to a Custom Tab with URL bar; this is a SECURITY feature.

## Compatibility
- Existing production app: com.hojrechi.app, versionCode 140
- This TWA update: com.hojrechi.app, versionCode 151
- Same signing certificate is required for in-place updates.
- Back navigation comes from Chrome Custom Tabs. Pull-to-refresh depends on the installed browser's implementation. The former WebView native loading badge cannot be injected into Chrome's TWA UI; page UX is governed by the website.

## Build
GitHub Actions: Hojrechi TWA 1.5.1 builds unsigned release, tests APK installation and opens Chrome, and uploads a debug APK for verification. Actual domain verification is required on a device with Chrome and deployed assetlinks.
