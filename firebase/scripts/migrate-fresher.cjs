// Targeted, recoverable migration. Run with --apply after inspecting its counts.
const fs = require('fs');
const path = require('path');
const { configstore } = require('../node_modules/firebase-tools/lib/configstore');
const { getAccessToken } = require('../node_modules/firebase-tools/lib/auth');
const base = 'https://firestore.googleapis.com/v1/projects/appeduvault/databases/(default)/documents';
const legacy = /Marketing Mix|Kinh tế vi mô|Xác suất thống kê|thi cuối kỳ|lượt tải|tặng sẵn|cộng đồng sinh viên|luyện thi|ôn tập|bắt đầu chia sẻ/i;
function enc(v) {
 if(typeof v==='string')return {stringValue:v};
 if(typeof v==='boolean')return {booleanValue:v};
 if(typeof v==='number')return {integerValue:String(v)};
 return {timestampValue:v.toISOString()};
}
(async()=>{
 const token = await getAccessToken(configstore.get('tokens').refresh_token,[]);
 const headers = {Authorization:`Bearer ${token.access_token}`,'Content-Type':'application/json'};
 async function request(url,opts={}) {const r=await fetch(url,{headers,...opts});if(!r.ok)throw Error(`HTTP ${r.status}`);return r.status===204?{}:r.json();}
 async function list(collection) {let docs=[],page='';do{const j=await request(`${base}/${collection}?pageSize=100${page?'&pageToken='+encodeURIComponent(page):''}`);docs.push(...(j.documents||[]));page=j.nextPageToken;}while(page);return docs;}
 const users=await list('users'),docs=await list('documents'),notifs=await list('notifications');
 const profiles=users.filter(d=>d.fields.isBlocked===undefined);
 const oldDocs=docs.filter(d=>d.fields.status?.stringValue!=='ARCHIVED' && legacy.test(d.fields.title?.stringValue||''));
 const oldNotifs=notifs.filter(d=>legacy.test((d.fields.title?.stringValue||'')+' '+(d.fields.content?.stringValue||'')));
 console.log(JSON.stringify({missingBlockFlag:profiles.length,legacyDocuments:oldDocs.length,legacyNotifications:oldNotifs.length,apply:process.argv.includes('--apply')}));
 if(!process.argv.includes('--apply'))return;
 const backupDir=path.resolve(__dirname,'../../.firebase-backups');fs.mkdirSync(backupDir,{recursive:true});
 const backup=path.join(backupDir,`fresher-${Date.now()}.json`);
 fs.writeFileSync(backup,JSON.stringify({users:profiles,documents:oldDocs,notifications:oldNotifs},null,2));
 async function patch(name,values,existing) {
 const params=new URLSearchParams();for(const k of Object.keys(values))params.append('updateMask.fieldPaths',k);
 if(existing)params.set('currentDocument.updateTime',existing.updateTime);
 return request(`https://firestore.googleapis.com/v1/${name}?${params}`,{method:'PATCH',body:JSON.stringify({fields:Object.fromEntries(Object.entries(values).map(([k,v])=>[k,enc(v)]))})});
 }
 for(const d of profiles)await patch(d.name,{isBlocked:false},d);
 for(const d of oldDocs)await patch(d.name,{status:'ARCHIVED'},d);
 for(const d of oldNotifs)await request(`https://firestore.googleapis.com/v1/${d.name}?currentDocument.updateTime=${encodeURIComponent(d.updateTime)}`,{method:'DELETE'});
 const resources=[
 ['fresher-conventions','SEC101','Quy chuẩn đặt tên biến & kiến trúc MVVM', 'Quy chuẩn mã nguồn\n\nDùng camelCase cho biến và hàm, PascalCase cho lớp. Tên cần diễn tả trách nhiệm; tránh tên viết tắt khó hiểu.\n\nTrong MVVM, View hiển thị trạng thái và gửi sự kiện. ViewModel quản lý trạng thái bằng StateFlow, gọi repository qua interface. Repository chịu trách nhiệm dữ liệu; không đặt Firebase hay Context của Activity trong ViewModel.\n\nBài thực hành: tách một màn hình thành UI, ViewModel và repository. Nộp link pull request cùng mô tả cách kiểm tra.'],
 ['fresher-git','GIT101','Git Flow & checklist Pull Request','Quy trình Git & Pull Request\n\nTạo nhánh feature theo nhiệm vụ, commit nhỏ với nội dung rõ ràng. Đồng bộ nhánh trước khi mở pull request; xử lý xung đột rồi chạy kiểm tra.\n\nPull request cần có mục tiêu, thay đổi chính, cách kiểm tra và ảnh giao diện khi cần. Không commit khóa API riêng, mật khẩu hoặc dữ liệu khách hàng.\n\nTự kiểm tra: Vì sao cần review trước khi merge? Khi xảy ra conflict, làm gì trước khi push?\n\nBài thực hành: mở pull request và nộp URL HTTPS ở mục Nộp bằng chứng. Tiến độ chỉ được nghiệm thu sau khi mentor kiểm tra.'],
 ['fresher-mock','DEV101','Mock project: tiêu chí nghiệm thu','Mục tiêu dự án thực hành\n\nXây dựng một luồng đăng nhập, đọc dữ liệu và gửi bằng chứng. Phải xử lý trạng thái đang tải, rỗng, mất mạng và lỗi quyền truy cập bằng thông báo dễ hiểu.\n\nTiêu chí kiểm tra: dữ liệu lưu trên Firestore; tài khoản chỉ đọc và sửa dữ liệu được phép; gửi thành công chỉ khi máy chủ xác nhận; khởi động lại vẫn đọc được dữ liệu đã lưu.\n\nNộp link repository hoặc pull request, hướng dẫn chạy và kết quả kiểm tra. Mentor kiểm tra trước khi cập nhật trạng thái hoàn thành.'],
 ];
 for(const [id,courseCode,title,aiContent] of resources) {
 const name=`projects/appeduvault/databases/(default)/documents/documents/${id}`;
 const r=await fetch(`https://firestore.googleapis.com/v1/${name}`,{headers});if(r.ok)continue;if(r.status!==404)throw Error(`HTTP ${r.status}`);
 await patch(name,{documentID:id,courseCode,title,type:'Note',status:'APPROVED',authorId:'training-team',quantityLabel:'Bài đọc',fileName:'',aiContent,aiKeyPoints:'Checklist thực hành và nộp bằng chứng.',aiPractice:'Nộp link HTTPS cùng ghi chú kết quả kiểm tra.',aiVerified:false,reportCount:0,views:0,rating:0,uploadedAt:new Date(),createdAt:new Date()});
 }
 console.log('Migration complete. Backup saved locally; no progress or role changed.');
})().catch(e=>{console.error(e.message);process.exitCode=1});
