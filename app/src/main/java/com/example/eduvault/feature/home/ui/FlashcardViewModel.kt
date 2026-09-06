package com.example.eduvault.feature.home.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.eduvault.domain.model.FlashcardSet
import com.example.eduvault.domain.repository.FlashcardRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class FlashcardUiState(
    val isLoading: Boolean = true,
    val sets: List<FlashcardSet> = emptyList(),
    val errorMessage: String? = null
)

@HiltViewModel
class FlashcardViewModel @Inject constructor(
    private val flashcardRepository: FlashcardRepository
) : ViewModel() {
    private val _uiState = MutableStateFlow(FlashcardUiState())
    val uiState = _uiState.asStateFlow()

    fun load() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }
            flashcardRepository.getFlashcardSets()
                .onSuccess { sets -> _uiState.value = FlashcardUiState(isLoading = false, sets = sets) }
                .onFailure { error ->
                    _uiState.value = FlashcardUiState(
                        isLoading = false,
                        errorMessage = error.localizedMessage ?: "Không thể tải bộ flashcard."
                    )
                }
        }
    }
}
