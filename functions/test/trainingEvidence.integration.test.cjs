const { test, after } = require("node:test");
const { getApps, deleteApp } = require("firebase-admin/app");
const assert = require("node:assert/strict");
const { getFirestore } = require("firebase-admin/firestore");
const { submitTrainingEvidence, getMyTrainingProgram } = require("../lib/index.js");
after(async () => { await Promise.all(getApps().map(deleteApp)); });

test("submission persists pending evidence and reload restores it without completing a module", async () => {
  assert.ok(process.env.FIRESTORE_EMULATOR_HOST, "This test must only run against the Firestore emulator");
  const db = getFirestore();
  const uid = "evidence-integration-test";
  const user = db.collection("users").doc(uid);
  await user.set({ fullName: "Test employee" });
  const before = await getMyTrainingProgram.run({ auth: { uid }, data: {} });
  const receipt = await submitTrainingEvidence.run({ auth: { uid }, data: {
    programId: before.program.id, moduleId: "git-workflow",
    link: "https://github.com/team/repo/pull/1", note: "Setup complete"
  } });
  assert.equal(receipt.status, "PENDING");
  const persisted = (await user.collection("trainingAssignments").doc(before.program.id).get()).data();
  assert.deepEqual(persisted.evidence, receipt);
  const reloaded = await getMyTrainingProgram.run({ auth: { uid }, data: {} });
  assert.deepEqual(reloaded.evidence, receipt);
  assert.deepEqual(reloaded.assignment.completedModuleIds, before.assignment.completedModuleIds);
  assert.equal(reloaded.assignment.completionPercent, before.assignment.completionPercent);
});

test("unauthenticated and unassigned users cannot submit evidence", async () => {
  const data = {
    programId: "software-fresher-foundation", moduleId: "git-workflow",
    link: "https://github.com/team/repo/pull/1", note: ""
  };
  await assert.rejects(submitTrainingEvidence.run({ data }), { code: "unauthenticated" });
  await assert.rejects(submitTrainingEvidence.run({ auth: { uid: "unassigned-user" }, data }), { code: "failed-precondition" });
});
