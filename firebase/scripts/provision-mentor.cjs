// Project-owner provisioning. Never store passwords, service account keys, or tokens in the repository.
const fs = require('node:fs');
const path = require('node:path');
const { configstore } = require('../node_modules/firebase-tools/lib/configstore');
const { getAccessToken } = require('../node_modules/firebase-tools/lib/auth');

async function provision() {
  const email = process.env.EDUVAULT_MENTOR_EMAIL?.trim().toLowerCase();
  const password = process.env.EDUVAULT_MENTOR_PASSWORD;
  const learnerId = process.env.EDUVAULT_LEARNER_UID;
  if (!email || !password || !learnerId) throw new Error('Set EDUVAULT_MENTOR_EMAIL, EDUVAULT_MENTOR_PASSWORD and EDUVAULT_LEARNER_UID.');
  if (!/^[a-zA-Z0-9_-]+$/.test(learnerId)) throw new Error('Invalid learner UID.');
  const config = JSON.parse(fs.readFileSync(path.join(__dirname, '../../app/google-services.json'), 'utf8'));
  const project = config.project_info.project_id;
  if (project !== 'appeduvault') throw new Error('Unexpected Firebase project.');
  const token = configstore.get('tokens');
  if (!token?.refresh_token) throw new Error('Firebase CLI must be logged in as the project owner.');
  const { access_token } = await getAccessToken(token.refresh_token, []);
  const headers = { Authorization: `Bearer ${access_token}`, 'Content-Type': 'application/json' };
  const apiKey = config.client[0].api_key[0].current_key;
  const base = `https://firestore.googleapis.com/v1/projects/${project}/databases/(default)/documents`;
  const request = async (url, options = {}) => {
    const response = await fetch(url, {headers, ...options});
    if (!response.ok) throw new Error(`Provisioning request failed: HTTP ${response.status}`);
    return response.json();
  };
  // Verify the learner before creating or granting any mentor account.
  await request(`${base}/users/${learnerId}`);
  await request(`${base}/users/${learnerId}/trainingAssignments/software-fresher-foundation`);
  const authCall = async name => {
    const response = await fetch(`https://identitytoolkit.googleapis.com/v1/accounts:${name}?key=${apiKey}`, {
      method: 'POST', headers: {'Content-Type':'application/json'},
      body: JSON.stringify({email, password, returnSecureToken: true}),
    });
    return {ok: response.ok, data: await response.json()};
  };
  let credential = await authCall('signUp');
  if (!credential.ok && credential.data.error?.message === 'EMAIL_EXISTS') credential = await authCall('signInWithPassword');
  if (!credential.ok) throw new Error(`Mentor authentication failed: ${credential.data.error?.message ?? 'unknown'}. Existing passwords are never reset by this script.`);
  const mentorId = credential.data.localId;
  if (!mentorId || mentorId === learnerId) throw new Error('Mentor must be a separate account.');
  const existing = await request(`https://identitytoolkit.googleapis.com/v1/projects/${project}/accounts:lookup`, {
    method:'POST', body:JSON.stringify({localId:[mentorId]}),
  });
  const user = existing.users?.[0];
  if (!user || user.disabled) throw new Error('Mentor account is missing or disabled.');
  const claims = user.customAttributes ? JSON.parse(user.customAttributes) : {};
  if (claims.admin === true) throw new Error('Use a separate mentor account, not an administrator account.');
  await request(`https://identitytoolkit.googleapis.com/v1/projects/${project}/accounts:update`, {
    method:'POST', body:JSON.stringify({localId:mentorId, customAttributes:JSON.stringify({...claims, mentor:true})}),
  });
  const profileResponse = await fetch(`${base}/users/${mentorId}`, {headers});
  const existingProfile = profileResponse.ok;
  if (!existingProfile && profileResponse.status !== 404) throw new Error(`Profile read failed: HTTP ${profileResponse.status}`);
  const fields = existingProfile ? {role:{stringValue:'mentor'}} : {
    uid:{stringValue:mentorId},name:{stringValue:'Mentor'},email:{stringValue:email},
    university:{stringValue:'Bộ phận đào tạo'},avatarUrl:{stringValue:''},role:{stringValue:'mentor'},
    isBlocked:{booleanValue:false},createdAt:{integerValue:String(Date.now())},documentCredits:{integerValue:'1'},
    uploadCount:{integerValue:'0'},quizCount:{integerValue:'0'},quizAverageScore:{doubleValue:0},totalXp:{integerValue:'0'},
    savedDocuments:{arrayValue:{values:[]}},unlockedDocuments:{arrayValue:{values:[]}},
  };
  await request(`${base}/users/${mentorId}${existingProfile?'?updateMask.fieldPaths=role':''}`, {
    method:'PATCH',body:JSON.stringify({fields}),
  });
  await request(`${base}/mentorAssignments/${mentorId}/learners/${learnerId}`, {
    method:'PATCH', body:JSON.stringify({fields:{learnerId:{stringValue:learnerId},programId:{stringValue:'software-fresher-foundation'}}}),
  });
  console.log(JSON.stringify({mentorEmail:email,mentorUid:mentorId,mentorClaim:true,adminClaim:false,learnerAssigned:true}));
}
provision().catch(error => {console.error(error.message);process.exitCode=1;});
