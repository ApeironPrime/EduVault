const { configstore } = require('../node_modules/firebase-tools/lib/configstore');
const { getAccessToken } = require('../node_modules/firebase-tools/lib/auth');
const project = 'appeduvault';
(async () => {
  const tokens = configstore.get('tokens');
  if (!tokens?.refresh_token) throw new Error('Firebase CLI chưa đăng nhập.');
  const { access_token } = await getAccessToken(tokens.refresh_token, []);
  const headers = { Authorization: `Bearer ${access_token}`, 'Content-Type': 'application/json' };
  if (process.argv.includes('--cleanup-verification')) {
    const base = `https://firestore.googleapis.com/v1/projects/${project}/databases/(default)/documents`;
    const response = await fetch(`${base}:runQuery`, { method: 'POST', headers, body: JSON.stringify({ structuredQuery: {
      from: [{ collectionId: 'users' }], where: { fieldFilter: { field: { fieldPath: 'email' }, op: 'EQUAL', value: { stringValue: 'spark-verification@eduvault.local' } } }, limit: 2,
    } }) });
    if (!response.ok) throw new Error(`Không truy vấn được dữ liệu kiểm thử: HTTP ${response.status}`);
    for (const result of await response.json()) {
      const document = result.document;
      if (!document) continue;
      if (document.fields?.name?.stringValue !== 'Kiểm thử Spark' || document.fields?.email?.stringValue !== 'spark-verification@eduvault.local') throw new Error('Không xóa: dữ liệu không khớp tài khoản kiểm thử.');
      const uid = document.name.split('/').at(-1);
      if (!uid || document.fields.uid?.stringValue !== uid) throw new Error('Không xóa: uid không khớp.');
      for (const path of [`users/${uid}/trainingAssignments/software-fresher-foundation`, `users/${uid}`]) {
        const removed = await fetch(`${base}/${path}`, { method: 'DELETE', headers });
        if (!removed.ok && removed.status !== 404) throw new Error(`Không dọn được dữ liệu kiểm thử: HTTP ${removed.status}`);
      }
    }
    console.log('Đã dọn dữ liệu Firestore của tài khoản kiểm thử riêng.');
  } else if (process.argv.includes('--unlink-billing')) {
    const response = await fetch(`https://cloudbilling.googleapis.com/v1/projects/${project}/billingInfo`, {
      method: 'PUT', headers, body: JSON.stringify({ billingAccountName: '' }),
    });
    if (!response.ok) throw new Error(`Không gỡ được billing: HTTP ${response.status}`);
    const info = await response.json();
    console.log(JSON.stringify({ project, billingEnabled: info.billingEnabled, hasLinkedBillingAccount: Boolean(info.billingAccountName) }));
  } else {
    const response = await fetch(`https://cloudbilling.googleapis.com/v1/projects/${project}/billingInfo`, { headers });
    if (!response.ok) throw new Error(`Không kiểm tra được billing: HTTP ${response.status}`);
    const info = await response.json();
    console.log(JSON.stringify({ project, billingEnabled: info.billingEnabled, hasLinkedBillingAccount: Boolean(info.billingAccountName) }));
  }
})().catch(error => { console.error(error.message); process.exitCode = 1; });
