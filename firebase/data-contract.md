# Document data contract

## `documents/{documentId}`

| Field | Owner | Meaning |
|---|---|---|
| `authorId` | backend | Firebase Auth UID of contributor |
| `storagePath` | backend | private Storage path, never a public download URL |
| `status` | backend | `PENDING` or `APPROVED` |
| `fileSizeBytes`, `contentType` | backend | copied from verified Storage metadata |
| `createdAt`, `uploadedAt` | backend | server timestamps |
| `approvedBy`, `approvedAt` | backend | moderation audit trail |
| `reportCount` | backend | aggregate of one report per authenticated user |

Clients may upload only into `documents/{uid}/...` and then call `submitDocument`. They never create or update a document Firestore record directly.

## `trainingPrograms/{programId}`

Programme definitions contain an ordered `modules` array. Each module has `id`, `title`, `description`, `skillCode`, `estimatedMinutes`, `order`, and `required`. Authenticated clients may read definitions; only the backend writes them.

## `users/{uid}/trainingAssignments/{programId}`

| Field | Owner | Meaning |
|---|---|---|
| `completedModuleIds` | backend | validated module IDs completed by the assigned user |
| `assignedAtMillis`, `dueAtMillis` | backend | assignment window used by the Android UI |
| `completionPercent` | backend | percentage derived from required module completion |
| `status` | backend | `IN_PROGRESS` or `COMPLETED` |

The Android client updates completion only through `updateTrainingModuleCompletion`; direct assignment writes are denied.

## Idempotency records

- `contributionLedger/document-approval_{documentId}`: one XP reward per approved contribution.
- `creditLedger/document-unlock_{uid}_{documentId}`: one debit per user/document pair.
- `documents/{documentId}/reports/{uid}`: one report per user/document pair.
