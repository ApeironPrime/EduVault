import { readFile } from 'node:fs/promises';
import { after, before, describe, test } from 'node:test';
import assert from 'node:assert/strict';
import {
  assertFails,
  assertSucceeds,
  initializeTestEnvironment
} from '@firebase/rules-unit-testing';
import { doc, getDoc, setDoc, updateDoc } from 'firebase/firestore';

const projectId = 'eduvault-rules-test';
let testEnv;

const userDocument = (uid) => ({
  uid,
  name: 'Test user',
  email: `${uid}@example.test`,
  university: '',
  avatarUrl: '',
  documentCredits: 1,
  uploadCount: 0,
  createdAt: new Date(),
  role: 'user',
  isBlocked: false,
  unlockedDocuments: [],
  quizCount: 0,
  quizAverageScore: 0,
  totalXp: 0,
  savedDocuments: []
});

before(async () => {
  testEnv = await initializeTestEnvironment({
    projectId,
    firestore: { rules: await readFile(new URL('../firestore.rules', import.meta.url), 'utf8') }
  });
});

after(async () => {
  await testEnv.cleanup();
});

describe('Firestore authorization', () => {
  test('anonymous callers cannot read a user profile', async () => {
    const db = testEnv.unauthenticatedContext().firestore();
    await assertFails(getDoc(doc(db, 'users/alice')));
  });

  test('a user can create only their initial profile', async () => {
    const db = testEnv.authenticatedContext('alice').firestore();
    await assertSucceeds(setDoc(doc(db, 'users/alice'), userDocument('alice')));
  });

  test('a user cannot grant themself an admin role or credits', async () => {
    await testEnv.withSecurityRulesDisabled(async (context) => {
      await setDoc(doc(context.firestore(), 'users/alice'), userDocument('alice'));
    });
    const db = testEnv.authenticatedContext('alice').firestore();
    await assertFails(updateDoc(doc(db, 'users/alice'), { role: 'admin' }));
    await assertFails(updateDoc(doc(db, 'users/alice'), { documentCredits: 999 }));
  });

  test('a contributor cannot publish a document directly', async () => {
    const db = testEnv.authenticatedContext('alice').firestore();
    await assertFails(setDoc(doc(db, 'documents/direct-publish'), {
      authorId: 'alice',
      status: 'APPROVED',
      reportCount: 0
    }));
  });

  test('an admin client cannot bypass the approval backend', async () => {
    await testEnv.withSecurityRulesDisabled(async (context) => {
      await setDoc(doc(context.firestore(), 'documents/pending'), {
        authorId: 'alice', status: 'PENDING', reportCount: 0
      });
    });
    const db = testEnv.authenticatedContext('moderator', { admin: true }).firestore();
    await assertFails(updateDoc(doc(db, 'documents/pending'), { status: 'APPROVED' }));
  });
});
