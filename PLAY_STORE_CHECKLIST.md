# LankaLens Google Play Release Checklist

## App build
- [x] Package name: `lk.lankalens.app`
- [x] App name: LankaLens
- [x] Production URL: `https://lankalens.lk/`
- [x] Target / compile SDK: API 36
- [x] HTTPS-only Android configuration
- [x] Fresh Android WebView app tested on a real Android device
- [x] Listing photo picker/upload works, including Android-side compression
- [x] Android-only Post Ad button sizing fixed
- [x] Release version prepared as `1.2.0` (`versionCode 3`)
- [x] Secure Gradle signing configuration reads credentials from environment variables
- [x] GitHub release workflow supports a signed Play Store AAB using repository secrets
- [x] Keystore files and keystore properties are excluded by `.gitignore`
- [ ] Create the permanent upload keystore and store it securely
- [ ] Add the four signing secrets to GitHub Actions
- [ ] Run the `Play Store release bundle` workflow and download the signed `.aab`

## Branding
- [x] App label is LankaLens
- [x] LankaLens branded Android 12+ splash screen configured
- [ ] Confirm final launcher icon artwork before production submission
- [ ] Prepare a 512x512 Play Store icon
- [ ] Prepare a 1024x500 feature graphic
- [ ] Capture current Android phone screenshots

## Play Console listing
- [ ] Create the LankaLens app in Google Play Console
- [ ] Enable Play App Signing
- [ ] App category: Shopping
- [ ] Add support email: `support@lankalens.online`
- [ ] Add website: `https://lankalens.lk`
- [ ] Add short description and full description from `PLAY_STORE_LISTING.md`
- [ ] Publish a privacy policy at a public HTTPS URL and add it to Play Console
- [ ] Complete the Data Safety form using the actual LankaLens backend data practices
- [ ] Complete the content rating questionnaire
- [ ] Add reviewer/app-access instructions if Google needs a test account

## Testing and release
- [ ] Upload the signed `.aab` to Internal testing first
- [ ] Install the Google Play-distributed build on a real Android phone
- [ ] Re-test login, registration, listing photos, shops, search, favorites, Post Ad, messaging, social links and back navigation
- [ ] Complete any testing requirement shown by your Play Console account
- [ ] Promote the tested build to Production

## Important security rule
Never commit the upload keystore, store password, key password, or secret values to GitHub. Keep a separate secure backup of the upload keystore because it is needed for future updates.
