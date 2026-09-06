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
                // Tự động chèn 4 thông báo mẫu của EduVault lùi timestamp thực tế
                val currentTime = System.currentTimeMillis()
                val defaultNotifications = listOf(
                    EduNotification(
                        title = "🎁 Chào mừng đến với EduVault!",
                        content = "Tài khoản của bạn đã được tạo thành công. Khám phá kho tài liệu học tập và bắt đầu chia sẻ ngay hôm nay.",
                        timestamp = currentTime - 5 * 60_000,
                        userId = userId,
                        type = NotificationType.SYSTEM
                    ),
                    EduNotification(
                        title = "📚 Hướng dẫn sử dụng",
                        content = "Upload tài liệu lên EduVault để nhận credit và chia sẻ kiến thức với cộng đồng sinh viên. Mỗi lượt tải thành công bạn sẽ nhận thêm điểm XP.",
                        timestamp = currentTime - 60 * 60_000,
                        userId = userId,
                        type = NotificationType.AI
                    ),
                    EduNotification(
                        title = "🧩 Tính năng Quiz AI",
                        content = "Thử tạo bộ câu hỏi trắc nghiệm tự động từ tài liệu của bạn bằng tính năng Quiz AI. Học hiệu quả hơn và luyện thi thông minh hơn.",
                        timestamp = currentTime - 3 * 3600_000,
                        userId = userId,
                        type = NotificationType.COMMUNITY
                    ),
                    EduNotification(
                        title = "💡 Mẹo sử dụng",
                        content = "Dùng tính năng Smart Summary để tóm tắt tài liệu PDF bằng AI chỉ trong vài giây. Tiết kiệm thời gian ôn tập hiệu quả.",
                        timestamp = currentTime - 24 * 3600_000,
                        userId = userId,
                        type = NotificationType.ACHIEVEMENT
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
