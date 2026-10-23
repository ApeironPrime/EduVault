package com.example.eduvault.feature.profile.ui

import com.example.eduvault.MainDispatcherRule
import com.example.eduvault.domain.model.DocumentCategory
import com.example.eduvault.domain.model.LibraryDocument
import com.example.eduvault.domain.model.LibraryDocumentType
import com.example.eduvault.domain.model.User
import com.example.eduvault.domain.repository.AdminRepository
import com.example.eduvault.domain.repository.CategoryRepository
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class AdminViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private class FakeCategoryRepository : CategoryRepository {
        var categoriesResult: Result<List<DocumentCategory>> = Result.success(emptyList())
        override suspend fun getCategories(): Result<List<DocumentCategory>> = categoriesResult
        override suspend fun addCategory(name: String, emoji: String): Result<Unit> = Result.success(Unit)
        override suspend fun updateCategory(id: String, name: String, emoji: String): Result<Unit> = Result.success(Unit)
        override suspend fun deleteCategory(id: String): Result<Unit> = Result.success(Unit)
    }

    private class FakeAdminRepository : AdminRepository {
        var pendingDocs: Result<List<LibraryDocument>> = Result.success(emptyList())
        var reportedDocs: Result<List<LibraryDocument>> = Result.success(emptyList())
        var users: Result<List<User>> = Result.success(emptyList())

        var approvedDocId: String? = null
        var deletedDocId: String? = null
        var dismissedDocId: String? = null
        var updatedBlockUid: String? = null
        var updatedBlockStatus: Boolean? = null

        override suspend fun deleteDocument(docId: String): Result<Unit> {
            deletedDocId = docId
            return Result.success(Unit)
        }

        override suspend fun getPendingDocuments(): Result<List<LibraryDocument>> = pendingDocs

        override suspend fun approveDocument(docId: String): Result<Unit> {
            approvedDocId = docId
            return Result.success(Unit)
        }

        override suspend fun getReportedDocuments(): Result<List<LibraryDocument>> = reportedDocs

        override suspend fun dismissReport(docId: String): Result<Unit> {
            dismissedDocId = docId
            return Result.success(Unit)
        }

        override suspend fun getAllUsers(): Result<List<User>> = users

        override suspend fun updateUserBlockStatus(uid: String, isBlocked: Boolean): Result<Unit> {
            updatedBlockUid = uid
            updatedBlockStatus = isBlocked
            return Result.success(Unit)
        }
    }

    @Test
    fun `loadAllData successfully populates pending and reported docs mapped to UI models`() = runTest {
        val fakeAdmin = FakeAdminRepository()
        val fakeCategory = FakeCategoryRepository()

        val samplePending = listOf(
            LibraryDocument(
                id = "doc_p1",
                courseCode = "CS101",
                title = "Pending Doc 1",
                type = LibraryDocumentType.NOTE,
                quantityLabel = "10 trang",
                rating = 4.0f,
                views = "0"
            )
        )
        val sampleReported = listOf(
            LibraryDocument(
                id = "doc_r1",
                courseCode = "MATH201",
                title = "Reported Doc 1",
                type = LibraryDocumentType.QUIZ,
                quantityLabel = "20 câu",
                rating = 3.5f,
                views = "50",
                reportCount = 3
            )
        )
        fakeAdmin.pendingDocs = Result.success(samplePending)
        fakeAdmin.reportedDocs = Result.success(sampleReported)

        val viewModel = AdminViewModel(fakeCategory, fakeAdmin)
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertFalse(state.isLoadingPending)
        assertFalse(state.isLoadingReported)
        assertEquals(1, state.pendingDocs.size)
        assertEquals("doc_p1", state.pendingDocs.first().id)
        assertEquals("Pending Doc 1", state.pendingDocs.first().title)

        assertEquals(1, state.reportedDocs.size)
        assertEquals("doc_r1", state.reportedDocs.first().id)
        assertEquals(3, state.reportedDocs.first().reportCount)
    }

    @Test
    fun `approveDocument calls repository and updates success message`() = runTest {
        val fakeAdmin = FakeAdminRepository()
        val fakeCategory = FakeCategoryRepository()
        val viewModel = AdminViewModel(fakeCategory, fakeAdmin)
        advanceUntilIdle()

        viewModel.approveDocument("doc_approve_123")
        advanceUntilIdle()

        assertEquals("doc_approve_123", fakeAdmin.approvedDocId)
        assertNotNull(viewModel.uiState.value.successMessage)
        assertTrue(viewModel.uiState.value.successMessage!!.contains("thành công"))
    }

    @Test
    fun `rejectDocument calls deleteDocument on repository`() = runTest {
        val fakeAdmin = FakeAdminRepository()
        val fakeCategory = FakeCategoryRepository()
        val viewModel = AdminViewModel(fakeCategory, fakeAdmin)
        advanceUntilIdle()

        viewModel.rejectDocument("doc_reject_456")
        advanceUntilIdle()

        assertEquals("doc_reject_456", fakeAdmin.deletedDocId)
        assertNotNull(viewModel.uiState.value.successMessage)
    }

    @Test
    fun `dismissReport calls dismissReport on repository`() = runTest {
        val fakeAdmin = FakeAdminRepository()
        val fakeCategory = FakeCategoryRepository()
        val viewModel = AdminViewModel(fakeCategory, fakeAdmin)
        advanceUntilIdle()

        viewModel.dismissReport("doc_dismiss_789")
        advanceUntilIdle()

        assertEquals("doc_dismiss_789", fakeAdmin.dismissedDocId)
        assertNotNull(viewModel.uiState.value.successMessage)
    }

    @Test
    fun `toggleUserBlock inverts block status and calls repository`() = runTest {
        val fakeAdmin = FakeAdminRepository()
        val fakeCategory = FakeCategoryRepository()
        val viewModel = AdminViewModel(fakeCategory, fakeAdmin)
        advanceUntilIdle()

        viewModel.toggleUserBlock("user_abc", currentBlockStatus = false)
        advanceUntilIdle()

        assertEquals("user_abc", fakeAdmin.updatedBlockUid)
        assertEquals(true, fakeAdmin.updatedBlockStatus)

        viewModel.toggleUserBlock("user_xyz", currentBlockStatus = true)
        advanceUntilIdle()

        assertEquals("user_xyz", fakeAdmin.updatedBlockUid)
        assertEquals(false, fakeAdmin.updatedBlockStatus)
    }

    @Test fun `permission failures stay visible and do not leak backend messages`() = runTest {
        val admin = FakeAdminRepository().apply {
            users = Result.failure(Exception("Lấy danh sách thất bại: PERMISSION_DENIED: Missing or insufficient permissions."))
        }
        val viewModel = AdminViewModel(FakeCategoryRepository(), admin)
        advanceUntilIdle()
        assertTrue(viewModel.uiState.value.accessDenied)
        assertTrue(viewModel.uiState.value.errorMessage!!.contains("quyền quản trị"))
        assertFalse(viewModel.uiState.value.errorMessage!!.contains("PERMISSION_DENIED"))
        viewModel.clearMessages()
        assertTrue(viewModel.uiState.value.accessDenied)
    }
}
