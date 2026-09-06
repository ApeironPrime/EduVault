package com.example.eduvault.feature.home.ui

import com.example.eduvault.MainDispatcherRule
import com.example.eduvault.domain.model.FlashcardItem
import com.example.eduvault.domain.model.FlashcardSet
import com.example.eduvault.domain.repository.FlashcardRepository
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class FlashcardViewModelTest {
    @get:Rule val mainDispatcherRule = MainDispatcherRule()

    @Test
    fun `load exposes flashcard sets when repository succeeds`() = runTest {
        val viewModel = FlashcardViewModel(FakeFlashcardRepository(Result.success(listOf(sampleSet))))

        viewModel.load()
        advanceUntilIdle()

        assertFalse(viewModel.uiState.value.isLoading)
        assertEquals(listOf(sampleSet), viewModel.uiState.value.sets)
        assertEquals(null, viewModel.uiState.value.errorMessage)
    }

    @Test
    fun `load exposes recoverable error when repository fails`() = runTest {
        val viewModel = FlashcardViewModel(FakeFlashcardRepository(Result.failure(Exception("offline"))))

        viewModel.load()
        advanceUntilIdle()

        assertFalse(viewModel.uiState.value.isLoading)
        assertTrue(viewModel.uiState.value.sets.isEmpty())
        assertEquals("offline", viewModel.uiState.value.errorMessage)
    }

    private class FakeFlashcardRepository(
        private val result: Result<List<FlashcardSet>>
    ) : FlashcardRepository {
        override suspend fun getFlashcardSets(): Result<List<FlashcardSet>> = result
    }

    private companion object {
        val sampleSet = FlashcardSet(
            id = "set-1",
            title = "Economics",
            cardCount = 1,
            cards = listOf(FlashcardItem("Demand", "Quantity requested")),
            authorId = "user-1",
            flashcardSetId = "set-1"
        )
    }
}
