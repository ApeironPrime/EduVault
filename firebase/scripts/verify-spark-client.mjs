import { readFile } from 'node:fs/promises';
import assert from 'node:assert/strict';
import { initializeApp, deleteApp } from 'firebase/app';
import { getAuth, createUserWithEmailAndPassword, signInWithEmailAndPassword, deleteUser } from 'firebase/auth';
import { getFirestore, doc, getDocFromServer, setDoc, updateDoc, serverTimestamp, Timestamp, terminate } from 'firebase/firestore';

const config = JSON.parse(await readFile(new URL('../../app/google-services.json', import.meta.url), 'utf8'));
assert.equal(config.project_info.project_id, 'appeduvault');
const client = config.client[0];
const app = initializeApp({ projectId: 'appeduvault', apiKey: client.api_key[0].current_key, appId: client.client_info.mobilesdk_app_id }, 'spark-verification');
const auth = getAuth(app);
const db = getFirestore(app);
try {
  const email = 'spark-verification@eduvault.local';
  const password = 'SparkVerify2026!';
  let credential;
  try { credential = await createUserWithEmailAndPassword(auth, email, password); }
  catch (error) {
    if (error.code !== 'auth/email-already-in-use') throw error;
    credential = await signInWithEmailAndPassword(auth, email, password);
  }
  const uid = credential.user.uid;
  const user = doc(db, `users/${uid}`);
  if (!(await getDocFromServer(user)).exists()) await setDoc(user, {
    uid, name: 'Kiểm thử Spark', email, university: '', avatarUrl: '', documentCredits: 1,
    uploadCount: 0, createdAt: Date.now(), role: 'user', isBlocked: false,
    unlockedDocuments: [], quizCount: 0, quizAverageScore: 0, totalXp: 0, savedDocuments: [],
  });
  const id = 'software-fresher-foundation';
  const program = (await getDocFromServer(doc(db, `trainingPrograms/${id}`))).data();
  assert.ok(program.modules.length >= 2);
  const assignment = doc(db, `users/${uid}/trainingAssignments/${id}`);
  if (!(await getDocFromServer(assignment)).exists()) await setDoc(assignment, {
    userId: uid, programId: id, completedModuleIds: [], completionPercent: 0,
    status: 'IN_PROGRESS', assignedAt: serverTimestamp(), dueAt: Timestamp.fromMillis(Date.now() + 30 * 86400000),
    createdAt: serverTimestamp(), updatedAt: serverTimestamp(),
  });
  const before = (await getDocFromServer(assignment)).data();
  await updateDoc(assignment, { evidence: {
    moduleId: program.modules[0].id, link: 'https://github.com/eduvault-verification/pull/1',
    note: 'Kiểm thử Firestore Spark trực tuyến', status: 'PENDING', submittedAt: serverTimestamp(),
  }, updatedAt: serverTimestamp() });
  await assert.rejects(updateDoc(assignment, { evidence: {
    moduleId: program.modules[1].id, link: 'https://github.com/eduvault-verification/pull/1',
    note: 'Skip attempt', status: 'PENDING', submittedAt: serverTimestamp(),
  }, updatedAt: serverTimestamp() }), error => error.code === 'permission-denied');
  const saved = (await getDocFromServer(assignment)).data();
  assert.equal(saved.evidence.status, 'PENDING');
  assert.ok(saved.evidence.submittedAt.toMillis() > 0);
  assert.equal(saved.completionPercent, before.completionPercent);
  await assert.rejects(updateDoc(assignment, { completionPercent: 100 }), error => error.code === 'permission-denied');
  console.log('PASS: Firebase Auth thật, tạo lộ trình online, lưu/đọc lại bằng chứng, chặn tự sửa tiến độ. Không gọi Functions hoặc Storage.');
} finally {
  if (auth.currentUser) await deleteUser(auth.currentUser);
  await terminate(db);
  await deleteApp(app);
}
