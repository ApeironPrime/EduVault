package com.example.eduvault.domain.repository

import com.example.eduvault.domain.model.TrainingAssignment
import com.example.eduvault.domain.model.TrainingDashboard
import com.example.eduvault.domain.model.TrainingEvidence

interface TrainingRepository {
    fun observeAssignment(program: com.example.eduvault.domain.model.TrainingProgram): kotlinx.coroutines.flow.Flow<Result<TrainingDashboard>> = kotlinx.coroutines.flow.emptyFlow()

    suspend fun getMyTrainingProgram(): Result<TrainingDashboard>
    suspend fun setModuleCompletion(
        programId: String,
        moduleId: String,
        completed: Boolean
    ): Result<TrainingAssignment>

    suspend fun submitEvidence(
        programId: String, moduleId: String, link: String, note: String,
    ): Result<TrainingEvidence>
}
