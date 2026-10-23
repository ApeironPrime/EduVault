package com.example.eduvault.data.repository

import com.example.eduvault.domain.model.*
import com.google.firebase.Timestamp

internal fun Map<String, Any>.toTrainingProgram(id: String) = TrainingProgram(
        id, this["title"] as? String ?: "Lộ trình đào tạo", this["description"] as? String ?: "",
        (this["version"] as? Number)?.toInt() ?: 1,
        (this["modules"] as? List<*>).orEmpty().mapNotNull { item ->
            val module = item as? Map<*, *> ?: return@mapNotNull null
            TrainingModule(
                module["id"] as? String ?: return@mapNotNull null,
                module["title"] as? String ?: "", module["description"] as? String ?: "",
                module["skillCode"] as? String ?: "", (module["order"] as? Number)?.toInt() ?: 0,
                (module["estimatedMinutes"] as? Number)?.toInt() ?: 0, module["required"] as? Boolean ?: true,
            )
        }.sortedBy { it.order },
    )

internal fun Map<String, Any>.toTrainingAssignment(programId: String) = TrainingAssignment(
        this["programId"] as? String ?: programId,
        (this["completedModuleIds"] as? List<*>).orEmpty().filterIsInstance<String>().toSet(),
        timestampMillis(this["assignedAt"] ?: this["assignedAtMillis"]),
        timestampMillis(this["dueAt"] ?: this["dueAtMillis"]),
        (this["completionPercent"] as? Number)?.toInt()?.coerceIn(0, 100) ?: 0,
        if (this["status"] == "COMPLETED") TrainingStatus.COMPLETED else TrainingStatus.IN_PROGRESS,
        isDemo = this["demoScenario"] == "stage-2",
        taskDefinitions = (this["taskDefinitions"] as? Map<*, *>).orEmpty().entries.mapNotNull { (id, value) ->
            val task = value as? Map<*, *> ?: return@mapNotNull null
            (id as? String ?: return@mapNotNull null) to TrainingTaskDefinition(
                task["title"] as? String ?: "", task["instructions"] as? String ?: "",
                task["dueDate"] as? String ?: "", task["resourceLink"] as? String ?: "")
        }.toMap(),
    )

internal fun Map<String, Any>.toTrainingEvidence(): TrainingEvidence? {
        val data = this["evidence"] as? Map<*, *> ?: return null
        if (data["status"] !in listOf("PENDING", "APPROVED", "REJECTED")) return null
        val time = timestampMillis(data["submittedAt"] ?: data["submittedAtMillis"])
        check(time > 0) { "Chưa xác nhận được thời gian nộp. Vui lòng tải lại lộ trình." }
        return TrainingEvidence(data["moduleId"] as? String ?: "", data["link"] as? String ?: "",
            data["note"] as? String ?: "", time,
            data["status"] as? String ?: "PENDING", data["feedback"] as? String ?: "")
    }

private fun timestampMillis(value: Any?) = when (value) {
        is Timestamp -> value.toDate().time
        is Number -> value.toLong()
        else -> 0L
    }

internal fun Map<String, Any>.toTrainingDashboard(program: TrainingProgram) = TrainingDashboard(
    program, toTrainingAssignment(program.id), toTrainingEvidence(),
    (this["reviewedEvidence"] as? Map<*, *>).orEmpty().entries.mapNotNull { (id, evidence) ->
        val value = evidence as? Map<*, *> ?: return@mapNotNull null
        val record = mapOf("evidence" to value).toTrainingEvidence() ?: return@mapNotNull null
        (id as? String ?: return@mapNotNull null) to record
    }.toMap(),
)

