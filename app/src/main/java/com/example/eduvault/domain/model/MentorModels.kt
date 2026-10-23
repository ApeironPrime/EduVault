package com.example.eduvault.domain.model

/** Roles are derived from signed Firebase claims, never an editable profile field. */
fun trustedRoleFromClaims(claims: Map<String, Any?>): String = when {
    claims["admin"] == true -> "admin"
    claims["mentor"] == true -> "mentor"
    else -> "user"
}

data class MentorLearner(
    val uid: String,
    val name: String,
    val email: String,
    val dashboard: TrainingDashboard,
    val evidenceVersion: String,
)

fun mentorReviewError(approved: Boolean, feedback: String): String? = when {
    feedback.length > 2000 -> "Nhận xét không được dài quá 2000 ký tự."
    !approved && feedback.isBlank() -> "Hãy ghi rõ nội dung học viên cần bổ sung."
    else -> null
}
