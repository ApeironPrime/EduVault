package com.example.eduvault.data.repository

import com.example.eduvault.domain.model.TrainingEvidence
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.withTimeoutOrNull

/** Call only after the Firestore transaction has been acknowledged by the server. */
internal suspend fun confirmedEvidence(
    acknowledged: TrainingEvidence,
    readReceipt: suspend () -> TrainingEvidence?,
): TrainingEvidence {
    val receipt = try {
        withTimeoutOrNull(4_000) { readReceipt() }
    } catch (cancelled: CancellationException) {
        throw cancelled
    } catch (_: Exception) {
        null
    }
    return receipt?.takeIf {
        it.moduleId == acknowledged.moduleId && it.link == acknowledged.link && it.note == acknowledged.note
    } ?: acknowledged
}
