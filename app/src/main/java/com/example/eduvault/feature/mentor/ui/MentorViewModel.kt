package com.example.eduvault.feature.mentor.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.eduvault.domain.model.MentorLearner
import com.example.eduvault.domain.model.mentorReviewError
import com.example.eduvault.domain.model.TrainingTaskDefinition
import com.example.eduvault.domain.model.trainingTaskError
import com.example.eduvault.domain.repository.AuthRepository
import com.example.eduvault.domain.repository.MentorRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class MentorUiState(
    val learners: List<MentorLearner> = emptyList(),
    val loading: Boolean = false,
    val saving: Boolean = false,
    val error: String? = null,
    val message: String? = null,
    val reviewVersion: Int = 0,
)

@HiltViewModel
class MentorViewModel @Inject constructor(
    private val repository: MentorRepository,
    private val auth: AuthRepository,
) : ViewModel() {
    private val state = MutableStateFlow(MentorUiState())
    val uiState = state.asStateFlow()
    init { reload() }

    fun reload() {
        if (state.value.loading || state.value.saving) return
        state.update { it.copy(loading = true, error = null) }
        viewModelScope.launch {
            repository.getAssignedLearners().onSuccess { learners ->
                state.update { it.copy(learners = learners, loading = false) }
            }.onFailure { error -> state.update { it.copy(loading = false, error = error.message) } }
        }
    }

    fun review(learner: MentorLearner, approved: Boolean, feedback: String) {
        if (state.value.saving || state.value.loading) return
        mentorReviewError(approved, feedback)?.let { message ->
            state.update { it.copy(error = message) }; return
        }
        state.update { it.copy(saving = true, error = null, message = null) }
        viewModelScope.launch {
            repository.reviewEvidence(learner, approved, feedback).onSuccess {
                state.update { it.copy(saving = false, reviewVersion = it.reviewVersion + 1,
                    message = if (approved) "Đã nghiệm thu chặng học." else "Đã gửi yêu cầu bổ sung bằng chứng.") }
                reload()
            }.onFailure { error -> state.update { it.copy(saving = false, error = error.message) } }
        }
    }

    fun clearError() { state.update { it.copy(error = null) } }
    fun saveTask(learner: MentorLearner, moduleId: String, task: TrainingTaskDefinition?) {
        if (state.value.saving || state.value.loading) return
        task?.let { trainingTaskError(it)?.let { message ->
            state.update { value -> value.copy(error = message) }; return
        } }
        state.update { it.copy(saving = true, error = null, message = null) }
        viewModelScope.launch {
            repository.saveTask(learner, moduleId, task).onSuccess {
                state.update { it.copy(saving = false, reviewVersion = it.reviewVersion + 1,
                    message = if (task == null) "Đã khôi phục nhiệm vụ của chương trình." else "Đã giao nhiệm vụ cho học viên.") }
                reload()
            }.onFailure { error -> state.update { it.copy(saving = false, error = error.message) } }
        }
    }
    fun logout() { auth.logout() }
}
