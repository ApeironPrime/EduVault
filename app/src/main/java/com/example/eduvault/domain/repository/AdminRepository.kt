package com.example.eduvault.domain.repository

import com.example.eduvault.domain.model.LibraryDocument
import com.example.eduvault.domain.model.User

/**
 * Interface cho AdminRepository - Các đặc quyền quản trị viên.
 */
interface AdminRepository {
    
    /**
     * Xóa vĩnh viễn tài liệu khỏi Firestore.
     */
    suspend fun deleteDocument(docId: String): Result<Unit>

    /**
     * Lấy danh sách tài liệu đang ở trạng thái PENDING chờ kiểm duyệt.
     */
    suspend fun getPendingDocuments(): Result<List<LibraryDocument>>

    /**
     * Phê duyệt tài liệu (status = APPROVED).
     */
    suspend fun approveDocument(docId: String): Result<Unit>

    /**
     * Lấy danh sách tài liệu bị người dùng tố cáo (reportCount > 0).
     */
    suspend fun getReportedDocuments(): Result<List<LibraryDocument>>

    /**
     * Bác bỏ tố cáo của tài liệu (reportCount = 0).
     */
    suspend fun dismissReport(docId: String): Result<Unit>

    /**
     * Lấy danh sách toàn bộ thành viên.
     */
    suspend fun getAllUsers(): Result<List<User>>

    /**
     * Cập nhật trạng thái khóa tài khoản của người dùng.
     */
    suspend fun updateUserBlockStatus(uid: String, isBlocked: Boolean): Result<Unit>
}
