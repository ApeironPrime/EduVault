package com.example.eduvault.domain.usecase

import com.example.eduvault.domain.model.LibraryDocument
import com.example.eduvault.domain.repository.DocumentRepository
import javax.inject.Inject

/** Single application action shared by Home and Quiz; presentation stays data-source agnostic. */
class GetPublishedDocumentsUseCase @Inject constructor(
    private val documentRepository: DocumentRepository
) {
    suspend operator fun invoke(): Result<List<LibraryDocument>> = documentRepository.getDocuments()
}
