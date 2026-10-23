package com.example.eduvault.domain.model

import org.junit.Assert.assertEquals
import org.junit.Test

class TrainingTasksTest {
    private fun dashboard(completed: Set<String> = emptySet(), evidence: TrainingEvidence? = null) = TrainingDashboard(
        TrainingProgram("p", "Onboarding", "", 1, listOf(
            TrainingModule("security", "Bảo mật", "", "SEC101", 1, 35, true),
            TrainingModule("git", "Git", "", "GIT101", 2, 50, true),
            TrainingModule("mock", "Mock project", "", "DEV101", 3, 180, true))),
        TrainingAssignment("p", completed, 0, 1, 0, TrainingStatus.IN_PROGRESS), evidence)

    @Test fun newAccountOnlyHasFirstTaskOpen() {
        assertEquals(listOf(TrainingTaskStatus.TODO, TrainingTaskStatus.LOCKED, TrainingTaskStatus.LOCKED),
            dashboard().tasks.map { it.status })
    }
    @Test fun pendingEvidenceDoesNotOpenNextTask() {
        assertEquals(listOf(TrainingTaskStatus.PENDING, TrainingTaskStatus.LOCKED, TrainingTaskStatus.LOCKED),
            dashboard(evidence = TrainingEvidence("security", "https://example.com", "", 1)).tasks.map { it.status })
    }
    @Test fun reviewedStageOpensNextAndOldReceiptIsNotPending() {
        assertEquals(listOf(TrainingTaskStatus.DONE, TrainingTaskStatus.TODO, TrainingTaskStatus.LOCKED),
            dashboard(setOf("security"), TrainingEvidence("security", "https://example.com", "", 1)).tasks.map { it.status })
    }
    @Test fun finishedProgramHasNoActionableTask() {
        assertEquals(listOf(TrainingTaskStatus.DONE, TrainingTaskStatus.DONE, TrainingTaskStatus.DONE),
            dashboard(setOf("security", "git", "mock")).tasks.map { it.status })
    }
}
