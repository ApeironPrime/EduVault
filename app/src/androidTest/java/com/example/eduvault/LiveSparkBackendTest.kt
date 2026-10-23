package com.example.eduvault

import androidx.test.platform.app.InstrumentationRegistry
import dagger.hilt.android.EntryPointAccessors
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withTimeout
import org.junit.Assert.*
import org.junit.Test
import org.junit.Assume.assumeTrue

/** Deliberately exercises real Spark Auth and Firestore with a labelled test account. */
class LiveSparkBackendTest {
    @Test fun onlineEnrollmentAndEvidencePersistWithoutFunctions() = runBlocking { withTimeout(90_000) {
        assumeTrue("Run live verification explicitly with liveSpark=true", InstrumentationRegistry.getArguments().getString("liveSpark") == "true")
        check(BuildConfig.SPARK_ONLY)
        val context = InstrumentationRegistry.getInstrumentation().targetContext.applicationContext
        val deps = EntryPointAccessors.fromApplication(context, LiveBackendDependencies::class.java)
        assertEquals("appeduvault", deps.firebaseAuth().app.options.projectId)
        val email = "spark-verification@eduvault.local"
        val password = "SparkVerify2026!"
        val registered = deps.auth().register("Kiểm thử Spark", email, password)
        if (registered.isFailure) deps.auth().login(email, password).getOrThrow()
        try {
            val dashboard = deps.training().getMyTrainingProgram().getOrThrow()
            assertTrue(dashboard.program.modules.size >= 2)
            assertTrue(deps.training().submitEvidence(dashboard.program.id,
                dashboard.orderedModules[1].id, "https://github.com/eduvault-verification/pull/1", "Skip attempt").isFailure)
            val receipt = deps.training().submitEvidence(dashboard.program.id,
                requireNotNull(dashboard.activeModule).id,
                "https://github.com/eduvault-verification/pull/1", "Kiểm thử Firestore Spark trực tuyến").getOrThrow()
            assertTrue(receipt.submittedAtMillis > 0)
            val reloaded = deps.training().getMyTrainingProgram().getOrThrow()
            assertEquals(receipt, reloaded.evidence)
            assertEquals(dashboard.assignment.completionPercent, reloaded.assignment.completionPercent)
            assertTrue(deps.training().setModuleCompletion(dashboard.program.id, receipt.moduleId, true).isFailure)
        } finally {
            deps.firebaseAuth().currentUser?.delete()?.await()
            deps.firebaseAuth().signOut()
        }
    } }
}
