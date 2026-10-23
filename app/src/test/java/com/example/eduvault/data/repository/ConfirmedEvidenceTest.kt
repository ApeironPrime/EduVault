package com.example.eduvault.data.repository

import com.example.eduvault.domain.model.TrainingEvidence
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.delay
import kotlinx.coroutines.test.runTest
import org.junit.Assert.*
import org.junit.Test

@OptIn(kotlinx.coroutines.ExperimentalCoroutinesApi::class)
class ConfirmedEvidenceTest {
    private val acknowledged = TrainingEvidence("git", "https://github.com/team/repo", "Proof", 0)

    @Test fun `read failure after committed write does not report failed submission or invent a timestamp`() = runTest {
        val result = confirmedEvidence(acknowledged) { error("Offline after commit") }
        assertEquals(acknowledged, result)
        assertEquals(0L, result.submittedAtMillis)
    }
    @Test fun `missing receipt after write still returns acknowledged submission`() = runTest {
        assertEquals(acknowledged, confirmedEvidence(acknowledged) { null })
    }
    @Test fun `confirmed server receipt includes mentor review if it arrived during read`() = runTest {
        val reviewed = acknowledged.copy(submittedAtMillis = 1000, status = "REJECTED", feedback = "Add screenshot")
        assertEquals(reviewed, confirmedEvidence(acknowledged) { reviewed })
    }
    @Test fun `receipt from another submission does not replace the acknowledged one`() = runTest {
        assertEquals(acknowledged, confirmedEvidence(acknowledged) { acknowledged.copy(link = "https://example.com/old") })
    }
    @Test fun `slow receipt read is bounded without changing write success`() = runTest {
        assertEquals(acknowledged, confirmedEvidence(acknowledged) { delay(10_000); acknowledged })
        assertEquals(4000, testScheduler.currentTime)
    }
    @Test fun `session cancellation is propagated`() = runTest {
        try {
            confirmedEvidence(acknowledged) { throw CancellationException("Signed out") }
            fail("Cancellation must propagate")
        } catch (_: CancellationException) { }
    }
}
