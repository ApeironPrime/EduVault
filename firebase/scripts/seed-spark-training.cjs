// Uses the user's existing Firebase CLI login; never prints credentials.
const { configstore } = require('../node_modules/firebase-tools/lib/configstore');
const { getAccessToken } = require('../node_modules/firebase-tools/lib/auth');
const projectId = 'appeduvault';
const programId = 'software-fresher-foundation';
const modules = [
  { id: 'onboarding-security', title: 'Quy chuẩn & bảo mật', description: 'Quy tắc làm việc, bảo vệ tài khoản và dữ liệu dự án.', skillCode: 'SEC101', order: 1, estimatedMinutes: 35, required: true },
  { id: 'git-workflow', title: 'Setup môi trường & Git', description: 'Thiết lập môi trường và nộp link pull request để mentor kiểm tra.', skillCode: 'GIT101', order: 2, estimatedMinutes: 50, required: true },
  { id: 'mock-project', title: 'Mock project dự án', description: 'Thực hành dự án và nghiệm thu với mentor.', skillCode: 'DEV101', order: 3, estimatedMinutes: 180, required: true },
];
function encode(value) {
  if (typeof value === 'string') return { stringValue: value };
  if (typeof value === 'boolean') return { booleanValue: value };
  if (typeof value === 'number') return { integerValue: String(value) };
  if (Array.isArray(value)) return { arrayValue: { values: value.map(encode) } };
  return { mapValue: { fields: Object.fromEntries(Object.entries(value).map(([key, v]) => [key, encode(v)])) } };
}
(async () => {
  const tokens = configstore.get('tokens');
  if (!tokens?.refresh_token) throw new Error('Firebase CLI chưa đăng nhập.');
  const token = await getAccessToken(tokens.refresh_token, []);
  const headers = { Authorization: `Bearer ${token.access_token}`, 'Content-Type': 'application/json' };
  const url = `https://firestore.googleapis.com/v1/projects/${projectId}/databases/(default)/documents/trainingPrograms/${programId}`;
  const current = await fetch(url, { headers });
  let document;
  let writeUrl = url;
  if (current.ok) {
    const existing = await current.json();
    const ids = existing.fields?.modules?.arrayValue?.values?.slice().sort((a,b) => Number(a.mapValue.fields.order.integerValue) - Number(b.mapValue.fields.order.integerValue)).map(m => m.mapValue.fields.id.stringValue);
    if (!ids?.length) throw new Error('Chương trình hiện có không có modules hợp lệ; giữ nguyên dữ liệu.');
    document = { moduleIds: ids, modulePrerequisites: Object.fromEntries(ids.map((id,index) => [id, ids.slice(0,index)])) };
    writeUrl += '?updateMask.fieldPaths=moduleIds&updateMask.fieldPaths=modulePrerequisites';
  } else if (current.status === 404) {
    const ids = modules.map(m => m.id);
    document = { programId, title: 'Chương trình đào tạo nhân sự mới', description: 'Lộ trình nội bộ có bằng chứng và nghiệm thu bởi mentor.', version: 1, active: true, modules, moduleIds: ids, modulePrerequisites: Object.fromEntries(ids.map((id,index) => [id, ids.slice(0,index)])) };
    writeUrl += '?currentDocument.exists=false';
  } else throw new Error(`Không đọc được chương trình: HTTP ${current.status}`);
  const response = await fetch(writeUrl, { method: 'PATCH', headers, body: JSON.stringify({ fields: Object.fromEntries(Object.entries(document).map(([key, v]) => [key, encode(v)])) }) });
  if (!response.ok) throw new Error(`Không lưu được chương trình: HTTP ${response.status}`);
  console.log(`Đã thiết lập chương trình Firestore thật: ${projectId}/${programId}. Không sửa tiến độ hay bằng chứng của người dùng.`);
})().catch(error => { console.error(error.message); process.exitCode = 1; });
