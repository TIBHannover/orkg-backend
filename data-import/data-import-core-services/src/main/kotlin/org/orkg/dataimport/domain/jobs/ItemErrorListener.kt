package org.orkg.dataimport.domain.jobs

import org.springframework.batch.core.BatchStatus
import org.springframework.batch.core.ExitStatus
import org.springframework.batch.core.SkipListener
import org.springframework.batch.core.StepExecution
import org.springframework.batch.core.StepExecutionListener

/**
 * This listener can be used to track all errors during a step execution.
 * If an error occurred, the job will be marked as failed, after all items have been processed.
 */
open class ItemErrorListener<T : Any, S : Any> :
    SkipListener<T, S>,
    StepExecutionListener {
    private lateinit var stepExecution: StepExecution

    override fun beforeStep(stepExecution: StepExecution) {
        this.stepExecution = stepExecution
    }

    override fun onSkipInProcess(item: T, t: Throwable) {
        stepExecution.addFailureException(t)
    }

    override fun afterStep(stepExecution: StepExecution): ExitStatus? {
        val exceptions: List<Throwable> = stepExecution.failureExceptions

        if (exceptions.isNotEmpty()) {
            // We have to set the step execution status ourselves, otherwise spring batch will continue executing the next step.
            stepExecution.status = BatchStatus.FAILED
            return ExitStatus.FAILED
        }

        return super.afterStep(stepExecution)
    }
}
