# LankaLens Google Play release checklist

## Android app
- [x] Package: `lk.lankalens.app`
- [x] App name: LankaLens
- [x] Production URL: `https://lankalens.lk/`
- [x] Target / compile SDK: API 36
- [x] HTTPS-only configuration
- [x] Real-device-tested WebView app
- [x] Listing photo upload/compression working
- [x] Android-only Post Ad button adjustment working
- [x] Release version `1.2.0` / versionCode `3`
- [x] Branded launcher icon and Android 12+ splash prepared
- [x] Secure environment-based release signing configuration
- [x] Manual GitHub workflow for signed Play Store AAB

## Signing
- [ ] Create the permanent upload keystore on a trusted computer
- [ ] Back up the keystore and passwords securely
- [ ] Add the four GitHub Actions signing secrets listed in `SIGNING_SETUP.md`
- [ ] Run **Play Store release bundle** and download the signed `.aab`

## Store graphics
- [ ] 512×512 Play Store icon
- [ ] 1024×500 feature graphic
- [ ] Phone screenshots from the final Android build

## Play Console
- [ ] Create the LankaLens app
- [ ] Enable Play App Signing
- [ ] Category: Shopping
- [ ] Website: `https://lankalens.lk`
- [ ] Support email: `support@lankalens.online`
- [ ] Add listing copy from `PLAY_STORE_LISTING.md`
- [ ] Publish a reviewed privacy policy at a public HTTPS URL
- [ ] Complete Data Safety using the app's actual data practices
- [ ] Complete content rating and target audience
- [ ] Add reviewer/app-access instructions if login is required

## Testing
- [ ] Upload signed AAB to Internal testing first
- [ ] Install the Play-distributed build on a real Android phone
- [ ] Re-test login, registration, images, shops, search, favourites, Post Ad, messages, social links and back navigation
- [ ] Complete any testing requirement shown in the Play Console account
- [ ] Promote the tested build to Production

## Security rule
Never commit the keystore, store password, key password or GitHub secret values. Keep a secure backup of the upload key for future app updates.
