import fs from 'node:fs';
import { randomUUID } from 'node:crypto';
import { createRequire } from 'node:module';
import assert from 'node:assert/strict';
import { initializeApp, deleteApp } from 'firebase/app';
import { getAuth, createUserWithEmailAndPassword, signInWithEmailAndPassword, deleteUser } from 'firebase/auth';
import { getFirestore, collection, doc, getDocs, getDocFromServer, setDoc, updateDoc, runTransaction, serverTimestamp, Timestamp, terminate } from 'firebase/firestore';

const require = createRequire(import.meta.url);
const { configstore } = require('../node_modules/firebase-tools/lib/configstore');
const { getAccessToken } = require('../node_modules/firebase-tools/lib/auth');
const config = JSON.parse(fs.readFileSync(new URL('../../app/google-services.json', import.meta.url), 'utf8'));
assert.equal(config.project_info.project_id, 'appeduvault');
const settings = {projectId:'appeduvault', apiKey:config.client[0].api_key[0].current_key, appId:config.client[0].client_info.mobilesdk_app_id};
const learnerApp = initializeApp(settings, `mentor-learner-${randomUUID()}`);
const mentorApp = initializeApp(settings, `mentor-review-${randomUUID()}`);
const learnerAuth = getAuth(learnerApp), mentorAuth = getAuth(mentorApp);
const learnerDb = getFirestore(learnerApp), mentorDb = getFirestore(mentorApp);
const programId = 'software-fresher-foundation';
const productionLearner = 'c4rFR4N0LRchoZKjogLjBSpHKP12';
const qaEmail = `mentor-verification-${randomUUID()}@eduvault.local`;
let qaUid, mentorUid;
const {access_token} = await getAccessToken(configstore.get('tokens').refresh_token, []);
const headers = {Authorization:`Bearer ${access_token}`, 'Content-Type':'application/json'};
const base = 'https://firestore.googleapis.com/v1/projects/appeduvault/databases/(default)/documents';

