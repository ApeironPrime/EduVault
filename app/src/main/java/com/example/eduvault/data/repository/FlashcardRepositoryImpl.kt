package com.example.eduvault.data.repository

import com.example.eduvault.domain.model.FlashcardItem
import com.example.eduvault.domain.model.FlashcardSet
import com.example.eduvault.domain.repository.FlashcardRepository
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

/** Firebase-only mapper for flashcard data; feature UI never touches Firestore. */
@Singleton
class FlashcardRepositoryImpl @Inject constructor(
    private val firestore: FirebaseFirestore
) : FlashcardRepository {
    override suspend fun getFlashcardSets(): Result<List<FlashcardSet>> = runCatching {
        firestore.collection("flashcards").get().await().documents.map { document ->
            val cards = (document.get("cards") as? List<*>)
                .orEmpty()
                .mapNotNull { raw ->
                    val card = raw as? Map<*, *> ?: return@mapNotNull null
                    val term = card["term"] as? String ?: return@mapNotNull null
                    val definition = card["definition"] as? String ?: return@mapNotNull null
                    FlashcardItem(term = term, definition = definition)
                }
            FlashcardSet(
                id = document.id,
                title = document.getString("title") ?: "Bộ thẻ chưa đặt tên",
                cardCount = document.getLong("cardCount")?.toInt() ?: cards.size,
                cards = cards,
                authorId = document.getString("authorId") ?: "",
                flashcardSetId = document.getString("flashcardSetID")
                    ?: document.getString("documentID")
                    ?: document.id
            )
        }
    }

    override suspend fun getUserFlashcardCount(userId: String): Result<Int> = runCatching {
        firestore.collection("flashcards")
            .whereEqualTo("authorId", userId)
            .get().await()
            .size()
    }
}
