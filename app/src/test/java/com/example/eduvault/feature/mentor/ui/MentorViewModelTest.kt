package com.example.eduvault.feature.mentor.ui

import com.example.eduvault.MainDispatcherRule
import com.example.eduvault.data.repository.FakeAuthRepository
import com.example.eduvault.domain.model.*
import com.example.eduvault.domain.repository.MentorRepository
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.test.*
import org.junit.Assert.*
import org.junit.Rule
import org.junit.Test

@OptIn(kotlinx.coroutines.ExperimentalCoroutinesApi::class)
class MentorViewModelTest {
    @get:Rule val main = MainDispatcherRule()
    private fun learner() = MentorLearner("learner", "Học viên", "learner@example.test",
        TrainingDashboard(TrainingProgram("p", "Onboarding", "", 1, listOf(
            TrainingModule("first", "Quy chuẩn", "", "SEC", 1, 15, true),
            TrainingModule("second", "Git", "", "GIT", 2, 15, true))),
            TrainingAssignment("p", emptySet(), 1, 2, 0, TrainingStatus.IN_PROGRESS),
            TrainingEvidence("first", "https://github.com/example/pull/1", "", 1000)), "1:0")

    private class Repository(var learners: List<MentorLearner>) : MentorRepository {
        var reviews = 0
        var taskSaves = 0
        var result: Result<Unit> = Result.success(Unit)
        var gate: CompletableDeferred<Unit>? = null
        override suspend fun saveTask(learner: MentorLearner, moduleId: String, task: TrainingTaskDefinition?): Result<Unit> {
            taskSaves++
            gate?.await()
            return result
        }
        override suspend fun getAssignedLearners() = Result.success(learners)
        override suspend fun reviewEvidence(learner: MentorLearner, approved: Boolean, feedback: String): Result<Unit> {
            reviews++
            gate?.await()
            return result
        }
    }

    @Test fun `signed boolean claims determine role instead of arbitrary text`() {
        assertEquals("mentor", trustedRoleFromClaims(mapOf("mentor" to true)))
        assertEquals("user", trustedRoleFromClaims(mapOf("mentor" to "true", "role" to "mentor")))
        assertEquals("user", trustedRoleFromClaims(emptyMap()))
        assertEquals("admin", trustedRoleFromClaims(mapOf("admin" to true)))
    }

    @Test fun `rejected evidence returns task to todo without opening the next stage`() {
        val dashboard = learner().dashboard.copy(evidence = learner().dashboard.evidence?.copy(status = "REJECTED", feedback = "Bổ sung"))
        assertEquals(TrainingTaskStatus.TODO, dashboard.tasks[0].status)
        assertEquals(TrainingTaskStatus.LOCKED, dashboard.tasks[1].status)
        assertEquals(0, dashboard.assignment.completionPercent)
    }

    @Test fun `rejecting without feedback never calls repository`() = runTest {
        val repo = Repository(listOf(learner()))
        val vm = MentorViewModel(repo, FakeAuthRepository())
        advanceUntilIdle()
        vm.review(learner(), false, "   ")
        advanceUntilIdle()
        assertEquals(0, repo.reviews)
        assertNotNull(vm.uiState.value.error)
        assertEquals(0, vm.uiState.value.reviewVersion)
    }

    @Test fun `failed review preserves pending evidence without reporting success`() = runTest {
        val repo = Repository(listOf(learner())).apply { result = Result.failure(Exception("Không có quyền nghiệm thu")) }
        val vm = MentorViewModel(repo, FakeAuthRepository())
        advanceUntilIdle()
        vm.review(learner(), true, "")
        advanceUntilIdle()
        assertFalse(vm.uiState.value.saving)
        assertEquals("PENDING", vm.uiState.value.learners.single().dashboard.evidence?.status)
        assertNull(vm.uiState.value.message)
        assertEquals(0, vm.uiState.value.reviewVersion)
    }

    @Test fun `double confirmation sends one review and refreshes after success`() = runTest {
        val repo = Repository(listOf(learner())).apply { gate = CompletableDeferred() }
        val vm = MentorViewModel(repo, FakeAuthRepository())
        advanceUntilIdle()
        vm.review(learner(), true, "")
        runCurrent()
        vm.review(learner(), true, "")
        assertEquals(1, repo.reviews)
        repo.learners = emptyList()
        repo.gate!!.complete(Unit)
        advanceUntilIdle()
        assertEquals(1, vm.uiState.value.reviewVersion)
        assertFalse(vm.uiState.value.saving)
        assertTrue(vm.uiState.value.learners.isEmpty())
    }
    @Test fun `invalid task never reaches repository`() = runTest {
        val repo = Repository(listOf(learner()))
        val vm = MentorViewModel(repo, FakeAuthRepository())
        advanceUntilIdle()
        vm.saveTask(learner(), "first", TrainingTaskDefinition("", ""))
        advanceUntilIdle()
        assertEquals(0, repo.taskSaves)
        assertNotNull(vm.uiState.value.error)
    }
    @Test fun `failed task write does not close editor or announce success`() = runTest {
        val repo = Repository(listOf(learner())).apply { result = Result.failure(Exception("Đang chờ duyệt")) }
        val vm = MentorViewModel(repo, FakeAuthRepository())
        advanceUntilIdle()
        vm.saveTask(learner(), "first", TrainingTaskDefinition("Checklist", "Hoàn thành checklist bảo mật"))
        advanceUntilIdle()
        assertEquals(1, repo.taskSaves)
        assertEquals(0, vm.uiState.value.reviewVersion)
        assertNull(vm.uiState.value.message)
        assertEquals("Đang chờ duyệt", vm.uiState.value.error)
    }
    @Test fun `task save prevents double click and closes editor only after success`() = runTest {
        val repo = Repository(listOf(learner())).apply { gate = CompletableDeferred() }
        val vm = MentorViewModel(repo, FakeAuthRepository())
        advanceUntilIdle()
        val task = TrainingTaskDefinition("Checklist", "Hoàn thành checklist bảo mật")
        vm.saveTask(learner(), "first", task)
        runCurrent()
        vm.saveTask(learner(), "first", task)
        assertEquals(1, repo.taskSaves)
        repo.gate!!.complete(Unit)
        advanceUntilIdle()
        assertEquals(1, vm.uiState.value.reviewVersion)
        assertFalse(vm.uiState.value.saving)
    }

}
