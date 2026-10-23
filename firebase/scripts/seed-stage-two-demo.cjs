// Explicit demo scenario for the user's test account. Never applies to new learners.
const fs = require('fs');
const path = require('path');
const { configstore } = require('../node_modules/firebase-tools/lib/configstore');
const { getAccessToken } = require('../node_modules/firebase-tools/lib/auth');
const root = 'projects/appeduvault/databases/(default)/documents';
(async () => {
 const token = await getAccessToken(configstore.get('tokens').refresh_token, []);
 const headers = { Authorization: `Bearer ${token.access_token}`, 'Content-Type': 'application/json' };
 async function request(url, options = {}) { const r = await fetch(url, {headers, ...options}); if(!r.ok) throw Error(`HTTP ${r.status}`); return r.json(); }
 const query = await request(`https://firestore.googleapis.com/v1/${root}:runQuery`, {method:'POST', body:JSON.stringify({structuredQuery:{from:[{collectionId:'users'}],where:{fieldFilter:{field:{fieldPath:'email'},op:'EQUAL',value:{stringValue:'hanryit6012@gmail.com'}}},limit:2}})});
 const users = query.filter(row=>row.document).map(row=>row.document);
 if(users.length!==1) throw Error('Không xác định được duy nhất tài khoản test.');
 const user = users[0], uid = user.name.split('/').pop();
 if(user.fields.uid?.stringValue!==uid) throw Error('Hồ sơ tài khoản không khớp.');
 const programId = 'software-fresher-foundation';
 const program = await request(`https://firestore.googleapis.com/v1/${root}/trainingPrograms/${programId}`);
 const firstId = program.fields.moduleIds.arrayValue.values[0].stringValue;
 const assignment = await request(`https://firestore.googleapis.com/v1/${user.name}/trainingAssignments/${programId}`);
 const completed = assignment.fields.completedModuleIds.arrayValue.values || [];
 if(completed.some(v=>v.stringValue!==firstId)) throw Error('Tài khoản đã có tiến độ ngoài kịch bản demo; giữ nguyên.');
 const notificationRows = await request(`https://firestore.googleapis.com/v1/${root}:runQuery`, {method:'POST',body:JSON.stringify({structuredQuery:{from:[{collectionId:'notifications'}],where:{fieldFilter:{field:{fieldPath:'userId'},op:'EQUAL',value:{stringValue:uid}}}}})});
 const welcome = notificationRows.filter(row=>row.document?.fields.title?.stringValue==='Chào mừng đến với chương trình đào tạo').map(row=>row.document);
 const dir=path.resolve(__dirname,'../../.firebase-backups');fs.mkdirSync(dir,{recursive:true});
 fs.writeFileSync(path.join(dir,`stage-two-demo-${Date.now()}.json`),JSON.stringify({assignment,notifications:welcome},null,2));
 async function patch(document,fields) {
  const params=new URLSearchParams({'currentDocument.updateTime':document.updateTime});
  for(const key of Object.keys(fields))params.append('updateMask.fieldPaths',key);
  return request(`https://firestore.googleapis.com/v1/${document.name}?${params}`,{method:'PATCH',body:JSON.stringify({fields})});
 }
 await patch(assignment, {completedModuleIds:{arrayValue:{values:[{stringValue:firstId}]}}, completionPercent:{integerValue:'33'}, status:{stringValue:'IN_PROGRESS'},demoScenario:{stringValue:'stage-2'},updatedAt:{timestampValue:new Date().toISOString()}});
 for(const document of welcome)await patch(document,{title:{stringValue:'Bắt đầu chương trình Onboarding'}});
 console.log('Đã đặt tài khoản test ở Chặng 2 (33%), có nhãn dữ liệu demo và bản sao lưu. Không sửa tài khoản khác.');
})().catch(e=>{console.error(e.message);process.exitCode=1});
