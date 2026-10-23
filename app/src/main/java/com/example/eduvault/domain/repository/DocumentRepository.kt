package com.example.eduvault.domain.repository

import com.example.eduvault.domain.model.DocViewerTabsContent
import com.example.eduvault.domain.model.DocumentPage
import com.example.eduvault.domain.model.LibraryDocument
import java.io.File

/**
 * Interface cho DocumentRepository - Quản lý tài liệu.
 */
interface DocumentRepository {
    
    /**
     * Đăng tải tài liệu mới của người dùng lên hệ thống.
     */
    suspend fun uploadUserDocument(
        title: String,
        subjectCode: String,
        docType: String,
        fileUri: String
    ): Result<Unit>

    /**
     * Lấy danh sách tài liệu từ Firestore.
     */
    suspend fun getDocuments(): Result<List<LibraryDocument>>

    /**
     * Fetches one server-ordered page of approved documents. The cursor is the
     * last document ID returned by the preceding page.
     */
    suspend fun getDocumentsPage(pageSize: Int, cursor: String? = null): Result<DocumentPage> =
        getDocuments().map { DocumentPage(documents = it, nextCursor = null) }

    /**
     * Tố cáo tài liệu vi phạm.
     */
    suspend fun reportDocument(docId: String): Result<Unit>

    /**
     * Lấy nội dung chi tiết DocViewer đã lưu cache trong Firestore (nếu có).
     */
    suspend fun getCachedDocViewerContent(docId: String): Result<DocViewerTabsContent?>

    /**
     * Lưu cache nội dung DocViewer vào Firestore.
     */
    suspend fun saveDocViewerContent(
        docId: String, 
        content: DocViewerTabsContent
    ): Result<Unit>

    /**
     * Lấy bản tóm tắt thông minh đã lưu cache trong Firestore (nếu có).
     */
    suspend fun getCachedSummary(docId: String): Result<String?>

    /**
     * Lưu cache bản tóm tắt thông minh vào Firestore.
     */
    suspend fun saveCachedSummary(docId: String, summary: String): Result<Unit>

    /**
     * Tải tệp PDF từ Cloud/Network về máy cục bộ an toàn trên luồng nền.
     */
    suspend fun downloadDocumentPdf(downloadUrl: String, destinationFile: File): Result<File>

    /**
     * Xóa tài liệu học tập khỏi hệ thống.
     */
    suspend fun deleteDocument(docId: String): Result<Unit>
}
