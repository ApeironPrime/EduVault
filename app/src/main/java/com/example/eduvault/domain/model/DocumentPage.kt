package com.example.eduvault.domain.model


/** A bounded Firestore result page; [nextCursor] is null when there is no next page. */
data class DocumentPage(
    val documents: List<LibraryDocument>,
    val nextCursor: String?
)
