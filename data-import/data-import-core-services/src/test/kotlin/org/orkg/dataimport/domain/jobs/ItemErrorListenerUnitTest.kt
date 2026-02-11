package org.orkg.dataimport.domain.jobs

import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.Test
import org.orkg.dataimport.domain.testing.fixtures.createStepExecution
import org.springframework.batch.core.ExitStatus

internal class ItemErrorListenerUnitTest {
    private val itemErrorListener = ItemErrorListener<Any, Any>()

    @Test
    fun `Given a step execution, when an exception is thrown during item processing, the execption is appended to the step execution and the step execution is marked as failed`() {
        val stepExecution = createStepExecution()

        itemErrorListener.beforeStep(stepExecution)
        itemErrorListener.onSkipInProcess("item", RuntimeException("Error during process"))
        itemErrorListener.afterStep(stepExecution) shouldBe ExitStatus.FAILED
    }

    @Test
    fun `Given a step execution, when no exception is thrown during item processing, it does nothing`() {
        val stepExecution = createStepExecution()

        itemErrorListener.beforeStep(stepExecution)
        itemErrorListener.afterStep(stepExecution) shouldBe null
    }
}
