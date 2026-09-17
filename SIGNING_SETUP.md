# LankaLens Android release signing

The Play Store release uses a permanent upload keystore. Never commit the keystore or its passwords.

## 1. Create the upload key locally

Run this on a trusted computer with Java installed:

```bash
keytool -genkeypair -v \
  -keystore lankalens-upload.jks \
  -alias lankalens-upload \
  -keyalg RSA -keysize 4096 -validity 10000
```

Choose a strong store password and key password. Keep the `.jks` file and passwords in at least two secure locations.

## 2. Add GitHub Actions secrets

In `naflan222/lankalens-android` add these repository secrets:

- `ANDROID_KEYSTORE_BASE64` — base64 of `lankalens-upload.jks`
- `ANDROID_KEYSTORE_PASSWORD`
- `ANDROID_KEY_ALIAS` — normally `lankalens-upload`
- `ANDROID_KEY_PASSWORD`

Encode the keystore with:

```bash
base64 -w 0 lankalens-upload.jks
```

On macOS use:

```bash
base64 < lankalens-upload.jks | tr -d '\n'
```

## 3. Build the signed Play Store bundle

GitHub → Actions → **Play Store release bundle** → **Run workflow**.

Download the `lankalens-play-store-aab` artifact and upload the `.aab` to Google Play Console.

## Security

Do not paste the keystore, passwords, or secret values into source files, issues, pull requests, or chat. The repository ignores `*.jks`, `*.keystore`, and `keystore.properties`.
