# LankaLens Android

Android Trusted Web Activity (TWA) wrapper for https://lankalens.lk.

This repository is intentionally separate from the production LankaLens website repository so Android development and Play Store releases do not redeploy or modify the live Flask/PostgreSQL site.

## App identity

- App name: `LankaLens`
- Application ID: `lk.lankalens.app`
- Launch URL: `https://lankalens.lk/`
- minSdk: 23
- targetSdk / compileSdk: 36
- Version: 1.0.0 (`versionCode 1`)

## Build

Use JDK 17 and Gradle 8.13:

```bash
gradle :app:assembleDebug
gradle :app:bundleRelease
```

The release bundle is created under `app/build/outputs/bundle/release/`.

## Signing and Digital Asset Links

Do **not** commit `.jks`, `.keystore`, passwords, or `keystore.properties`.

For Google Play, create an upload key locally and enable Play App Signing. After the real SHA-256 signing certificate fingerprint is known, replace the placeholder in `assetlinks.template.json` and publish the final file on the website at:

`https://lankalens.lk/.well-known/assetlinks.json`

Until Digital Asset Links are configured with the real signing certificate, the app may fall back to a Custom Tab instead of a verified full-screen TWA.