async function privileged(path, method, body) {
  const response = await fetch(`${base}/${path}`, {method, headers, ...(body ? {body:JSON.stringify(body)} : {})});
  if (!response.ok && !(method === 'DELETE' && response.status === 404)) throw new Error(`Verification fixture ${method} failed: HTTP ${response.status}`);
}
try {
  if (!process.env.EDUVAULT_MENTOR_EMAIL || !process.env.EDUVAULT_MENTOR_PASSWORD) throw new Error('Mentor credentials must be supplied via environment variables.');
  const mentor = await signInWithEmailAndPassword(mentorAuth, process.env.EDUVAULT_MENTOR_EMAIL, process.env.EDUVAULT_MENTOR_PASSWORD);
  mentorUid = mentor.user.uid;
  const claims = (await mentor.user.getIdTokenResult(true)).claims;
  assert.equal(claims.mentor, true); assert.notEqual(claims.admin, true);
  const baseline = (await getDocFromServer(doc(mentorDb, `users/${productionLearner}/trainingAssignments/${programId}`))).data();
  const qa = await createUserWithEmailAndPassword(learnerAuth, qaEmail, randomUUID());
  qaUid = qa.user.uid;
  assert.notEqual(qaUid, productionLearner); assert.notEqual(qaUid, mentorUid);
  await setDoc(doc(learnerDb, `users/${qaUid}`), {
    uid:qaUid,name:'Kiểm thử mentor',email:qaEmail,university:'',avatarUrl:'',documentCredits:1,uploadCount:0,
    createdAt:Date.now(),role:'user',isBlocked:false,unlockedDocuments:[],quizCount:0,quizAverageScore:0,totalXp:0,savedDocuments:[],
  });
  const program = (await getDocFromServer(doc(learnerDb, `trainingPrograms/${programId}`))).data();
  const modules = program.moduleIds;
  assert.equal(modules.length, 3);
  const path = `users/${qaUid}/trainingAssignments/${programId}`;
  const learnerRef = doc(learnerDb, path), mentorRef = doc(mentorDb, path);
  await setDoc(learnerRef, {userId:qaUid,programId,completedModuleIds:[],completionPercent:0,status:'IN_PROGRESS',
    assignedAt:serverTimestamp(),dueAt:Timestamp.fromMillis(Date.now()+30*86400000),createdAt:serverTimestamp(),updatedAt:serverTimestamp()});
  await assert.rejects(getDocFromServer(mentorRef), e=>e.code==='permission-denied');
  await assert.rejects(getDocs(collection(mentorDb,'users')), e=>e.code==='permission-denied');
  await privileged(`mentorAssignments/${mentorUid}/learners/${qaUid}`, 'PATCH', {fields:{learnerId:{stringValue:qaUid},programId:{stringValue:programId}}});
  await getDocFromServer(mentorRef);
  await assert.rejects(updateDoc(learnerRef,{completionPercent:100}), e=>e.code==='permission-denied');

  const submit = async moduleId => {
    await updateDoc(learnerRef,{evidence:{moduleId,link:'https://github.com/eduvault-verification/pull/1',note:'Kiểm thử nghiệm thu',status:'PENDING',submittedAt:serverTimestamp()},updatedAt:serverTimestamp()});
    return (await getDocFromServer(mentorRef)).data();
  };
  const review = async (expected, approved) => runTransaction(mentorDb, async tx => {
    const data = (await tx.get(mentorRef)).data();
    assert.ok(data.evidence.submittedAt.isEqual(expected.evidence.submittedAt), 'Evidence changed');
    assert.equal(data.evidence.status,'PENDING');
    const completed = approved ? [...data.completedModuleIds,data.evidence.moduleId] : data.completedModuleIds;
    const receipt={...data.evidence,status:approved?'APPROVED':'REJECTED',feedback:approved?'Đã kiểm tra':'Bổ sung ảnh cấu hình',reviewedBy:mentorUid,reviewedAt:serverTimestamp()};
    tx.update(mentorRef,{evidence:receipt,reviewedEvidence:{...(data.reviewedEvidence || {}),[receipt.moduleId]:receipt},
      completedModuleIds:completed,completionPercent:Math.trunc(completed.length*100/modules.length),status:completed.length===modules.length?'COMPLETED':'IN_PROGRESS',updatedAt:serverTimestamp()});
  });
  const definition = {title:'Checklist bảo mật được mentor giao',instructions:'Đọc quy chuẩn, hoàn thành checklist và nộp liên kết bằng chứng.',dueDate:'15/11/2026',resourceLink:'https://developer.android.com',updatedBy:mentorUid,updatedAt:serverTimestamp()};
  await updateDoc(mentorRef,{taskDefinitions:{[modules[0]]:definition},taskEditModuleId:modules[0],updatedAt:serverTimestamp()});
  assert.equal((await getDocFromServer(learnerRef)).data().taskDefinitions[modules[0]].title,definition.title);
  await assert.rejects(updateDoc(learnerRef,{taskDefinitions:{},updatedAt:serverTimestamp()}),e=>e.code==='permission-denied');
  await updateDoc(mentorRef,{taskDefinitions:{[modules[0]]:{...definition,title:'Checklist bảo mật đã cập nhật'}},taskEditModuleId:modules[0],updatedAt:serverTimestamp()});
  await updateDoc(mentorRef,{taskDefinitions:{},taskEditModuleId:modules[0],updatedAt:serverTimestamp()});
  await updateDoc(mentorRef,{taskDefinitions:{[modules[0]]:definition},taskEditModuleId:modules[0],updatedAt:serverTimestamp()});
  const first = await submit(modules[0]);
  await assert.rejects(updateDoc(mentorRef,{taskDefinitions:{},taskEditModuleId:modules[0],updatedAt:serverTimestamp()}),e=>e.code==='permission-denied');
  await review(first,false);
  let result=(await getDocFromServer(learnerRef)).data();
  assert.equal(result.evidence.status,'REJECTED'); assert.equal(result.completionPercent,0);
  await assert.rejects(submit(modules[1]), e=>e.code==='permission-denied');
  const resubmitted = await submit(modules[0]);
  await assert.rejects(review(first,true), /Evidence changed/);
  await review(resubmitted,true);
  assert.equal((await getDocFromServer(learnerRef)).data().completionPercent,33);
  for (const moduleId of modules.slice(1)) await review(await submit(moduleId),true);
  result=(await getDocFromServer(learnerRef)).data();
  assert.equal(result.completionPercent,100); assert.equal(result.status,'COMPLETED');
  assert.deepEqual(result.completedModuleIds,modules);
  assert.equal(Object.keys(result.reviewedEvidence).length,3);
  await assert.rejects(updateDoc(mentorRef,{taskDefinitions:{},taskEditModuleId:modules[0],updatedAt:serverTimestamp()}),e=>e.code==='permission-denied');
  const after=(await getDocFromServer(doc(mentorDb, `users/${productionLearner}/trainingAssignments/${programId}`))).data();
  assert.equal(after.completionPercent,baseline.completionPercent);
  assert.deepEqual(after.completedModuleIds,baseline.completedModuleIds);
  console.log('PASS: scoped task create/edit/reset, pending/completed edit protection, reject/resubmit, stale-review guard, archived submissions and all three approvals on live Spark. Main learner progress unchanged.');
} finally {
  // Delete only fixture records belonging to the unique Auth account created in this run.
  if (qaUid && qaUid !== mentorUid && qaUid !== productionLearner && learnerAuth.currentUser?.email === qaEmail) {
    await privileged(`users/${qaUid}/trainingAssignments/${programId}`,'DELETE');
    if (mentorUid) await privileged(`mentorAssignments/${mentorUid}/learners/${qaUid}`,'DELETE');
    await privileged(`users/${qaUid}`,'DELETE');
    await deleteUser(learnerAuth.currentUser);
    console.log('CLEAN: temporary verification account and Firestore fixtures removed.');
  }
  await terminate(learnerDb); await terminate(mentorDb);
  await deleteApp(learnerApp); await deleteApp(mentorApp);
}
