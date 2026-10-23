package com.example.eduvault.domain.repository

import com.example.eduvault.domain.model.FlashcardSet

interface FlashcardRepository {
    suspend fun getFlashcardSets(): Result<List<FlashcardSet>>
    suspend fun getUserFlashcardCount(userId: String): Result<Int>
}
