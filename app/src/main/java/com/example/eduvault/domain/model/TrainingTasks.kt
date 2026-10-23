package com.example.eduvault.domain.model

enum class TrainingTaskStatus(val label: String) {
    TODO("Cần làm"), PENDING("Chờ duyệt"), DONE("Đã đạt"), LOCKED("Chưa mở")
}
data class TrainingTask(val module: TrainingModule, val stage: Int, val status: TrainingTaskStatus)

val TrainingDashboard.tasks: List<TrainingTask>
    get() = orderedModules.mapIndexed { index, module ->
        TrainingTask(module, index + 1, taskStatus(module.id))
    }

fun TrainingDashboard.taskStatus(moduleId: String): TrainingTaskStatus = when {
    moduleId in assignment.completedModuleIds -> TrainingTaskStatus.DONE
    !canSubmit(moduleId) -> TrainingTaskStatus.LOCKED
    currentEvidence?.status == "PENDING" -> TrainingTaskStatus.PENDING
    else -> TrainingTaskStatus.TODO
}

fun TrainingDashboard.stageStatusLabel(moduleId: String): String = when (taskStatus(moduleId)) {
    TrainingTaskStatus.DONE -> "Đã đạt"
    TrainingTaskStatus.LOCKED -> "Đang khóa"
    TrainingTaskStatus.PENDING -> "Chờ duyệt"
    TrainingTaskStatus.TODO -> if (currentEvidence?.status == "REJECTED") "Cần bổ sung" else "Đang làm"
}

val TrainingModule.evidenceTaskTitle: String
    get() = taskDefinition?.title ?: when (id) {
        "onboarding-security" -> "Nộp checklist quy chuẩn & bảo mật"
        "git-workflow" -> "Setup Android Studio & JDK 17"
        "mock-project" -> "Nộp repository và báo cáo mock project"
        else -> title
    }

val TrainingModule.evidenceInstructions: String get() = taskDefinition?.instructions ?: description
