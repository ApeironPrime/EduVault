package com.example.eduvault.feature.home.ui

import com.example.eduvault.MainDispatcherRule
import com.example.eduvault.domain.model.TrainingAssignment
import com.example.eduvault.domain.model.TrainingDashboard
import com.example.eduvault.domain.model.TrainingEvidence
import com.example.eduvault.domain.model.TrainingModule
import com.example.eduvault.domain.model.TrainingProgram
import com.example.eduvault.domain.model.TrainingStatus
import com.example.eduvault.domain.repository.TrainingRepository
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.CompletableDeferred
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class TrainingViewModelTest {
    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val module = TrainingModule("git", "Git Workflow", "", "GIT101", 1, 45, true)
    private val programme = TrainingProgram("foundation", "Foundation", "", 1, listOf(module))
    private val emptyAssignment = TrainingAssignment(
        "foundation", emptySet(), 1L, 2L, 0, TrainingStatus.IN_PROGRESS
    )

    @Test
    fun `load exposes assigned programme`() = runTest {
        val repository = FakeTrainingRepository(TrainingDashboard(programme, emptyAssignment))
        val viewModel = TrainingViewModel(repository)

        viewModel.loadProgram()
        advanceUntilIdle()

        assertEquals("foundation", viewModel.uiState.value.dashboard?.program?.id)
        assertEquals(0, viewModel.uiState.value.dashboard?.assignment?.completionPercent)
    }

    @Test
    fun `forced reload displays progress reviewed by backend`() = runTest {
        val repository = FakeTrainingRepository(TrainingDashboard(programme, emptyAssignment))
        val viewModel = TrainingViewModel(repository)
        viewModel.loadProgram()
        advanceUntilIdle()
        repository.loadResult = Result.success(TrainingDashboard(programme,
            emptyAssignment.copy(completedModuleIds = setOf("git"), completionPercent = 100)))
        viewModel.loadProgram(force = true)
        advanceUntilIdle()
        assertEquals(100, viewModel.uiState.value.dashboard?.assignment?.completionPercent)
        assertNull(repository.lastUpdate)
    }

    private val evidenceDashboard = TrainingDashboard(
        programme.copy(modules = listOf(module.copy(id = "security", order = 1), module.copy(order = 2))),
        emptyAssignment.copy(completedModuleIds = setOf("security"), completionPercent = 33),
    )
    private val evidence = TrainingEvidence("git", "https://github.com/team/repo/pull/1", "Ready", 100L)

    @Test
    fun `load failure exposes unavailable state instead of sample progress and retry recovers`() = runTest {
        val repository = FakeTrainingRepository(evidenceDashboard)
        repository.loadResult = Result.failure(Exception("Service unavailable"))
        val viewModel = TrainingViewModel(repository)
        assertNull(viewModel.uiState.value.dashboard)
        viewModel.loadProgram()
        advanceUntilIdle()
        assertNull(viewModel.uiState.value.dashboard)
        assertEquals("Service unavailable", viewModel.uiState.value.error)
        assertFalse(viewModel.uiState.value.isLoading)
        repository.loadResult = Result.success(evidenceDashboard)
        viewModel.loadProgram(force = true)
        advanceUntilIdle()
        assertEquals(evidenceDashboard, viewModel.uiState.value.dashboard)
        assertNull(viewModel.uiState.value.error)
    }

    @Test
    fun `zero progress submits first stage and pending evidence does not unlock the next`() = runTest {
        val dashboard = evidenceDashboard.copy(assignment = emptyAssignment)
        val repository = FakeTrainingRepository(dashboard).apply {
            evidenceResult = Result.success(evidence.copy(moduleId = "security"))
        }
        val viewModel = TrainingViewModel(repository)
        viewModel.loadProgram()
        advanceUntilIdle()
        assertEquals("security", viewModel.uiState.value.dashboard?.activeModule?.id)
        assertFalse(dashboard.canSubmit("git"))
        viewModel.submitEvidence(evidence.link, "Checklist")
        advanceUntilIdle()
        assertEquals("security", repository.lastEvidence?.get(1))
        assertEquals("security", viewModel.uiState.value.dashboard?.activeModule?.id)
        assertEquals("security", viewModel.uiState.value.dashboard?.currentEvidence?.moduleId)
    }

    @Test
    fun `evidence success stores server receipt and trimmed input`() = runTest {
        val repository = FakeTrainingRepository(evidenceDashboard)
        val viewModel = TrainingViewModel(repository)
        viewModel.loadProgram()
        advanceUntilIdle()
        viewModel.submitEvidence(" ${evidence.link} ", " Ready ")
        advanceUntilIdle()
        assertEquals(evidence, viewModel.uiState.value.dashboard?.evidence)
        assertEquals(listOf("foundation", "git", evidence.link, "Ready"), repository.lastEvidence)
        assertEquals(1, viewModel.uiState.value.evidenceSubmissionVersion)
        assertFalse(viewModel.uiState.value.isSubmittingEvidence)
    }

    @Test
    fun `evidence failure keeps dialog state without a success receipt`() = runTest {
        val repository = FakeTrainingRepository(evidenceDashboard)
        repository.evidenceResult = Result.failure(Exception("Network unavailable"))
        val viewModel = TrainingViewModel(repository)
        viewModel.loadProgram()
        advanceUntilIdle()
        viewModel.submitEvidence(evidence.link, "Ready")
        advanceUntilIdle()
        assertNull(viewModel.uiState.value.dashboard?.evidence)
        assertEquals(0, viewModel.uiState.value.evidenceSubmissionVersion)
        assertEquals("Network unavailable", viewModel.uiState.value.evidenceError)
        assertFalse(viewModel.uiState.value.isSubmittingEvidence)
    }

    @Test
    fun `invalid links never reach repository`() = runTest {
        val repository = FakeTrainingRepository(evidenceDashboard)
        val viewModel = TrainingViewModel(repository)
        viewModel.loadProgram()
        advanceUntilIdle()
        for (link in listOf("", "invalid", "http://example.com", "https://user:pass@example.com")) {
            viewModel.submitEvidence(link, "Ready")
            advanceUntilIdle()
            assertTrue(viewModel.uiState.value.evidenceError != null)
        }
        assertEquals(0, repository.evidenceCalls)
    }

    @Test
    fun `duplicate taps while request is pending send only once`() = runTest {
        val repository = FakeTrainingRepository(evidenceDashboard)
        val gate = CompletableDeferred<Result<TrainingEvidence>>()
        repository.evidenceGate = gate
        val viewModel = TrainingViewModel(repository)
        viewModel.loadProgram()
        advanceUntilIdle()
        viewModel.submitEvidence(evidence.link, "Ready")
        runCurrent()
        viewModel.submitEvidence(evidence.link, "Ready")
        assertTrue(viewModel.uiState.value.isSubmittingEvidence)
        assertEquals(1, repository.evidenceCalls)
        gate.complete(Result.success(evidence))
        advanceUntilIdle()
        assertEquals(evidence, viewModel.uiState.value.dashboard?.evidence)
    }

    @Test
    fun `load restores previously submitted evidence`() = runTest {
        val viewModel = TrainingViewModel(FakeTrainingRepository(evidenceDashboard.copy(evidence = evidence)))
        viewModel.loadProgram()
        advanceUntilIdle()
        assertEquals(evidence, viewModel.uiState.value.dashboard?.evidence)
    }

    @Test fun `mentor changes update learner state live and clear cancels subscription`() = runTest {
        val repo = FakeTrainingRepository(TrainingDashboard(programme, emptyAssignment))
        val vm = TrainingViewModel(repo)
        vm.loadProgram()
        advanceUntilIdle()
        val approved = TrainingDashboard(programme, emptyAssignment.copy(completedModuleIds = setOf("git"), completionPercent = 100, status = TrainingStatus.COMPLETED))
        repo.updates.emit(Result.success(approved))
        runCurrent()
        assertEquals(100, vm.uiState.value.dashboard?.assignment?.completionPercent)
        vm.clear()
        repo.updates.emit(Result.success(approved))
        runCurrent()
        assertNull(vm.uiState.value.dashboard)
    }

    @Test fun `review arriving while receipt read is pending is not overwritten by pending response`() = runTest {
        val repo = FakeTrainingRepository(evidenceDashboard)
        val gate = CompletableDeferred<Result<TrainingEvidence>>()
        repo.evidenceGate = gate
        val vm = TrainingViewModel(repo)
        vm.loadProgram(); advanceUntilIdle()
        vm.submitEvidence(evidence.link, evidence.note); runCurrent()
        val reviewed = evidence.copy(submittedAtMillis = 2000, status = "REJECTED", feedback = "Bổ sung ảnh JDK")
        repo.updates.emit(Result.success(evidenceDashboard.copy(evidence = reviewed))); runCurrent()
        gate.complete(Result.success(evidence.copy(submittedAtMillis = 0)))
        advanceUntilIdle()
        assertEquals("REJECTED", vm.uiState.value.dashboard?.currentEvidence?.status)
        assertEquals("Bổ sung ảnh JDK", vm.uiState.value.dashboard?.currentEvidence?.feedback)
        assertEquals(1, vm.uiState.value.evidenceSubmissionVersion)
        assertNull(vm.uiState.value.evidenceError)
    }

    @Test fun `signing out cancels outstanding submission and does not populate the next session`() = runTest {
        val repo = FakeTrainingRepository(evidenceDashboard)
        val gate = CompletableDeferred<Result<TrainingEvidence>>()
        repo.evidenceGate = gate
        val vm = TrainingViewModel(repo)
        vm.loadProgram(); advanceUntilIdle()
        vm.submitEvidence(evidence.link, evidence.note); runCurrent()
        vm.clear(); runCurrent()
        gate.complete(Result.success(evidence)); advanceUntilIdle()
        assertNull(vm.uiState.value.dashboard)
        assertEquals(0, vm.uiState.value.evidenceSubmissionVersion)
        assertNull(vm.uiState.value.evidenceError)
    }

    private class FakeTrainingRepository(
        private val dashboard: TrainingDashboard
    ) : TrainingRepository {
        val updates = kotlinx.coroutines.flow.MutableSharedFlow<Result<TrainingDashboard>>(extraBufferCapacity = 1)
        override fun observeAssignment(program: TrainingProgram) = updates
        var lastUpdate: Triple<String, String, Boolean>? = null
        var lastEvidence: List<String>? = null
        var evidenceCalls = 0
        var evidenceGate: CompletableDeferred<Result<TrainingEvidence>>? = null
        var evidenceResult = Result.success(TrainingEvidence("git", "https://github.com/team/repo/pull/1", "Ready", 100L))

        override suspend fun submitEvidence(programId: String, moduleId: String, link: String, note: String): Result<TrainingEvidence> {
            evidenceCalls++
            lastEvidence = listOf(programId, moduleId, link, note)
            return evidenceGate?.await() ?: evidenceResult
        }

        var loadResult: Result<TrainingDashboard> = Result.success(dashboard)
        override suspend fun getMyTrainingProgram(): Result<TrainingDashboard> = loadResult

        override suspend fun setModuleCompletion(
            programId: String,
            moduleId: String,
            completed: Boolean
        ): Result<TrainingAssignment> {
            lastUpdate = Triple(programId, moduleId, completed)
            return Result.success(
                dashboard.assignment.copy(
                    completedModuleIds = setOf(moduleId),
                    completionPercent = 100,
                    status = TrainingStatus.COMPLETED
                )
            )
        }
    }
}
