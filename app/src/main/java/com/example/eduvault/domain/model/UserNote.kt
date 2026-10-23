package com.example.eduvault.domain.model

/**
 * Domain model đại diện cho một ghi chú cá nhân của người dùng gắn với một tài liệu.
 *
 * Firestore path: notes/{userId}/documents/{docId}
 */
data class UserNote(
    /** ID của tài liệu mà ghi chú này gắn với */
    val docId: String = "",
    /** Tiêu đề tài liệu — lưu để hiển thị trên màn danh sách ghi chú */
    val docTitle: String = "",
    /** Mã kỹ năng/chương trình của tài liệu */
    val docCourseCode: String = "",
    /** Nội dung ghi chú tự do của người dùng */
    val noteContent: String = "",
    /** Thời gian cập nhật gần nhất (epoch millis) */
    val updatedAt: Long = 0L,
    /** Thời gian tạo ghi chú (epoch millis) */
    val createdAt: Long = 0L
)
