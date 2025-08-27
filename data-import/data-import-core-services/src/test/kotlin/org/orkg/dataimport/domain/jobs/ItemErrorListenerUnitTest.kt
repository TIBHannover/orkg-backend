package org.orkg.dataimport.domain.jobs

import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.Test
import org.springframework.batch.core.ExitStatus
import org.springframework.batch.core.JobExecution
import org.springframework.batch.core.StepExecution

internal class ItemErrorListenerUnitTest {
    private val itemErrorListener = ItemErrorListener<Any, Any>()

    @Test
    fun `Given a step execution, when an exception is thrown during item processing, the execption is appended to the step execution and the step execution is marked as failed`() {
        val stepExecution = StepExecution("test", JobExecution(123))

        itemErrorListener.beforeStep(stepExecution)
        itemErrorListener.onSkipInProcess("item", RuntimeException("Error during process"))
        itemErrorListener.afterStep(stepExecution) shouldBe ExitStatus.FAILED
    }

    @Test
    fun `Given a step execution, when no exception is thrown during item processing, it does nothing`() {
        val stepExecution = StepExecution("test", JobExecution(123))

        itemErrorListener.beforeStep(stepExecution)
        itemErrorListener.afterStep(stepExecution) shouldBe null
    }
}
