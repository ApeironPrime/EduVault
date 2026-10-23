# EduVault business backend

All callable functions use region `asia-southeast1`, require Firebase Authentication and enforce App Check.

| Callable API | Caller | Server-authoritative behavior |
|---|---|---|
| `submitDocument` | authenticated owner | validates Storage path, size and MIME type; creates only `PENDING` metadata |
| `approveDocument` | admin custom claim | approves a pending document and awards contribution XP exactly once |
| `unlockDocument` | authenticated user | legacy community-mode API; not used by the enterprise training UI |
| `reportDocument` | authenticated user | records at most one report per user/document and increments the aggregate safely |
| `dismissDocumentReports` | admin custom claim | clears the report queue through a server-authoritative write |
| `recordQuizAttempt` | authenticated user | calculates score and XP from stored answers; ignores client XP/score |
| `setUserBlocked` | admin custom claim | blocks or unblocks a user |
| `deleteDocument` | admin custom claim | deletes a document and its owned Storage object |
| `upsertCategory` | admin custom claim | creates or updates a training-skill category |
| `deleteCategory` | admin custom claim | deletes a training-skill category |
| `getMyTrainingProgram` | authenticated user | auto-assigns and returns the current onboarding programme |
| `updateTrainingModuleCompletion` | authenticated user | validates a module and recalculates completion server-side |

## Integration rule

Android must call these APIs through the Firebase Functions client SDK. It must not write XP, `status`, `reportCount`, `isBlocked`, training completion, categories, or other administrative fields to Firestore directly.

Debug Android builds use Firebase's App Check debug provider. Register the token printed in Logcat in Firebase Console before calling an App Check-enforced function from an emulator. Release builds use Play Integrity.

## Local commands

```powershell
npm.cmd install
npm.cmd run build
```

Deploy only after App Check is in monitoring mode and the staging Rules tests pass:

```powershell
npx.cmd firebase-tools deploy --only functions --project appeduvault
```
