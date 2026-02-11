package org.orkg.dataimport.domain.jobs

import com.fasterxml.jackson.databind.ObjectMapper
import org.orkg.common.exceptions.ProblemResponseFactory
import org.orkg.common.exceptions.SimpleMessageException
import org.orkg.dataimport.domain.PROBLEMS
import org.orkg.dataimport.domain.internal.RecordParsingException
import org.springframework.batch.core.job.JobExecution
import org.springframework.batch.core.listener.JobExecutionListener
import org.springframework.batch.core.repository.JobRepository
import org.springframework.batch.core.step.FatalStepExecutionException
import org.springframework.http.HttpStatus

class JobExecutionErrorListener(
    private val jobRepository: JobRepository,
    private val problemResponseFactory: ProblemResponseFactory,
    private val objectMapper: ObjectMapper,
) : JobExecutionListener {
    public override fun afterJob(jobExecution: JobExecution) {
        val exceptions: List<Throwable> = jobExecution.allFailureExceptions.filter { it !is FatalStepExecutionException }
        if (exceptions.isEmpty()) {
            return
        }
        val problems = exceptions.flatMap { exception ->
            when (exception) {
                is RecordParsingException -> exception.causes.map { it.toProblemDetail() }
                else -> setOf(exception.toProblemDetail())
            }
        }
        // We serialize `problems` using jackson here, because we cannot guarantee
        // that the contents of each problem detail implement java.io.Serializable.
        jobExecution.executionContext.put(PROBLEMS, objectMapper.writeValueAsBytes(problems))
        jobRepository.updateExecutionContext(jobExecution)
    }

    private fun Throwable.toProblemDetail() =
        problemResponseFactory.createProblemResponse(getRootCause(), getStatus(), null).problemDetail

    private fun Throwable.getRootCause(): Throwable =
        when {
            this is SimpleMessageException || cause == null -> this
            else -> cause!!.getRootCause()
        }

    private fun Throwable.getStatus(): HttpStatus =
        when {
            this is SimpleMessageException -> HttpStatus.resolve(statusCode.value()) ?: HttpStatus.INTERNAL_SERVER_ERROR
            cause != null -> cause!!.getStatus()
            else -> HttpStatus.INTERNAL_SERVER_ERROR
        }
}
