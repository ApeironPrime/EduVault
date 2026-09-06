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

## Idempotency records

- `creditLedger/document-approval_{documentId}`: one reward per approved document.
- `creditLedger/document-unlock_{uid}_{documentId}`: one debit per user/document pair.
- `documents/{documentId}/reports/{uid}`: one report per user/document pair.
