package com.example.eduvault.data.repository

import com.example.eduvault.domain.model.LibraryDocument
import com.example.eduvault.domain.model.LibraryDocumentType
import com.example.eduvault.domain.repository.AdminRepository
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.functions.FirebaseFunctions
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AdminRepositoryImpl @Inject constructor(
    private val firestore: FirebaseFirestore,
    private val functions: FirebaseFunctions
) : AdminRepository {

    override suspend fun deleteDocument(docId: String): Result<Unit> {
        return try {
            functions.availableCallable("deleteDocument").call(mapOf("documentId" to docId)).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(Exception("Xóa tài liệu thất bại: ${e.localizedMessage}"))
        }
    }

    override suspend fun getPendingDocuments(): Result<List<LibraryDocument>> {
        return try {
            val snapshot = firestore.collection("documents")
                .whereEqualTo("status", "PENDING")
                .get().await()
            val list = snapshot.documents.mapNotNull { doc ->
                val title = doc.getString("title") ?: ""
                if (title.isBlank()) return@mapNotNull null
                val id = doc.getString("documentID") ?: doc.id
                val courseCode = doc.getString("courseCode") ?: ""
                val typeStr = doc.getString("type") ?: "Ghi chú"
                val type = when (typeStr.lowercase()) {
                    "quiz", "bộ đề", "trắc nghiệm" -> LibraryDocumentType.QUIZ
                    "slide", "bài giảng" -> LibraryDocumentType.SLIDE
                    "tóm tắt", "summary" -> LibraryDocumentType.SUMMARY
                    else -> LibraryDocumentType.NOTE
                }
                val quantityLabel = doc.getString("quantityLabel") ?: "Tài liệu tự đăng"
                val rating = doc.getDouble("rating")?.toFloat() ?: 0.0f
                val rawViews = doc.get("views")
                val views = when (rawViews) {
                    is Number -> rawViews.toInt().toString()
                    is String -> rawViews
                    else -> "0"
                }
                val aiVerified = doc.getBoolean("aiVerified") ?: false
                val aiCheckResult = doc.getString("aiCheckResult") ?: ""
                val reportCount = doc.getLong("reportCount")?.toInt() ?: 0
                val authorId = doc.getString("authorId") ?: ""
                val fileSizeBytes = doc.getLong("fileSizeBytes") ?: 0L
                LibraryDocument(
                    id = id,
                    courseCode = courseCode,
                    title = title,
                    type = type,
                    quantityLabel = quantityLabel,
                    rating = rating,
                    views = views,
                    bgIndex = Math.abs(title.hashCode() % 6),
                    aiVerified = aiVerified,
                    aiCheckResult = aiCheckResult,
                    status = "PENDING",
                    reportCount = reportCount,
                    authorId = authorId,
                    fileSizeBytes = fileSizeBytes
                )
            }
            Result.success(list)
        } catch (e: Exception) {
            Result.failure(Exception("Lấy tài liệu chờ duyệt thất bại: ${e.localizedMessage}"))
        }
    }

    override suspend fun approveDocument(docId: String): Result<Unit> {
        return try {
            functions.availableCallable("approveDocument").call(mapOf("documentId" to docId)).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(Exception("Phê duyệt tài liệu thất bại: ${e.localizedMessage}"))
        }
    }

    override suspend fun getReportedDocuments(): Result<List<LibraryDocument>> {
        return try {
            val snapshot = firestore.collection("documents")
                .whereGreaterThan("reportCount", 0L)
                .get().await()
            val list = snapshot.documents.mapNotNull { doc ->
                val title = doc.getString("title") ?: ""
                if (title.isBlank()) return@mapNotNull null
                val id = doc.getString("documentID") ?: doc.id
                val courseCode = doc.getString("courseCode") ?: ""
                val typeStr = doc.getString("type") ?: "Ghi chú"
                val type = when (typeStr.lowercase()) {
                    "quiz", "bộ đề", "trắc nghiệm" -> LibraryDocumentType.QUIZ
                    "slide", "bài giảng" -> LibraryDocumentType.SLIDE
                    "tóm tắt", "summary" -> LibraryDocumentType.SUMMARY
                    else -> LibraryDocumentType.NOTE
                }
                val quantityLabel = doc.getString("quantityLabel") ?: "Tài liệu tự đăng"
                val rating = doc.getDouble("rating")?.toFloat() ?: 0.0f
                val rawViews = doc.get("views")
                val views = when (rawViews) {
                    is Number -> rawViews.toInt().toString()
                    is String -> rawViews
                    else -> "0"
                }
                val aiVerified = doc.getBoolean("aiVerified") ?: false
                val aiCheckResult = doc.getString("aiCheckResult") ?: ""
                val status = doc.getString("status") ?: "APPROVED"
                val reportCount = doc.getLong("reportCount")?.toInt() ?: 0
                val authorId = doc.getString("authorId") ?: ""
                val fileSizeBytes = doc.getLong("fileSizeBytes") ?: 0L
                LibraryDocument(
                    id = id,
                    courseCode = courseCode,
                    title = title,
                    type = type,
                    quantityLabel = quantityLabel,
                    rating = rating,
                    views = views,
                    bgIndex = Math.abs(title.hashCode() % 6),
                    aiVerified = aiVerified,
                    aiCheckResult = aiCheckResult,
                    status = status,
                    reportCount = reportCount,
                    authorId = authorId,
                    fileSizeBytes = fileSizeBytes
                )
            }
            Result.success(list)
        } catch (e: Exception) {
            Result.failure(Exception("Lấy tài liệu bị tố cáo thất bại: ${e.localizedMessage}"))
        }
    }

    override suspend fun dismissReport(docId: String): Result<Unit> {
        return try {
            functions.availableCallable("dismissDocumentReports")
                .call(mapOf("documentId" to docId)).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(Exception("Bác tố cáo thất bại: ${e.localizedMessage}"))
        }
    }

    override suspend fun getAllUsers(): Result<List<com.example.eduvault.domain.model.User>> {
        return try {
            val snapshot = firestore.collection("users").get().await()
            val list = snapshot.documents.mapNotNull { doc ->
                val uid = doc.id
                val fullName = doc.getString("name") ?: doc.getString("fullName") ?: ""
                val email = doc.getString("email") ?: ""
                val university = doc.getString("university") ?: ""
                val avatarUrl = doc.getString("avatarUrl") ?: ""
                val documentCredits = doc.getLong("documentCredits")?.toInt() ?: 1
                val uploadCount = doc.getLong("uploadCount")?.toInt() ?: 0
                
                val rawCreatedAt = doc.get("createdAt")
                val milliseconds = when (rawCreatedAt) {
                    is com.google.firebase.Timestamp -> rawCreatedAt.toDate().time
                    is Long -> rawCreatedAt
                    is Number -> rawCreatedAt.toLong()
                    else -> System.currentTimeMillis()
                }
                
                val role = doc.getString("role") ?: "user"
                val isBlocked = doc.getBoolean("isBlocked") ?: false
                val quizCount = doc.getLong("quizCount")?.toInt() ?: 0
                val rawAverage = doc.getDouble("quizAverageScore") ?: 0.0
                val quizAverageScore = (if (rawAverage > 10.0) rawAverage / 10.0 else rawAverage)
                    .toFloat()
                    .coerceIn(0.0f, 10.0f)
                val totalXp = doc.getLong("totalXp")?.toInt() ?: 0
                
                com.example.eduvault.domain.model.User(
                    uid = uid,
                    fullName = fullName,
                    email = email,
                    university = university,
                    avatarUrl = avatarUrl,
                    documentCredits = documentCredits,
                    uploadCount = uploadCount,
                    createdAt = milliseconds,
                    role = role,
                    isBlocked = isBlocked,
                    quizCount = quizCount,
                    quizAverageScore = quizAverageScore,
                    totalXp = totalXp,
                    trainingProgressPercent = doc.getLong("trainingProgressPercent")?.toInt()?.coerceIn(0, 100) ?: 0
                )
            }
            Result.success(list)
        } catch (e: Exception) {
            Result.failure(Exception("Lấy danh sách thành viên thất bại: ${e.localizedMessage}"))
        }
    }

    override suspend fun updateUserBlockStatus(uid: String, isBlocked: Boolean): Result<Unit> {
        return try {
            functions.availableCallable("setUserBlocked")
                .call(mapOf("userId" to uid, "isBlocked" to isBlocked)).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(Exception("Cập nhật trạng thái khóa thất bại: ${e.localizedMessage}"))
        }
    }
}
