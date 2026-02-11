package org.orkg.dataimport.domain.jobs

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.just
import io.mockk.mockk
import io.mockk.runs
import io.mockk.verify
import org.junit.jupiter.api.Test
import org.orkg.common.exceptions.ProblemResponse
import org.orkg.common.exceptions.ProblemResponseFactory
import org.orkg.common.testing.fixtures.MockkBaseTest
import org.orkg.dataimport.domain.PROBLEMS
import org.orkg.dataimport.domain.getAndCast
import org.orkg.dataimport.domain.internal.RecordParsingException
import org.orkg.dataimport.domain.testing.fixtures.createJobExecution
import org.springframework.batch.core.repository.JobRepository
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR
import org.springframework.http.ProblemDetail

internal class JobExecutionErrorListenerUnitTest : MockkBaseTest {
    private val jobRepository: JobRepository = mockk()
    private val problemResponseFactory: ProblemResponseFactory = mockk()
    private val objectMapper: ObjectMapper = ObjectMapper()

    private val jobExecutionErrorListener = JobExecutionErrorListener(jobRepository, problemResponseFactory, objectMapper)

    @Test
    fun `Given a job execution, when an exception was thrown during a step, it saves the problem detail to the execution context`() {
        val exception = RuntimeException("Error during step")
        val jobExecution = createJobExecution()
        jobExecution.addFailureException(exception)
        val problemDetails = listOf(
            ProblemDetail.forStatusAndDetail(INTERNAL_SERVER_ERROR, exception.message),
        )
        val problemResponse = problemDetails.map { ProblemResponse(HttpHeaders(), it) }

        every { problemResponseFactory.createProblemResponse(exception, INTERNAL_SERVER_ERROR, null) } returnsMany problemResponse
        every { jobRepository.updateExecutionContext(jobExecution) } just runs

        jobExecutionErrorListener.afterJob(jobExecution)
        objectMapper.readValue<List<ProblemDetail>>(jobExecution.executionContext.getAndCast<ByteArray>(PROBLEMS)!!) shouldBe problemDetails

        verify(exactly = 1) { jobRepository.updateExecutionContext(jobExecution) }
        verify(exactly = 1) { problemResponseFactory.createProblemResponse(exception, INTERNAL_SERVER_ERROR, null) }
    }

    @Test
    fun `Given a job execution, when a record parsing exception was thrown during a step, it unwraps the nested exceptions and saves the exceptions to the execution context`() {
        val exception1 = RuntimeException("Error during step 1")
        val exception2 = RuntimeException("Error during step 2")
        val recordParsingException = RecordParsingException(listOf(exception1, exception2))
        val jobExecution = createJobExecution()
        jobExecution.addFailureException(recordParsingException)
        val problemDetails = listOf(
            ProblemDetail.forStatusAndDetail(INTERNAL_SERVER_ERROR, exception1.message),
            ProblemDetail.forStatusAndDetail(INTERNAL_SERVER_ERROR, exception2.message),
        )
        val problemResponse = problemDetails.map { ProblemResponse(HttpHeaders(), it) }

        every { problemResponseFactory.createProblemResponse(any(), INTERNAL_SERVER_ERROR, null) } returnsMany problemResponse
        every { jobRepository.updateExecutionContext(jobExecution) } just runs

        jobExecutionErrorListener.afterJob(jobExecution)
        objectMapper.readValue<List<ProblemDetail>>(jobExecution.executionContext.getAndCast<ByteArray>(PROBLEMS)!!) shouldBe problemDetails

        verify(exactly = 1) { jobRepository.updateExecutionContext(jobExecution) }
        verify(exactly = 2) { problemResponseFactory.createProblemResponse(any(), INTERNAL_SERVER_ERROR, null) }
    }

    @Test
    fun `Given a job execution, when no exception was thrown during a step, it does nothing`() {
        val jobExecution = createJobExecution()

        jobExecutionErrorListener.afterJob(jobExecution)
        jobExecution.executionContext.containsKey(PROBLEMS) shouldBe false
    }
}
