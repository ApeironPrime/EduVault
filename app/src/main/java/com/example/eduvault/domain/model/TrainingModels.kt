package com.example.eduvault.domain.model

data class TrainingModule(
    val id: String,
    val title: String,
    val description: String,
    val skillCode: String,
    val order: Int,
    val estimatedMinutes: Int,
    val required: Boolean,
    val taskDefinition: TrainingTaskDefinition? = null,
)

data class TrainingProgram(
    val id: String,
    val title: String,
    val description: String,
    val version: Int,
    val modules: List<TrainingModule>
)

enum class TrainingStatus { IN_PROGRESS, COMPLETED }

data class TrainingAssignment(
    val programId: String,
    val completedModuleIds: Set<String>,
    val assignedAtMillis: Long,
    val dueAtMillis: Long,
    val completionPercent: Int,
    val status: TrainingStatus,
    val isDemo: Boolean = false,
    val taskDefinitions: Map<String, TrainingTaskDefinition> = emptyMap(),
)

data class TrainingDashboard(
    val program: TrainingProgram,
    val assignment: TrainingAssignment,
    val evidence: TrainingEvidence? = null,
    val reviewedEvidence: Map<String, TrainingEvidence> = emptyMap(),
) {
    val orderedModules: List<TrainingModule> get() = program.modules.sortedBy { it.order }.map {
        it.copy(taskDefinition = assignment.taskDefinitions[it.id])
    }
    val activeModule: TrainingModule? get() = orderedModules.firstOrNull { it.id !in assignment.completedModuleIds }
    fun canSubmit(moduleId: String): Boolean = activeModule?.id == moduleId
    val activeStageNumber: Int get() = orderedModules.indexOfFirst { it.id == activeModule?.id } + 1
    val currentEvidence: TrainingEvidence? get() = evidence?.takeIf { it.moduleId == activeModule?.id }
}

data class TrainingEvidence(
    val moduleId: String,
    val link: String,
    val note: String,
    val submittedAtMillis: Long,
    val status: String = "PENDING",
    val feedback: String = "",
)
