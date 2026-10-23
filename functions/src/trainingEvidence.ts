import { HttpsError } from "firebase-functions/v2/https";

export function validateTrainingEvidence(input: Record<string, unknown>, programId: string, moduleIds: string[]) {
  if (input.programId !== programId) throw new HttpsError("not-found", "Training programme was not found.");
  if (typeof input.moduleId !== "string" || !moduleIds.includes(input.moduleId)) {
    throw new HttpsError("invalid-argument", "Training module was not found.");
  }
  const link = typeof input.link === "string" ? input.link.trim() : "";
  if (!link || link.length > 2048) throw new HttpsError("invalid-argument", "Evidence link is invalid.");
  let url: URL;
  try { url = new URL(link); } catch { throw new HttpsError("invalid-argument", "Evidence link is invalid."); }
  if (url.protocol !== "https:" || !url.hostname || url.username || url.password) {
    throw new HttpsError("invalid-argument", "Evidence link must use HTTPS without credentials.");
  }
  if (typeof input.note !== "string" || input.note.trim().length > 2000) {
    throw new HttpsError("invalid-argument", "Evidence note is invalid.");
  }
  return { moduleId: input.moduleId, link, note: input.note.trim() };
}
