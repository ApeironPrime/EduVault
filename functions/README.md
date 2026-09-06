# EduVault business backend

All callable functions use region `asia-southeast1`, require Firebase Authentication and enforce App Check.

| Callable API | Caller | Server-authoritative behavior |
|---|---|---|
| `submitDocument` | authenticated owner | validates Storage path, size and MIME type; creates only `PENDING` metadata |
| `approveDocument` | admin custom claim | approves a pending document and credits its author exactly once |
| `unlockDocument` | authenticated user | verifies approval and deducts one credit exactly once |
| `reportDocument` | authenticated user | records at most one report per user/document and increments the aggregate safely |
| `recordQuizAttempt` | authenticated user | calculates score and XP from stored answers; ignores client XP/score |
| `setUserBlocked` | admin custom claim | blocks or unblocks a user |
| `deleteDocument` | admin custom claim | deletes a document and its owned Storage object |

## Integration rule

Android must call these APIs through the Firebase Functions client SDK. It must not write credits, XP, `status`, `reportCount`, `isBlocked`, or `unlockedDocuments` to Firestore directly. This integration is deliberately not applied to the currently modified upload/auth repositories, so their in-progress user changes remain intact.

## Local commands

```powershell
npm.cmd install
npm.cmd run build
```

Deploy only after App Check is in monitoring mode and the staging Rules tests pass:

```powershell
npx.cmd firebase-tools deploy --only functions --project appeduvault
```
