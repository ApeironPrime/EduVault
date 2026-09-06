# Firebase security deployment

## Local verification

From this folder, install the development dependencies once and run:

```powershell
npm.cmd install
npm.cmd run test:rules
```

## Deploy safely

1. In Firebase Console, register Android App Check with Play Integrity. Start in **monitoring** mode; enforce only after confirming real-device traffic is valid.
2. Authenticate Firebase CLI with a project-owner account and deploy to staging first:

```powershell
npx.cmd firebase-tools deploy --only firestore:rules,firestore:indexes,storage,functions --project appeduvault
```

3. Verify the signed-in user experience on staging. The new policy intentionally blocks client-side credit, XP, moderation, and self-approval changes; use the callable APIs in `functions/` for those operations.
4. Grant the initial administrator claim from a secured terminal; do not use a mobile app or Firestore field for this:

```powershell
$env:ALLOW_ADMIN_CLAIM = 'true'
$env:FIREBASE_SERVICE_ACCOUNT_JSON = Get-Content -Raw C:\secure\service-account.json
npm.cmd run grant:admin -- <firebase-auth-uid>
```

The administrator must sign out and sign in again after a claim is changed.
