package com.example.eduvault.data.repository

import android.net.Uri
import com.example.eduvault.BuildConfig
import com.example.eduvault.domain.model.DocViewerTabsContent
import com.example.eduvault.domain.model.DocumentPage
import com.example.eduvault.domain.model.LibraryDocument
import com.example.eduvault.domain.model.LibraryDocumentType
import com.example.eduvault.domain.repository.DocumentRepository
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.functions.FirebaseFunctions
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageMetadata
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import java.io.File
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

// Transitional local alias keeps legacy Firestore labels out of the domain API.
private typealias LibraryDocType = LibraryDocumentType

@Singleton
class DocumentRepositoryImpl @Inject constructor(
    private val firebaseAuth: FirebaseAuth,
    private val firestore: FirebaseFirestore,
    private val storage: FirebaseStorage,
    private val functions: FirebaseFunctions
) : DocumentRepository {

    override suspend fun uploadUserDocument(
        title: String,
        subjectCode: String,
        docType: String,
        fileUri: String
    ): Result<Unit> = runCatching {
        check(!BuildConfig.SPARK_ONLY) { "Bản Spark không hỗ trợ upload file. Hãy nộp bằng chứng bằng link GitHub hoặc Drive." }
        val uid = firebaseAuth.currentUser?.uid ?: error("Bạn cần đăng nhập để tải tài liệu.")
        val localFile = File(fileUri)
        require(localFile.exists() && localFile.isFile) { "Không tìm thấy tệp đã chọn." }
        require(localFile.length() in 1..MAX_FILE_SIZE_BYTES) { "Kích thước tệp phải từ 1 byte đến 25 MB." }

        val contentType = contentTypeFor(localFile.extension)
            ?: error("Chỉ hỗ trợ PDF, Word, PowerPoint và ảnh PNG/JPEG.")
        val extension = localFile.extension.lowercase().ifBlank { "bin" }
        val storagePath = "documents/$uid/${UUID.randomUUID()}.$extension"
        val storageRef = storage.reference.child(storagePath)
        val metadata = StorageMetadata.Builder().setContentType(contentType).build()

        storageRef.putFile(Uri.fromFile(localFile), metadata).await()
        try {
            functions.availableCallable("submitDocument")
                .call(
                    mapOf(
                        "title" to title.trim(),
                        "courseCode" to subjectCode.trim().uppercase(),
                        "type" to docType.trim(),
                        "storagePath" to storagePath
                    )
                )
                .await()
            Unit
        } catch (error: Exception) {
            // Do not leave orphan objects when the server rejects metadata.
            runCatching { storageRef.delete().await() }
            throw error
        }
    }.recoverCatching { error ->
        throw IllegalStateException("Gửi tài liệu thất bại: ${error.message ?: "vui lòng thử lại"}", error)
    }

    override suspend fun getDocuments(): Result<List<LibraryDocument>> =
        getDocumentsPage(DEFAULT_PAGE_SIZE).map { it.documents }

    override suspend fun getDocumentsPage(pageSize: Int, cursor: String?): Result<DocumentPage> = runCatching {
        require(pageSize in 1..MAX_PAGE_SIZE) { "Kích thước trang không hợp lệ." }
        var query: Query = firestore.collection("documents")
            .whereEqualTo("status", "APPROVED")
            .orderBy("uploadedAt", Query.Direction.DESCENDING)
            .limit(pageSize.toLong())
        if (!cursor.isNullOrBlank()) {
            val cursorSnapshot = firestore.collection("documents").document(cursor).get().await()
            require(cursorSnapshot.exists()) { "Con trỏ phân trang không tồn tại." }
            query = query.startAfter(cursorSnapshot)
        }
        val snapshot = query.get().await()
        val documents = snapshot.documents.mapNotNull(::toLibraryDoc)
        DocumentPage(
            documents = documents,
            nextCursor = if (snapshot.size() == pageSize) snapshot.documents.lastOrNull()?.id else null
        )
    }

    override suspend fun reportDocument(docId: String): Result<Unit> = runCatching {
        functions.availableCallable("reportDocument").call(mapOf("documentId" to docId)).await()
        Unit
    }

    override suspend fun getCachedDocViewerContent(docId: String): Result<DocViewerTabsContent?> = runCatching {
        val snapshot = firestore.collection("documents").document(docId).get().await()
        val content = snapshot.getString("aiContent") ?: return@runCatching null
        val keyPoints = snapshot.getString("aiKeyPoints") ?: return@runCatching null
        val practice = snapshot.getString("aiPractice") ?: return@runCatching null
        DocViewerTabsContent(content, keyPoints, practice)
    }

    override suspend fun saveDocViewerContent(docId: String, content: DocViewerTabsContent): Result<Unit> =
        Result.failure(IllegalStateException("Nội dung AI chỉ được lưu bởi backend."))

    override suspend fun getCachedSummary(docId: String): Result<String?> = runCatching {
        val snapshot = firestore.collection("documents").document(docId).get().await()
        if (snapshot.exists()) {
            val cachedSummary = snapshot.getString("aiSummary") ?: ""
            val cachedVersion = snapshot.getLong("aiSummaryVersion") ?: 0L
            if (cachedSummary.trim().isNotEmpty() && cachedVersion == 1L) {
                return@runCatching cachedSummary
            }
        }
        null
    }

    override suspend fun saveCachedSummary(docId: String, summary: String): Result<Unit> = runCatching {
        firestore.collection("documents").document(docId).update(
            mapOf(
                "aiSummary" to summary,
                "aiSummaryVersion" to 1L
            )
        ).await()
        Unit
    }

    override suspend fun downloadDocumentPdf(downloadUrl: String, destinationFile: File): Result<File> = withContext(Dispatchers.IO) {
        runCatching {
            if (destinationFile.exists() && destinationFile.length() > 0) {
                return@runCatching destinationFile
            }
            val parent = destinationFile.parentFile
            if (parent != null && !parent.exists()) {
                parent.mkdirs()
            }
            val tempFile = File(destinationFile.absolutePath + ".tmp")
            if (downloadUrl.startsWith("documents/")) {
                storage.reference.child(downloadUrl).getFile(tempFile).await()
            } else if (downloadUrl.startsWith("http://") || downloadUrl.startsWith("https://")) {
                val url = java.net.URL(downloadUrl)
                val connection = url.openConnection() as java.net.HttpURLConnection
                connection.requestMethod = "GET"
                connection.connectTimeout = 15000
                connection.readTimeout = 15000
                connection.doInput = true
                connection.connect()
                if (connection.responseCode == java.net.HttpURLConnection.HTTP_OK) {
                    connection.inputStream.use { input ->
                        tempFile.outputStream().use { output ->
                            input.copyTo(output)
                        }
                    }
                } else {
                    throw IllegalStateException("Lỗi tải tệp: HTTP ${connection.responseCode}")
                }
            } else {
                storage.reference.child(downloadUrl).getFile(tempFile).await()
            }
            if (!tempFile.renameTo(destinationFile)) {
                tempFile.copyTo(destinationFile, overwrite = true)
                tempFile.delete()
            }
            destinationFile
        }
    }

    override suspend fun deleteDocument(docId: String): Result<Unit> = runCatching {
        functions.availableCallable("deleteDocument").call(mapOf("documentId" to docId)).await()
        Unit
    }

    private fun toLibraryDoc(document: DocumentSnapshot): LibraryDocument? {
        val title = document.getString("title")?.takeIf { it.isNotBlank() } ?: return null
        val type = when (document.getString("type")?.lowercase()) {
            "quiz", "bộ đề", "trắc nghiệm" -> LibraryDocType.QUIZ
            "slide", "bài giảng" -> LibraryDocType.SLIDE
            "tóm tắt", "summary" -> LibraryDocType.SUMMARY
            else -> LibraryDocumentType.NOTE
        }
        val views = (document.get("views") as? Number)?.toInt()?.toString() ?: "0"
        return LibraryDocument(
            id = document.id,
            courseCode = document.getString("courseCode") ?: "",
            title = title,
            type = type,
            quantityLabel = document.getString("quantityLabel") ?: "Học liệu đào tạo",
            rating = document.getDouble("rating")?.toFloat() ?: 0f,
            views = views,
            bgIndex = title.hashCode().and(Int.MAX_VALUE) % 6,
            aiVerified = document.getBoolean("aiVerified") ?: false,
            aiCheckResult = document.getString("aiCheckResult") ?: "",
            status = document.getString("status") ?: "APPROVED",
            reportCount = document.getLong("reportCount")?.toInt() ?: 0,
            fileName = document.getString("fileName") ?: "",
            downloadUrl = document.getString("storagePath") ?: "",
            authorId = document.getString("authorId") ?: "",
            fileSizeBytes = document.getLong("fileSizeBytes") ?: 0L
        )
    }

    private fun contentTypeFor(extension: String): String? = when (extension.lowercase()) {
        "pdf" -> "application/pdf"
        "doc" -> "application/msword"
        "docx" -> "application/vnd.openxmlformats-officedocument.wordprocessingml.document"
        "ppt" -> "application/vnd.ms-powerpoint"
        "pptx" -> "application/vnd.openxmlformats-officedocument.presentationml.presentation"
        "png" -> "image/png"
        "jpg", "jpeg" -> "image/jpeg"
        else -> null
    }

    private companion object {
        const val MAX_FILE_SIZE_BYTES = 25L * 1024L * 1024L
        const val DEFAULT_PAGE_SIZE = 24
        const val MAX_PAGE_SIZE = 50
    }
}
