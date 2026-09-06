# AI gateway security runbook

`generateAiContent` is the only path from the Android client to a generative model. It is a callable Cloud Function in `asia-southeast1` and requires both Firebase Authentication and App Check.

## Before deployment

1. Create a new, server-only Gemini API key in Google AI Studio. Do not reuse a key that has ever shipped in an APK; revoke that old key.
2. Store the new key in Firebase Secret Manager. Never put it in `.env`, `local.properties`, Gradle, source code, or CI logs:

   ```powershell
   firebase functions:secrets:set GEMINI_API_KEY
   ```

3. Deploy the Functions. The secret is bound only to `generateAiContent`:

   ```powershell
   firebase deploy --only functions:generateAiContent
   ```

4. Optionally choose a supported model through the Firebase parameter prompt / environment configuration. The default is `gemini-2.5-flash`; changing it requires a Functions deploy.

## Controls included

- Authentication + App Check are enforced before model invocation.
- Inputs have strict field, length and history bounds; document context is capped at 30,000 characters.
- Each account is limited to 30 calls and 120,000 input characters per UTC day. A rejected model response still consumes a reservation to resist retry abuse.
- `aiUsage` stores counters; `aiAudit` stores only request ID, account ID, operation, model, size, status and latency. Prompts and model outputs are never persisted or logged.
- The gateway gives the model an education-only system instruction and tells it to treat user content as untrusted. Gemini service safety policies remain active.
- The Android implementation contains no AI SDK, API key, or direct model call. It validates structured JSON before displaying quiz/doc-viewer content.

## Operational follow-up

- Restrict access to Secret Manager and rotate the secret on a schedule or after suspected exposure.
- Add a retention policy for `aiAudit` / `aiUsage` and an alert for repeated quota or model failures.
- For stronger enterprise isolation, migrate the gateway from a Gemini API key to Vertex AI using the Cloud Functions service identity, then remove `GEMINI_API_KEY`.
- Before production, test App Check with real Play Integrity devices and review model safety settings against the organisation's policy.
