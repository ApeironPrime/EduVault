package com.example.eduvault.domain.model

/**
 * Domain model cho người dùng.
 * Lớp này độc lập hoàn toàn với Firebase — không import bất kỳ Firebase class nào.
 */
data class User(
    val uid: String,
    val fullName: String,
    val email: String,
    val university: String = "",
    val avatarUrl: String = "",
    val documentCredits: Int = 1,
    val uploadCount: Int = 0,
    val createdAt: Long = System.currentTimeMillis(),
    val role: String = "user",
    val isBlocked: Boolean = false,
    val unlockedDocuments: List<String> = emptyList(),
    val quizCount: Int = 0,
    val quizAverageScore: Float = 0.0f,
    val totalXp: Int = 0,
    val trainingProgressPercent: Int = 0,
    val savedDocuments: List<String> = emptyList(),
)
