package com.example.eduvault.feature.library.ui

import com.example.eduvault.domain.model.LibraryDocument
import com.example.eduvault.domain.model.LibraryDocumentType

/** Boundary mapper: domain entities are converted only when entering UI state. */
fun LibraryDocument.toLibraryDoc(): LibraryDoc = LibraryDoc(
    id = id,
    courseCode = courseCode,
    title = title,
    type = when (type) {
        LibraryDocumentType.NOTE -> LibraryDocType.NOTE
        LibraryDocumentType.QUIZ -> LibraryDocType.QUIZ
        LibraryDocumentType.SLIDE -> LibraryDocType.SLIDE
        LibraryDocumentType.SUMMARY -> LibraryDocType.SUMMARY
    },
    quantityLabel = quantityLabel,
    rating = rating,
    views = views,
    isSaved = isSaved,
    bgIndex = bgIndex,
    aiVerified = aiVerified,
    aiCheckResult = aiCheckResult,
    status = status,
    reportCount = reportCount,
    fileName = fileName,
    downloadUrl = downloadUrl,
    authorId = authorId,
    fileSizeBytes = fileSizeBytes
)
