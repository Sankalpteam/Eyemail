# native/ — the Eyemail Android app

Kotlin + Jetpack Compose, package `in.eyemail.app`, minSdk 24, targetSdk 35.

The screens must match the web app exactly: same blue, same spacing, same words.

## What is in this first push

Three screens, wired to the same endpoints the web app calls:

| Screen | File | Calls |
|---|---|---|
| Sign in | `ui/screens/LoginScreen.kt` | Firebase sign-in over REST |
| Create account | `ui/screens/SignupScreen.kt` | `/api/auth/signup`, sign-in, `/api/auth/verify-signup-otp`, `/api/auth/register-mailbox`, `/api/auth/send-signup-otp` |
| Reset password | `ui/screens/ForgotPasswordScreen.kt` | `/api/auth/forgot-password` |

Shared pieces: `ui/theme/Theme.kt` holds the colour tokens, `ui/components/Components.kt`
holds the field, the buttons, the mark and the alert, `data/AuthRepository.kt` holds every
call in one place, `MainActivity.kt` holds the three way switch and the back handling.

## Build it

1. Open the `native/` folder in Android Studio (it is the project root).
2. Create `native/local.properties` next to this file with:

```
sdk.dir=/path/to/Android/sdk
BACKEND_URL=https://eyemail-backend.onrender.com
FIREBASE_API_KEY=<the apiKey from firebase-config.js in the repository root>
```

The key is read from `local.properties`, never committed, so nothing secret sits in the repo.

3. Run on a phone or an emulator with Android 7 or newer.

## Not done yet, on purpose

- The inbox, reader and settings screens. Signing in lands on a plain screen that says so.
- A signed release build. It needs a keystore, which the owner holds, and the Android SDK,
  which this repository does not carry.
- Play Store listing: account, privacy policy page, screenshots, data safety form.
