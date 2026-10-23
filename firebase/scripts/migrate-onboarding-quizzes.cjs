const fs = require('fs');
const path = require('path');
const { configstore } = require('../node_modules/firebase-tools/lib/configstore');
const { getAccessToken } = require('../node_modules/firebase-tools/lib/auth');
const base = 'https://firestore.googleapis.com/v1/projects/appeduvault/databases/(default)/documents';
function encode(v) {
 if (typeof v === 'string') return {stringValue:v};
 if (typeof v === 'number') return {integerValue:String(v)};
 if (typeof v === 'boolean') return {booleanValue:v};
 if (Array.isArray(v)) return {arrayValue:{values:v.map(encode)}};
 return {mapValue:{fields:Object.fromEntries(Object.entries(v).map(([k,x])=>[k,encode(x)]))}};
}
(async()=>{
 const token=await getAccessToken(configstore.get('tokens').refresh_token,[]);
 const headers={Authorization:`Bearer ${token.access_token}`,'Content-Type':'application/json'};
 async function request(url,options={}) {
  const r=await fetch(url,{headers,...options});
  if(!r.ok) throw Error(`HTTP ${r.status}`);
  return r.status===204?{}:r.json();
 }
 let docs=[],page='';
 do {const response=await request(`${base}/quizzes?pageSize=100${page?'&pageToken='+encodeURIComponent(page):''}`);
 docs.push(...(response.documents||[]));page=response.nextPageToken;}while(page);
 const legacy=/Xác suất|thống kê|Kinh tế|Marketing|thi cuối kỳ|quiz_001/i;
 const old=docs.filter(d=>!d.fields.trainingModuleId &&
  (!d.fields.title?.stringValue || legacy.test([d.fields.title?.stringValue,d.fields.subject?.stringValue,d.fields.quizID?.stringValue].join(' '))));
 console.log(JSON.stringify({total:docs.length,legacy:old.length,titles:old.map(d=>d.fields.title?.stringValue||'(chưa có tiêu đề)'),apply:process.argv.includes('--apply')}));
 if(!process.argv.includes('--apply')) return;
 const backupDir=path.resolve(__dirname,'../../.firebase-backups');fs.mkdirSync(backupDir,{recursive:true});
 fs.writeFileSync(path.join(backupDir,`onboarding-quizzes-${Date.now()}.json`),JSON.stringify(old,null,2));
 for(const d of old) await request(`https://firestore.googleapis.com/v1/${d.name}?currentDocument.updateTime=${encodeURIComponent(d.updateTime)}`,{method:'DELETE'});
 const id='onboarding-security-check';
 if (!docs.some(d=>d.name.endsWith('/'+id))) {
  const questions=[
   {content:'Khi phát hiện khóa API bí mật đã được commit vào repository, bạn cần làm gì?',options:['Chỉ xóa dòng chứa khóa ở commit mới','Thu hồi khóa, báo người phụ trách và xử lý lịch sử repository','Đổi tên biến chứa khóa','Chuyển repository sang private là đủ'],correctAnswer:1,explanation:'Khóa đã lộ cần được thu hồi; xóa ở commit mới không loại bỏ khóa khỏi lịch sử.'},
   {content:'Quyền truy cập dữ liệu dự án nên được cấp theo nguyên tắc nào?',options:['Mọi nhân viên có quyền admin','Chia sẻ một tài khoản chung','Chỉ cấp quyền cần thiết cho công việc','Tắt kiểm tra quyền để demo nhanh'],correctAnswer:2,explanation:'Nguyên tắc quyền tối thiểu giảm nguy cơ truy cập hoặc sửa dữ liệu ngoài trách nhiệm.'},
   {content:'Trong MVVM, thành phần nào quản lý trạng thái màn hình và gọi repository?',options:['ViewModel','Composable trực tiếp gọi Firebase','Theme','AndroidManifest'],correctAnswer:0,explanation:'ViewModel quản lý trạng thái và điều phối dữ liệu qua repository; UI hiển thị trạng thái và gửi sự kiện.'},
   {content:'Pull Request nên chứa thông tin nào để mentor kiểm tra?',options:['Chỉ có tiêu đề','Chỉ có ảnh giao diện','Mục tiêu, thay đổi chính và cách kiểm tra','Mật khẩu tài khoản test công khai'],correctAnswer:2,explanation:'Mô tả thay đổi và cách kiểm tra giúp người review đánh giá đúng phạm vi và kết quả.'},
   {content:'Khi cần chia sẻ dữ liệu dự án cho người ngoài nhóm, bạn nên làm gì trước?',options:['Gửi link ngay','Kiểm tra phân quyền và xin phép người phụ trách','Đăng công khai để tiện tải','Dùng tài khoản của đồng nghiệp'],correctAnswer:1,explanation:'Dữ liệu dự án chỉ được chia sẻ qua kênh và phạm vi đã được cho phép.'},
  ];
  const data={quizID:id,title:'Quy chuẩn & bảo mật thông tin',subject:'SEC101',trainingModuleId:'onboarding-security',authorId:'training-team',difficulty:'Easy',questionCount:questions.length,questions,aiGenerated:false};
  await request(`${base}/quizzes/${id}?currentDocument.exists=false`,{method:'PATCH',body:JSON.stringify({fields:Object.fromEntries(Object.entries(data).map(([k,v])=>[k,encode(v)]))})});
 }
 console.log('Legacy quizzes removed after local backup; manual onboarding self-check ready. No assignment or score changed.');
})().catch(e=>{console.error(e.message);process.exitCode=1;});
