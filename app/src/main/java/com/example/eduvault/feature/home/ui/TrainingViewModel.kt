package com.example.eduvault.feature.home.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.eduvault.domain.model.TrainingDashboard
import com.example.eduvault.domain.model.trainingEvidenceError
import com.example.eduvault.domain.repository.TrainingRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.Job
import javax.inject.Inject

data class TrainingUiState(
    val dashboard: TrainingDashboard? = null,
    val isLoading: Boolean = false,
    val error: String? = null,
    val isSubmittingEvidence: Boolean = false,
    val evidenceError: String? = null,
    val evidenceSubmissionVersion: Int = 0,
)

@HiltViewModel
class TrainingViewModel @Inject constructor(
    private val repository: TrainingRepository
) : ViewModel() {
    private val _uiState = MutableStateFlow(TrainingUiState())
    val uiState: StateFlow<TrainingUiState> = _uiState.asStateFlow()

    private var loaded = false
    private var observer: Job? = null
    private var loadJob: Job? = null
    private var submissionJob: Job? = null

    fun loadProgram(force: Boolean = false) {
        if ((loaded || _uiState.value.isLoading) && !force) return
        loadJob?.cancel()
        loadJob = viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            repository.getMyTrainingProgram()
                .onSuccess { dashboard ->
                    loaded = true
                    _uiState.update { it.copy(dashboard = dashboard, isLoading = false, error = null) }
                    observer?.cancel()
                    observer = viewModelScope.launch {
                        repository.observeAssignment(dashboard.program).collect { result ->
                            result.onSuccess { updated -> _uiState.update { it.copy(dashboard = updated, error = null) } }
                                .onFailure { error -> _uiState.update { it.copy(error = error.message) } }
                        }
                    }
                }
                .onFailure { error ->
                    loaded = false
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            dashboard = it.dashboard,
                            error = error.message ?: "Không thể tải chương trình đào tạo."
                        )
                    }
                }
        }
    }

    fun submitEvidence(link: String, note: String) {
        if (_uiState.value.isSubmittingEvidence) return
        trainingEvidenceError(link, note)?.let { message ->
            _uiState.update { it.copy(evidenceError = message) }
            return
        }
        val dashboard = _uiState.value.dashboard
        val module = dashboard?.activeModule
        if (dashboard == null || module == null || !loaded || _uiState.value.isLoading) {
            _uiState.update { it.copy(evidenceError = "Chưa tải được chương trình đào tạo. Vui lòng tải lại lộ trình.") }
            return
        }
        _uiState.update { it.copy(isSubmittingEvidence = true, evidenceError = null) }
        submissionJob = viewModelScope.launch {
            repository.submitEvidence(dashboard.program.id, module.id, link.trim(), note.trim())
                .onSuccess { evidence ->
                    _uiState.update {
                        val latest = it.dashboard?.evidence
                        val receipt = if (latest != null && latest != dashboard.evidence &&
                            latest.moduleId == evidence.moduleId && latest.submittedAtMillis >= evidence.submittedAtMillis) latest else evidence
                        it.copy(
                            dashboard = it.dashboard?.copy(evidence = receipt),
                            isSubmittingEvidence = false, evidenceError = null,
                            evidenceSubmissionVersion = it.evidenceSubmissionVersion + 1,
                        )
                    }
                }
                .onFailure { error ->
                    _uiState.update { it.copy(isSubmittingEvidence = false, evidenceError = error.message ?: "Không thể gửi bằng chứng.") }
                }
        }
    }

    fun clearEvidenceError() {
        _uiState.update { it.copy(evidenceError = null) }
    }

    fun clear() {
        observer?.cancel()
        loadJob?.cancel()
        submissionJob?.cancel()
        loaded = false
        _uiState.value = TrainingUiState()
    }
}
