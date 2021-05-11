package com.avito.instrumentation.internal.verdict

import com.avito.instrumentation.internal.finalizer.verdict.HasFailedTestDeterminer
import com.avito.instrumentation.internal.finalizer.verdict.HasNotReportedTestsDeterminer
import com.avito.instrumentation.internal.finalizer.verdict.Verdict
import com.avito.instrumentation.internal.finalizer.verdict.Verdict.Failure
import com.avito.instrumentation.internal.finalizer.verdict.VerdictDeterminerFactory
import com.avito.report.model.SimpleRunTest
import com.avito.report.model.TestName
import com.avito.report.model.createStubInstance
import com.avito.truth.isInstanceOf
import com.google.common.truth.Truth.assertThat
import org.junit.jupiter.api.Test

internal class VerdictDeterminerTest {

    private val verdictDeterminer = VerdictDeterminerFactory.create()

    @Test
    fun `has 1 unsuppresed test`() {
        val verdict = verdictDeterminer.determine(
            failed = HasFailedTestDeterminer.Result.Failed(
                failed = listOf(
                    SimpleRunTest.createStubInstance()
                )
            ),
            notReported = HasNotReportedTestsDeterminer.Result.AllTestsReported
        )

        assertThat(verdict).isInstanceOf<Failure>()

        verdict.assertVerdictFailure(
            Failure.Details.Test(
                name = TestName("com.Test", "test"),
                devices = setOf("api22")
            )
        )
    }

    @Test
    fun `has 1 unsuppresed test with 2 apis`() {
        val verdict = verdictDeterminer.determine(
            failed = HasFailedTestDeterminer.Result.Failed(
                failed = listOf(
                    SimpleRunTest.createStubInstance(deviceName = "api21"),
                    SimpleRunTest.createStubInstance(deviceName = "api27")
                )
            ),
            notReported = HasNotReportedTestsDeterminer.Result.AllTestsReported
        )

        verdict.assertVerdictFailure(
            Failure.Details.Test(
                name = TestName("com.Test", "test"),
                devices = setOf("api21", "api27")
            )
        )
    }

    private fun Verdict.assertVerdictFailure(
        failedTest: Failure.Details.Test
    ) {
        assertThat(this).apply {
            isInstanceOf(
                Failure::class.java
            )
        }

        with(this as Failure) {
            assertThat(
                prettifiedDetails.lostTests
            ).isEmpty()

            assertThat(
                prettifiedDetails.failedTests
            ).hasSize(1)

            assertThat(
                prettifiedDetails.failedTests.first()
            ).isEqualTo(
                failedTest
            )
        }
    }
}