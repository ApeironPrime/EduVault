package com.example.eduvault.data.repository

import com.example.eduvault.domain.model.EduNotification
import com.example.eduvault.domain.model.NotificationType
import com.example.eduvault.domain.repository.NotificationRepository
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class NotificationRepositoryImpl @Inject constructor(
    private val firestore: FirebaseFirestore
) : NotificationRepository {

    override suspend fun getNotifications(userId: String): Result<List<EduNotification>> {
        return try {
            val snapshot = firestore.collection("notifications")
                .whereEqualTo("userId", userId)
                .orderBy("timestamp", Query.Direction.DESCENDING)
                .get()
                .await()
            
            val list = snapshot.documents.mapNotNull { doc ->
                val id = doc.id
                val title = doc.getString("title") ?: ""
                val content = doc.getString("content") ?: ""
                val timestamp = doc.getLong("timestamp") ?: 0L
                val typeStr = doc.getString("type") ?: NotificationType.SYSTEM.name
                val type = try {
                    NotificationType.valueOf(typeStr)
                } catch (e: Exception) {
                    NotificationType.SYSTEM
                }
                val isRead = doc.getBoolean("isRead") ?: false
                
                EduNotification(id, title, content, timestamp, userId, type, isRead)
            }
            Result.success(list)
        } catch (e: Exception) {
            Result.failure(Exception("Không thể tải thông báo: ${e.localizedMessage}"))
        }
    }

    override suspend fun ensureDefaultNotifications(userId: String): Result<Unit> {
        return try {
            // Không khởi tạo cho Guest
            if (userId.isBlank() || userId == "guest_user") {
                return Result.success(Unit)
            }

            val snapshot = firestore.collection("notifications")
                .whereEqualTo("userId", userId)
                .limit(1)
                .get()
                .await()
            
            if (snapshot.isEmpty) {
                // A real welcome notice, without invented achievements or AI activity.
                val defaultNotifications = listOf(
                    EduNotification(
                        title = "Bắt đầu chương trình Onboarding",
                        content = "Bắt đầu với quy chuẩn và bảo mật, tiếp tục setup môi trường & Git, rồi thực hành mock project. Nộp link bằng chứng để mentor kiểm tra.",
                        timestamp = System.currentTimeMillis(),
                        userId = userId,
                        type = NotificationType.SYSTEM
                    )
                )

                for (notif in defaultNotifications) {
                    val docRef = firestore.collection("notifications").document()
                    val map = hashMapOf(
                        "id" to docRef.id,
                        "title" to notif.title,
                        "content" to notif.content,
                        "timestamp" to notif.timestamp,
                        "userId" to notif.userId,
                        "type" to notif.type.name,
                        "isRead" to notif.isRead
                    )
                    docRef.set(map).await()
                }
            }
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(Exception("Khởi tạo thông báo mẫu thất bại: ${e.localizedMessage}"))
        }
    }

    override suspend fun addNotification(
        userId: String,
        title: String,
        content: String,
        type: NotificationType
    ): Result<Unit> {
        return try {
            if (userId.isBlank() || userId == "guest_user") {
                // Tuyệt đối không ghi notifications cho Guest vào Firestore
                return Result.success(Unit)
            }

            val docRef = firestore.collection("notifications").document()
            val map = hashMapOf(
                "id" to docRef.id,
                "title" to title,
                "content" to content,
                "timestamp" to System.currentTimeMillis(),
                "userId" to userId,
                "type" to type.name,
                "isRead" to false
            )
            docRef.set(map).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(Exception("Không thể tạo thông báo mới: ${e.localizedMessage}"))
        }
    }

    override suspend fun markAsRead(id: String): Result<Unit> {
        return try {
            firestore.collection("notifications").document(id)
                .update("isRead", true)
                .await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(Exception("Không thể đánh dấu đã đọc: ${e.localizedMessage}"))
        }
    }
}
