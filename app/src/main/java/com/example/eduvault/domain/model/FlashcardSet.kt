package com.example.eduvault.domain.model

data class FlashcardItem(
    val term: String,
    val definition: String
)

data class FlashcardSet(
    val id: String,
    val title: String,
    val cardCount: Int,
    val cards: List<FlashcardItem>,
    val authorId: String,
    val flashcardSetId: String
)
