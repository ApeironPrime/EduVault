# Product quality gates

Every change must satisfy these executable gates before release:

1. `:app:lintDebug` — Android static analysis.
2. `:app:testDebugUnitTest` — ViewModel and domain behavior.
3. `:app:assembleDebug` — developer distribution.
4. `:app:assembleRelease` — R8/minification/resource shrinking compatibility.
5. `functions/npm run build` — backend TypeScript contract compilation.

The CI workflow runs all of these. Release builds now enable code and resource shrinking, while preserving source/line metadata for Crashlytics-compatible stack traces.

## Product acceptance checklist

- A user action must expose a disabled/loading state and a readable failure state.
- Retryable network failures must leave the screen usable; never trap the user in a spinner.
- Icon-only controls require meaningful content descriptions. Decorative icons remain explicitly null.
- Screens must use lifecycle-aware state collection for ViewModel flows.
- New repository behavior needs both success and failure tests, as demonstrated by `FlashcardViewModelTest`.
- Release APKs must be tested on a physical device for authentication, upload, App Check and AI gateway flows before publishing.
