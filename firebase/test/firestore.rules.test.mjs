import { readFile } from 'node:fs/promises';
import { after, before, describe, test } from 'node:test';
import assert from 'node:assert/strict';
import {
  assertFails,
  assertSucceeds,
  initializeTestEnvironment
} from '@firebase/rules-unit-testing';
import { doc, getDoc, getDocs, collection, setDoc, updateDoc, deleteDoc, serverTimestamp, Timestamp } from 'firebase/firestore';

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

describe('Spark training writes', () => {
  const path = 'users/spark-alice/trainingAssignments/spark-foundation';
  const enrollment = () => ({
    userId: 'spark-alice', programId: 'spark-foundation', completedModuleIds: [],
    completionPercent: 0, status: 'IN_PROGRESS', assignedAt: serverTimestamp(),
    dueAt: Timestamp.fromMillis(Date.now() + 30 * 86400000),
    createdAt: serverTimestamp(), updatedAt: serverTimestamp(),
  });
  const evidence = () => ({
    moduleId: 'git-workflow', link: 'https://github.com/example/pull/1', note: '',
    status: 'PENDING', submittedAt: serverTimestamp(),
  });
  const prepare = async (withAssignment = true) => testEnv.withSecurityRulesDisabled(async context => {
    const db = context.firestore();
    await setDoc(doc(db, 'users/spark-alice'), userDocument('spark-alice'));
    await setDoc(doc(db, 'trainingPrograms/spark-foundation'), { active: true, moduleIds: ['security', 'git-workflow', 'mock-project'], modulePrerequisites: { security: [], 'git-workflow': ['security'], 'mock-project': ['security', 'git-workflow'] } });
    if (withAssignment) await setDoc(doc(db, path), { ...enrollment(), completedModuleIds: ['security'], completionPercent: 33 });
    else await deleteDoc(doc(db, path));
  });
  test('owner can enroll only with zero progress', async () => {
    await prepare(false);
    const db = testEnv.authenticatedContext('spark-alice').firestore();
    await assertFails(setDoc(doc(db, path), { ...enrollment(), completionPercent: 100, completedModuleIds: ['security'] }));
    await assertSucceeds(setDoc(doc(db, path), enrollment()));
  });
  test('owner can save pending evidence, but cannot grant progress or approval', async () => {
    await prepare();
    const db = testEnv.authenticatedContext('spark-alice').firestore();
    await assertSucceeds(updateDoc(doc(db, path), { evidence: evidence(), updatedAt: serverTimestamp() }));
    await assertFails(updateDoc(doc(db, path), { evidence: { ...evidence(), status: 'APPROVED' }, updatedAt: serverTimestamp() }));
    await assertFails(updateDoc(doc(db, path), { evidence: evidence(), completionPercent: 100, updatedAt: serverTimestamp() }));
  });
  test('other users cannot read or overwrite evidence', async () => {
    await prepare();
    const db = testEnv.authenticatedContext('spark-bob').firestore();
    await assertFails(getDoc(doc(db, path)));
    await assertFails(updateDoc(doc(db, path), { evidence: evidence(), updatedAt: serverTimestamp() }));
  });
  test('rejects insecure links, unknown modules, oversized notes and forged timestamps', async () => {
    await prepare();
    const db = testEnv.authenticatedContext('spark-alice').firestore();
    for (const invalid of [
      { link: 'http://example.com' }, { link: 'https://user:password@example.com' },
      { moduleId: 'unknown' }, { note: 'x'.repeat(2001) },
      { submittedAt: Timestamp.fromMillis(1) }, { extraField: true },
    ]) await assertFails(updateDoc(doc(db, path), { evidence: { ...evidence(), ...invalid }, updatedAt: serverTimestamp() }));
  });
  test('blocked users cannot submit evidence', async () => {
    await prepare();
    await testEnv.withSecurityRulesDisabled(context => updateDoc(doc(context.firestore(), 'users/spark-alice'), { isBlocked: true }));
    const db = testEnv.authenticatedContext('spark-alice').firestore();
    await assertFails(updateDoc(doc(db, path), { evidence: evidence(), updatedAt: serverTimestamp() }));
  });
  test('new learner can submit stage one but cannot skip to later stages', async () => {
    await prepare(false);
    const db = testEnv.authenticatedContext('spark-alice').firestore();
    await assertSucceeds(setDoc(doc(db, path), enrollment()));
    await assertFails(updateDoc(doc(db, path), { evidence: evidence(), updatedAt: serverTimestamp() }));
    await assertFails(updateDoc(doc(db, path), { evidence: { ...evidence(), moduleId: 'mock-project' }, updatedAt: serverTimestamp() }));
    await assertSucceeds(updateDoc(doc(db, path), { evidence: { ...evidence(), moduleId: 'security' }, updatedAt: serverTimestamp() }));
    // A submitted receipt does not unlock the following stage.
    await assertFails(updateDoc(doc(db, path), { evidence: evidence(), updatedAt: serverTimestamp() }));
  });
  test('reviewed stage one unlocks only stage two, not completed or future stages', async () => {
    await prepare();
    const db = testEnv.authenticatedContext('spark-alice').firestore();
    for (const moduleId of ['security', 'mock-project']) {
      await assertFails(updateDoc(doc(db, path), { evidence: { ...evidence(), moduleId }, updatedAt: serverTimestamp() }));
    }
    await assertSucceeds(updateDoc(doc(db, path), { evidence: evidence(), updatedAt: serverTimestamp() }));
  });
  test('trusted admin can review progress without giving learner write access', async () => {
    await prepare();
    const db = testEnv.authenticatedContext('spark-mentor', { admin: true }).firestore();
    await assertSucceeds(updateDoc(doc(db, path), { completedModuleIds: ['security'], completionPercent: 33, status: 'IN_PROGRESS', updatedAt: serverTimestamp() }));
  });
});

