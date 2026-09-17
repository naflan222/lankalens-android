# LankaLens Android Release Signing

The release build is configured to use a private upload keystore without committing it to the repository.

## Required GitHub Actions secrets
Add these repository secrets before running the `Play Store release bundle` workflow:

- `ANDROID_KEYSTORE_BASE64` — base64-encoded upload keystore file
- `ANDROID_KEYSTORE_PASSWORD` — keystore password
- `ANDROID_KEY_ALIAS` — upload-key alias
- `ANDROID_KEY_PASSWORD` — key password

## Generate the upload keystore
Run this once on a trusted computer with Java installed:

```bash
keytool -genkeypair \
  -v \
  -keystore lankalens-upload.jks \
  -alias lankalens-upload \
  -keyalg RSA \
  -keysize 4096 \
  -validity 10000
```

Choose strong passwords and keep them private. Do not paste the keystore or passwords into source code, issues, pull requests, chat screenshots or public files.

## Convert the keystore to base64 for GitHub Secrets
Linux/macOS:

```bash
base64 < lankalens-upload.jks | tr -d '\n'
```

Windows PowerShell:

```powershell
[Convert]::ToBase64String([IO.File]::ReadAllBytes("lankalens-upload.jks"))
```

Copy the resulting text into the `ANDROID_KEYSTORE_BASE64` GitHub Actions secret.

## Build locally with signing
Set these environment variables before running Gradle:

- `ANDROID_KEYSTORE_PATH`
- `ANDROID_KEYSTORE_PASSWORD`
- `ANDROID_KEY_ALIAS`
- `ANDROID_KEY_PASSWORD`

Then run:

```bash
gradle :app:bundleRelease
```

The resulting bundle is:

`app/build/outputs/bundle/release/app-release.aab`

## Backup rule
Keep at least two secure backups of the upload keystore and its passwords in separate safe locations. The keystore is needed to upload future LankaLens updates. Google Play App Signing should hold the final app-signing key; this local key is the upload key used to authenticate future releases.
