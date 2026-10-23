package com.example.eduvault.data.repository

import com.example.eduvault.domain.model.*
import com.google.firebase.Timestamp
import org.junit.Assert.*
import org.junit.Test

class TrainingTaskMapperTest {
    private val program = TrainingProgram("p", "Onboarding", "", 1, listOf(
        TrainingModule("git-workflow", "Setup & Git", "Default requirements", "GIT", 1, 30, true),
        TrainingModule("project", "Project", "", "DEV", 2, 30, true)))

    @Test fun `mentor definition overrides task text but keeps stage identity and unlock logic`() {
        val data = mapOf<String, Any>("completedModuleIds" to emptyList<String>(), "completionPercent" to 0,
            "taskDefinitions" to mapOf("git-workflow" to mapOf("title" to "Create a pull request",
                "instructions" to "Follow Git Flow and submit the PR link", "dueDate" to "15/11/2026", "resourceLink" to "https://github.com")))
        val dashboard = data.toTrainingDashboard(program)
        assertEquals("Create a pull request", dashboard.activeModule!!.evidenceTaskTitle)
        assertEquals("Follow Git Flow and submit the PR link", dashboard.activeModule!!.evidenceInstructions)
        assertEquals("Setup & Git", dashboard.activeModule!!.title)
        assertEquals(TrainingTaskStatus.TODO, dashboard.tasks[0].status)
        assertEquals(TrainingTaskStatus.LOCKED, dashboard.tasks[1].status)
        assertEquals("Default requirements", program.modules[0].description)
    }
    @Test fun `reset restores default and assignment snapshot does not retain old override`() {
        val dashboard = mapOf<String, Any>("taskDefinitions" to emptyMap<String, Any>()).toTrainingDashboard(program)
        assertEquals("Setup Android Studio & JDK 17", dashboard.activeModule!!.evidenceTaskTitle)
        assertEquals("Default requirements", dashboard.activeModule!!.evidenceInstructions)
    }
    @Test fun `archived approval remains accessible after next stage submission`() {
        val approved = mapOf("moduleId" to "git-workflow", "link" to "https://github.com/approved", "note" to "First stage",
            "status" to "APPROVED", "feedback" to "Good", "submittedAt" to Timestamp(100, 0))
        val pending = approved + mapOf("moduleId" to "project", "status" to "PENDING", "link" to "https://github.com/new")
        val dashboard = mapOf<String, Any>("completedModuleIds" to listOf("git-workflow"),
            "evidence" to pending, "reviewedEvidence" to mapOf("git-workflow" to approved)).toTrainingDashboard(program)
        assertEquals("https://github.com/approved", dashboard.reviewedEvidence["git-workflow"]!!.link)
        assertEquals("project", dashboard.currentEvidence!!.moduleId)
        assertEquals(TrainingTaskStatus.DONE, dashboard.tasks[0].status)
    }
    @Test fun `task validation checks real dates links and bounded requirements`() {
        val task = TrainingTaskDefinition("Setup JDK", "Install JDK and capture configuration")
        assertNull(trainingTaskError(task))
        assertNull(trainingTaskError(task.copy(dueDate = "29/02/2028", resourceLink = "https://developer.android.com")))
        assertNotNull(trainingTaskError(task.copy(dueDate = "31/02/2026")))
        assertNotNull(trainingTaskError(task.copy(dueDate = "2026-11-15")))
        assertNotNull(trainingTaskError(task.copy(resourceLink = "https://user:password@example.com")))
        assertNotNull(trainingTaskError(task.copy(instructions = "x".repeat(4001))))
    }
}
