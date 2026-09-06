import process from 'node:process';
import { getApps, initializeApp, cert } from 'firebase-admin/app';
import { getAuth } from 'firebase-admin/auth';

const uid = process.argv[2];
if (!uid) {
  throw new Error('Usage: npm run grant:admin -- <firebase-auth-uid>');
}
if (process.env.ALLOW_ADMIN_CLAIM !== 'true') {
  throw new Error('Set ALLOW_ADMIN_CLAIM=true after verifying the target user and change ticket.');
}
if (!process.env.FIREBASE_SERVICE_ACCOUNT_JSON) {
  throw new Error('FIREBASE_SERVICE_ACCOUNT_JSON is required and must never be committed.');
}

const serviceAccount = JSON.parse(process.env.FIREBASE_SERVICE_ACCOUNT_JSON);
const app = getApps()[0] ?? initializeApp({ credential: cert(serviceAccount) });
await getAuth(app).setCustomUserClaims(uid, { admin: true });
console.log(`Granted admin custom claim to ${uid}. The user must sign out and sign in again.`);
