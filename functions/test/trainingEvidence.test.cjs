const { test } = require("node:test");
const assert = require("node:assert/strict");
const { validateTrainingEvidence } = require("../lib/trainingEvidence.js");
const valid = { programId: "foundation", moduleId: "git", link: "https://github.com/team/repo/pull/1", note: "Ready" };
const validate = input => validateTrainingEvidence(input, "foundation", ["git"]);

test("trims valid evidence without changing its link", () => {
  assert.deepEqual(validate({ ...valid, link: ` ${valid.link} `, note: "  Ready  " }), {
    moduleId: "git", link: valid.link, note: "Ready"
  });
});
test("rejects unknown programmes and modules", () => {
  assert.throws(() => validate({ ...valid, programId: "someone-else" }), { code: "not-found" });
  assert.throws(() => validate({ ...valid, moduleId: "missing" }), { code: "invalid-argument" });
});
test("rejects missing, insecure and credential-bearing links", () => {
  for (const link of ["", "invalid", "http://github.com/repo", "https://user:password@example.com", "javascript:alert(1)"]) {
    assert.throws(() => validate({ ...valid, link }), { code: "invalid-argument" });
  }
});
test("rejects oversized inputs and accepts an optional empty note", () => {
  assert.throws(() => validate({ ...valid, note: "x".repeat(2001) }), { code: "invalid-argument" });
  assert.throws(() => validate({ ...valid, link: `https://example.com/${"x".repeat(2048)}` }), { code: "invalid-argument" });
  assert.equal(validate({ ...valid, note: "" }).note, "");
});