before(async () => {
  testEnv = await initializeTestEnvironment({
    projectId,
    firestore: { rules: await readFile(new URL('../firestore.rules', import.meta.url), 'utf8') }
  });
  await testEnv.clearFirestore();
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

  test('only the owner can read an assignment and clients cannot change completion', async () => {
    await testEnv.withSecurityRulesDisabled(async (context) => {
      await setDoc(doc(context.firestore(), 'users/alice/trainingAssignments/foundation'), {
        userId: 'alice', completedModuleIds: [], completionPercent: 0, status: 'IN_PROGRESS'
      });
    });
    const ownerDb = testEnv.authenticatedContext('alice').firestore();
    const otherDb = testEnv.authenticatedContext('bob').firestore();
    await assertSucceeds(getDoc(doc(ownerDb, 'users/alice/trainingAssignments/foundation')));
    await assertFails(getDoc(doc(otherDb, 'users/alice/trainingAssignments/foundation')));
    await assertFails(updateDoc(doc(ownerDb, 'users/alice/trainingAssignments/foundation'), {
      completedModuleIds: ['git-workflow'], completionPercent: 100
    }));
  });
});


describe('Scoped mentor review on Spark', () => {
  const mentorId = 'mentor-reviewer';
  const learnerId = 'mentor-learner';
  const programId = 'mentor-foundation';
  const path = `users/${learnerId}/trainingAssignments/${programId}`;
  const prepare = async () => testEnv.withSecurityRulesDisabled(async context => {
    const db = context.firestore();
    await setDoc(doc(db, `users/${mentorId}`), {...userDocument(mentorId), role: 'mentor'});
    await setDoc(doc(db, `users/${learnerId}`), userDocument(learnerId));
    await setDoc(doc(db, 'users/mentor-stranger'), userDocument('mentor-stranger'));
    await setDoc(doc(db, `mentorAssignments/${mentorId}/learners/${learnerId}`), {programId});
    await setDoc(doc(db, `trainingPrograms/${programId}`), {active:true, moduleIds:['security','git','project'],
      modulePrerequisites:{security:[],git:['security'],project:['security','git']}});
    await setDoc(doc(db, path), {userId:learnerId, programId, completedModuleIds:['security'], completionPercent:33,
      status:'IN_PROGRESS', evidence:{moduleId:'git',link:'https://github.com/example/pull/1',note:'Setup',status:'PENDING',submittedAt:Timestamp.fromMillis(1000)}});
  });
  const mentorDb = () => testEnv.authenticatedContext(mentorId, {mentor:true}).firestore();
  const review = (approved = true) => ({
    evidence:{moduleId:'git',link:'https://github.com/example/pull/1',note:'Setup',submittedAt:Timestamp.fromMillis(1000),
      status:approved?'APPROVED':'REJECTED',feedback:approved?'':'Add setup screenshot',reviewedBy:mentorId,reviewedAt:serverTimestamp()},
    completedModuleIds:approved?['security','git']:['security'], completionPercent:approved?66:33,
    status:'IN_PROGRESS', updatedAt:serverTimestamp()
  });
  test('mentor can list own assignments and read only assigned learner/program', async () => {
    await prepare(); const db=mentorDb();
    await assertSucceeds(getDocs(collection(db,`mentorAssignments/${mentorId}/learners`)));
    await assertSucceeds(getDoc(doc(db,`users/${learnerId}`)));
    await assertSucceeds(getDoc(doc(db,path)));
    await assertFails(getDoc(doc(db,'users/mentor-stranger')));
    await assertFails(getDoc(doc(db,`users/${learnerId}/trainingAssignments/other-program`)));
    await assertFails(getDocs(collection(db,'users')));
    await assertFails(getDocs(collection(db,'mentorAssignments/other-mentor/learners')));
  });
  test('profile role alone cannot grant mentor access; assignments cannot be self-created', async () => {
    await prepare(); const untrusted=testEnv.authenticatedContext(mentorId).firestore();
    await assertFails(getDoc(doc(untrusted,path)));
    await assertFails(getDocs(collection(untrusted,`mentorAssignments/${mentorId}/learners`)));
    await assertFails(setDoc(doc(mentorDb(),`mentorAssignments/${mentorId}/learners/mentor-stranger`),{programId}));
    await assertFails(updateDoc(doc(testEnv.authenticatedContext(learnerId).firestore(),`users/${learnerId}`),{role:'mentor'}));
  });
  test('approve adds exactly the pending stage and computes progress; forbids tampering', async () => {
    await prepare(); const ref=doc(mentorDb(),path);
    await assertFails(updateDoc(ref,{...review(),completedModuleIds:['security','git','project'],completionPercent:100,status:'COMPLETED'}));
    await assertFails(updateDoc(ref,{...review(),completionPercent:67}));
    await assertFails(updateDoc(ref,{...review(),evidence:{...review().evidence,link:'https://example.com/changed'}}));
    await assertFails(updateDoc(ref,{...review(),evidence:{...review().evidence,reviewedBy:'other'}}));
    await assertSucceeds(updateDoc(ref,review()));
    const saved=(await getDoc(ref)).data(); assert.equal(saved.completionPercent,66); assert.equal(saved.evidence.status,'APPROVED');
    await assertFails(updateDoc(ref,review()));
  });
  test('reject requires feedback, leaves progress and allows learner to resubmit', async () => {
    await prepare(); const ref=doc(mentorDb(),path);
    await assertFails(updateDoc(ref,{...review(false),evidence:{...review(false).evidence,feedback:''}}));
    await assertFails(updateDoc(ref,{...review(false),completionPercent:66}));
    await assertSucceeds(updateDoc(ref,review(false)));
    const db=testEnv.authenticatedContext(learnerId).firestore();
    await assertSucceeds(updateDoc(doc(db,path),{evidence:{moduleId:'git',link:'https://github.com/example/pull/2',note:'Added screenshot',status:'PENDING',submittedAt:serverTimestamp()},updatedAt:serverTimestamp()}));
    await assertFails(updateDoc(doc(db,path),{evidence:{moduleId:'project',link:'https://github.com/example/pull/3',note:'Skip',status:'PENDING',submittedAt:serverTimestamp()},updatedAt:serverTimestamp()}));
  });
  test('unassigned, blocked and self-reviewing mentors cannot review', async () => {
    await prepare();
    await assertFails(updateDoc(doc(testEnv.authenticatedContext('other-mentor',{mentor:true}).firestore(),path),review()));
    await testEnv.withSecurityRulesDisabled(async context=>{
      const db=context.firestore();
      await updateDoc(doc(db,`users/${mentorId}`),{isBlocked:true});
    });
    await assertFails(updateDoc(doc(mentorDb(),path),review()));
    await prepare();
    await testEnv.withSecurityRulesDisabled(async context=>{
      const db=context.firestore();
      await setDoc(doc(db,`mentorAssignments/${learnerId}/learners/${learnerId}`),{programId});
    });
    await assertFails(updateDoc(doc(testEnv.authenticatedContext(learnerId,{mentor:true}).firestore(),path),{
      ...review(),evidence:{...review().evidence,reviewedBy:learnerId}
    }));
  });
  test('last approved stage closes the programme at 100 percent', async()=>{
    await prepare();
    await testEnv.withSecurityRulesDisabled(context=>updateDoc(doc(context.firestore(),path),{
      completedModuleIds:['security','git'],completionPercent:66,
      evidence:{moduleId:'project',link:'https://github.com/example/pull/1',note:'Setup',status:'PENDING',submittedAt:Timestamp.fromMillis(1000)}
    }));
    await assertSucceeds(updateDoc(doc(mentorDb(),path),{...review(),completedModuleIds:['security','git','project'],completionPercent:100,status:'COMPLETED',
      evidence:{...review().evidence,moduleId:'project'}}));
  });
});


