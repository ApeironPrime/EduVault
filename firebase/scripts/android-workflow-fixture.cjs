// Temporary live QA data only; passwords/tokens are never printed or committed.
const fs = require('node:fs');
const path = require('node:path');
const { randomUUID } = require('node:crypto');
const { configstore } = require('../node_modules/firebase-tools/lib/configstore');
const { getAccessToken } = require('../node_modules/firebase-tools/lib/auth');
const fixturePath = path.resolve(__dirname, '../../.codex-preview/live-final-fixture.json');
const project = 'appeduvault', program = 'software-fresher-foundation';
const mainLearner = 'c4rFR4N0LRchoZKjogLjBSpHKP12';
(async () => {
 const mode = process.argv[2];
 if (!['prepare','cleanup'].includes(mode)) throw new Error('Use prepare or cleanup');
 const config = JSON.parse(fs.readFileSync(path.resolve(__dirname,'../../app/google-services.json'),'utf8'));
 if (config.project_info.project_id !== project) throw new Error('Unexpected project');
 const {access_token} = await getAccessToken(configstore.get('tokens').refresh_token, []);
 const headers = {Authorization:`Bearer ${access_token}`,'Content-Type':'application/json'};
 const base = `https://firestore.googleapis.com/v1/projects/${project}/databases/(default)/documents`;
 async function call(url,method='GET',body) {
  const res=await fetch(url,{method,headers,...(body?{body:JSON.stringify(body)}:{})});
  if (method==='DELETE' && res.status===404) return;
  if (!res.ok) throw new Error(`${method} fixture operation failed: HTTP ${res.status}`);
  return res.status===204?null:res.json();
 }
 async function clientAuth(method,body) {
  const res=await fetch(`https://identitytoolkit.googleapis.com/v1/accounts:${method}?key=${config.client[0].api_key[0].current_key}`,{
   method:'POST',headers:{'Content-Type':'application/json'},body:JSON.stringify(body)});
  if (!res.ok) throw new Error(`Fixture authentication failed: HTTP ${res.status}`);
  return res.json();
 }
 const checkpoint = async () => {
  const data=await call(`${base}/users/${mainLearner}/trainingAssignments/${program}`);
  return {completed:data.fields.completedModuleIds,percent:data.fields.completionPercent};
 };
 if (mode==='prepare') {
  if (fs.existsSync(fixturePath)) throw new Error('Clean existing fixture before creating another');
  const mentorEmail=process.env.EDUVAULT_MENTOR_EMAIL, mentorPassword=process.env.EDUVAULT_MENTOR_PASSWORD;
  if (!mentorEmail || !mentorPassword) throw new Error('Supply mentor credentials through environment');
  const mentor=await clientAuth('signInWithPassword',{email:mentorEmail,password:mentorPassword,returnSecureToken:true});
  const lookup=await call(`https://identitytoolkit.googleapis.com/v1/projects/${project}/accounts:lookup`,'POST',{localId:[mentor.localId]});
  if (JSON.parse(lookup.users[0].customAttributes || '{}').mentor !== true) throw new Error('Expected trusted mentor');
  const email=`qa-finish-${randomUUID()}@eduvault.local`, password=randomUUID();
  const baseline=await checkpoint();
  const user=await clientAuth('signUp',{email,password,returnSecureToken:true});
  const record={qaEmail:email,qaPassword:password,qaUid:user.localId,mentorEmail,mentorPassword,mentorUid:mentor.localId,baseline};
  fs.mkdirSync(path.dirname(fixturePath),{recursive:true});fs.writeFileSync(fixturePath,JSON.stringify(record));
  await call(`${base}/users/${user.localId}`,'PATCH',{fields:{uid:{stringValue:user.localId},name:{stringValue:'Kiểm thử luồng hoàn chỉnh'},
   email:{stringValue:email},role:{stringValue:'user'},isBlocked:{booleanValue:false}}});
  await call(`${base}/mentorAssignments/${mentor.localId}/learners/${user.localId}`,'PATCH',{fields:{programId:{stringValue:program},learnerId:{stringValue:user.localId}}});
  console.log('READY: isolated Android workflow fixture created. Credentials saved only in ignored temporary file.');
 } else {
  const record=JSON.parse(fs.readFileSync(fixturePath,'utf8'));
  if (!/^qa-finish-[a-f0-9-]+@eduvault\.local$/.test(record.qaEmail) || record.qaUid===mainLearner || record.qaUid===record.mentorUid) throw new Error('Unsafe fixture identity');
  const lookup=await call(`https://identitytoolkit.googleapis.com/v1/projects/${project}/accounts:lookup`,'POST',{localId:[record.qaUid]});
  if (lookup.users?.length && lookup.users[0].email!==record.qaEmail) throw new Error('Fixture identity does not match');
  for (const target of [`users/${record.qaUid}/trainingAssignments/${program}`,`mentorAssignments/${record.mentorUid}/learners/${record.qaUid}`,`users/${record.qaUid}`]) await call(`${base}/${target}`,'DELETE');
  if (lookup.users?.length) await call(`https://identitytoolkit.googleapis.com/v1/projects/${project}/accounts:delete`,'POST',{localId:record.qaUid});
  const unchanged=JSON.stringify(await checkpoint())===JSON.stringify(record.baseline);
  fs.unlinkSync(fixturePath);
  if (!unchanged) throw new Error('Main learner progress changed outside verification');
  console.log('CLEAN: temporary account, assignment, mentor binding and credentials removed. Main learner progress unchanged.');
 }
})().catch(error=>{console.error(error.message);process.exitCode=1});
