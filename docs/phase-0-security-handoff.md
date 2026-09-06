# Phase 0 security handoff

## Completed in the Android project

- Gemini is no longer injected into `BuildConfig` and cannot be packaged in an APK.
- The direct Google AI client dependency has been removed.
- AI requests fail safely until an authenticated server-side gateway is delivered.
- Password reset only succeeds with Firebase's verified email-link code.
- Unit tests cover the removal of the former demo reset paths.
- CI verifies the safeguards, lint, unit tests, and debug build.

## Required console actions (project owner)

1. Revoke the Gemini key currently stored in `local.properties`; assume it has been exposed through previous APK builds.
2. Create a replacement key only for the future server-side AI gateway. Do not add it to Android, Gradle, or CI variables used by the mobile build.
3. Restrict the replacement key to its intended API and service account/network identity.
4. Review Google Cloud and Gemini usage logs for unexpected requests, then set quota and billing alerts.
5. Record the revocation date and the key owner in the team's secret register.

Do not re-enable AI in the Android client. Phase 4 will connect it to an authenticated, rate-limited backend gateway.