describe('Mentor task authoring and reviewed submissions', () => {
  const mentor = 'task-mentor', learner = 'task-learner', programId = 'task-program';
  const path = `users/${learner}/trainingAssignments/${programId}`;
  const prepare = async () => testEnv.withSecurityRulesDisabled(async context => {
    const db = context.firestore();
    await setDoc(doc(db,`users/${mentor}`),userDocument(mentor));
    await setDoc(doc(db,`users/${learner}`),userDocument(learner));
    await setDoc(doc(db,`mentorAssignments/${mentor}/learners/${learner}`),{programId});
    await setDoc(doc(db,`trainingPrograms/${programId}`),{active:true,moduleIds:['a','b','c'],modulePrerequisites:{a:[],b:['a'],c:['a','b']}});
    await setDoc(doc(db,path),{userId:learner,programId,completedModuleIds:['a'],completionPercent:33,status:'IN_PROGRESS'});
  });
  const mentorDb = () => testEnv.authenticatedContext(mentor,{mentor:true}).firestore();
  const task = () => ({title:'Setup JDK 17',instructions:'Install JDK and submit configuration screenshot.',dueDate:'15/11/2026',
    resourceLink:'https://developer.android.com/studio',updatedBy:mentor,updatedAt:serverTimestamp()});
  const edit = () => ({taskDefinitions:{b:task()},taskEditModuleId:'b',updatedAt:serverTimestamp()});
  test('assigned mentor can create edit and reset one stage task without changing progress',async()=>{
    await prepare(); const ref=doc(mentorDb(),path);
    await assertSucceeds(updateDoc(ref,edit()));
    assert.equal((await getDoc(ref)).data().completionPercent,33);
    await assertSucceeds(updateDoc(ref,{...edit(),taskDefinitions:{b:{...task(),title:'Setup Android Studio'}}}));
    await assertSucceeds(updateDoc(ref,{taskDefinitions:{},taskEditModuleId:'b',updatedAt:serverTimestamp()}));
  });
  test('learner unassigned and blocked mentors cannot author tasks',async()=>{
    await prepare();
    await assertFails(updateDoc(doc(testEnv.authenticatedContext(learner).firestore(),path),edit()));
    await assertFails(updateDoc(doc(testEnv.authenticatedContext('stranger',{mentor:true}).firestore(),path),edit()));
    await testEnv.withSecurityRulesDisabled(context=>updateDoc(doc(context.firestore(),`users/${mentor}`),{isBlocked:true}));
    await assertFails(updateDoc(doc(mentorDb(),path),edit()));
  });
  test('reject invalid tasks unknown stages progress changes and editing completed stages',async()=>{
    await prepare();const ref=doc(mentorDb(),path);
    for (const invalid of [{title:'x'}, {instructions:'x'}, {resourceLink:'http://example.com'},
      {updatedBy:'stranger'},{updatedAt:Timestamp.fromMillis(1)},{extra:true},{dueDate:'2026-11-15'}]) {
      await assertFails(updateDoc(ref,{...edit(),taskDefinitions:{b:{...task(),...invalid}}}));
    }
    await assertFails(updateDoc(ref,{...edit(),completionPercent:100}));
    await assertFails(updateDoc(ref,{...edit(),taskDefinitions:{unknown:task()},taskEditModuleId:'unknown'}));
    await assertFails(updateDoc(ref,{...edit(),taskDefinitions:{a:task()},taskEditModuleId:'a'}));
    await assertFails(updateDoc(ref,{...edit(),taskDefinitions:{b:task(),c:task()}}));
  });
  test('pending submission freezes its task but mentor can prepare a future stage',async()=>{
    await prepare();await testEnv.withSecurityRulesDisabled(context=>updateDoc(doc(context.firestore(),path),{
      evidence:{moduleId:'b',link:'https://example.com/proof',note:'',status:'PENDING',submittedAt:Timestamp.fromMillis(1000)}}));
    const ref=doc(mentorDb(),path);
    await assertFails(updateDoc(ref,edit()));
    await assertSucceeds(updateDoc(ref,{...edit(),taskDefinitions:{c:task()},taskEditModuleId:'c'}));
  });
  test('review archives exact receipt and learner cannot forge or erase archived evidence',async()=>{
    await prepare();await testEnv.withSecurityRulesDisabled(context=>updateDoc(doc(context.firestore(),path),{
      evidence:{moduleId:'b',link:'https://example.com/proof',note:'',status:'PENDING',submittedAt:Timestamp.fromMillis(1000)}}));
    const receipt={moduleId:'b',link:'https://example.com/proof',note:'',status:'APPROVED',submittedAt:Timestamp.fromMillis(1000),
      feedback:'Good setup',reviewedBy:mentor,reviewedAt:serverTimestamp()};
    const review={evidence:receipt,reviewedEvidence:{b:receipt},completedModuleIds:['a','b'],completionPercent:66,status:'IN_PROGRESS',updatedAt:serverTimestamp()};
    const ref=doc(mentorDb(),path);
    await assertFails(updateDoc(ref,{...review,reviewedEvidence:{b:{...receipt,link:'https://example.com/fake'}}}));
    await assertSucceeds(updateDoc(ref,review));
    const own=doc(testEnv.authenticatedContext(learner).firestore(),path);
    await assertFails(updateDoc(own,{reviewedEvidence:{},updatedAt:serverTimestamp()}));
    await assertSucceeds(updateDoc(own,{evidence:{moduleId:'c',link:'https://example.com/next',note:'',status:'PENDING',submittedAt:serverTimestamp()},updatedAt:serverTimestamp()}));
    assert.equal((await getDoc(own)).data().reviewedEvidence.b.link,'https://example.com/proof');
  });
});
