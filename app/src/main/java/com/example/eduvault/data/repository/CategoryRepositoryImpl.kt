package com.example.eduvault.data.repository

import com.example.eduvault.domain.model.DocumentCategory
import com.example.eduvault.domain.repository.CategoryRepository
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.functions.FirebaseFunctions
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class CategoryRepositoryImpl @Inject constructor(
    private val firestore: FirebaseFirestore,
    private val functions: FirebaseFunctions
) : CategoryRepository {

    override suspend fun getCategories(): Result<List<DocumentCategory>> {
        return try {
            val snapshot = firestore.collection("categories").get().await()
            if (snapshot.isEmpty) {
                // Read-only fallback for a new environment. Admins can persist
                // these or other categories through the callable backend.
                val defaultCategories = listOf(
                    DocumentCategory("git", "Git", "🌿", 0),
                    DocumentCategory("programming", "Lập trình", "💻", 0),
                    DocumentCategory("testing", "Kiểm thử", "🧪", 0),
                    DocumentCategory("agile", "Agile/Scrum", "🔁", 0),
                    DocumentCategory("devops", "DevOps", "⚙️", 0),
                    DocumentCategory("security", "Bảo mật", "🛡️", 0)
                )
                Result.success(defaultCategories)
            } else {
                val list = snapshot.documents.mapNotNull { doc ->
                    val id = doc.getString("categoryID") ?: doc.id
                    val name = doc.getString("name") ?: ""
                    val emoji = doc.getString("emoji") ?: "📁"
                    val count = doc.getLong("count")?.toInt() ?: 0
                    
                    DocumentCategory(id, name, emoji, count)
                }
                Result.success(list)
            }
        } catch (e: Exception) {
            Result.failure(Exception("Không thể tải danh sách danh mục: ${e.localizedMessage}"))
        }
    }

    override suspend fun addCategory(name: String, emoji: String): Result<Unit> {
        return try {
            functions.availableCallable("upsertCategory")
                .call(mapOf("name" to name.trim(), "emoji" to emoji.trim())).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(Exception("Thêm danh mục thất bại: ${e.localizedMessage}"))
        }
    }

    override suspend fun updateCategory(id: String, name: String, emoji: String): Result<Unit> {
        return try {
            functions.availableCallable("upsertCategory")
                .call(mapOf("id" to id, "name" to name.trim(), "emoji" to emoji.trim())).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(Exception("Cập nhật danh mục thất bại: ${e.localizedMessage}"))
        }
    }

    override suspend fun deleteCategory(id: String): Result<Unit> {
        return try {
            functions.availableCallable("deleteCategory").call(mapOf("id" to id)).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(Exception("Xóa danh mục thất bại: ${e.localizedMessage}"))
        }
    }
}
