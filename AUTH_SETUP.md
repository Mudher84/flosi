# Flosi — Google-first authentication setup

Flosi now uses this account flow:

1. New account starts with Google approval.
2. After Google succeeds, the user must create a strong password for the same verified Google email.
3. Existing users can sign in with the verified email + password.
4. Password recovery is sent by Firebase Authentication to the same Google email.
5. The app fails closed if cloud authentication is not configured.

## Firebase console

Create or use the Flosi Firebase project, then enable these Authentication providers:

- Google
- Email/Password

Add the Android app with package name:

`com.flosi.app`

Add the SHA-1 and SHA-256 fingerprints for the signing certificate used to build the app.

## Required Gradle properties

Do not commit live credentials to the repository. Put these values in the build environment or local Gradle properties:

```properties
FLOSI_FIREBASE_API_KEY=...
FLOSI_FIREBASE_APP_ID=...
FLOSI_FIREBASE_PROJECT_ID=...
FLOSI_GOOGLE_WEB_CLIENT_ID=...
```

`FLOSI_GOOGLE_WEB_CLIENT_ID` must be the Web OAuth 2.0 client ID used by Google Identity/Firebase Authentication, not the Android OAuth client ID.

## Password policy

The app requires all of the following before enabling the create-password button:

- at least 12 characters
- at least one lowercase letter
- at least one uppercase letter
- at least one digit
- at least one non-alphanumeric symbol

## Password reset

The reset screen uses Firebase `sendPasswordResetEmail` and therefore sends the reset link to the verified account email. Firebase email templates can be branded from Authentication → Templates in Firebase Console.
