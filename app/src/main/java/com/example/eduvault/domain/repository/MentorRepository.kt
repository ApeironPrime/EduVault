package com.example.eduvault.domain.repository

import com.example.eduvault.domain.model.MentorLearner
import com.example.eduvault.domain.model.TrainingTaskDefinition

interface MentorRepository {
    suspend fun getAssignedLearners(): Result<List<MentorLearner>>
    suspend fun reviewEvidence(learner: MentorLearner, approved: Boolean, feedback: String): Result<Unit>
    suspend fun saveTask(learner: MentorLearner, moduleId: String, task: TrainingTaskDefinition?): Result<Unit> =
        Result.failure(IllegalStateException("Chưa hỗ trợ quản lý nhiệm vụ."))
}
