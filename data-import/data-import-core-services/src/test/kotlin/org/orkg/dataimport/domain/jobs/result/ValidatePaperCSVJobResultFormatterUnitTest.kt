package org.orkg.dataimport.domain.jobs.result

import com.fasterxml.jackson.databind.ObjectMapper
import io.kotest.assertions.asClue
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.junit.jupiter.api.Test
import org.orkg.common.testing.fixtures.MockkBaseTest
import org.orkg.dataimport.domain.CSV_ID_FIELD
import org.orkg.dataimport.domain.JobException
import org.orkg.dataimport.domain.PROBLEMS
import org.orkg.dataimport.domain.add
import org.orkg.dataimport.domain.csv.CSVID
import org.orkg.dataimport.domain.jobs.JobStatus.Status
import org.orkg.dataimport.domain.testing.fixtures.createJobExecution
import org.orkg.dataimport.domain.testing.fixtures.createPaperCSVRecord
import org.orkg.dataimport.output.PaperCSVRecordRepository
import org.orkg.testing.pageOf
import org.springframework.batch.core.BatchStatus
import org.springframework.batch.core.job.parameters.JobParametersBuilder
import org.springframework.data.domain.PageRequest
import org.springframework.http.ProblemDetail
import java.util.Optional

internal class ValidatePaperCSVJobResultFormatterUnitTest : MockkBaseTest {
    private val paperCSVRecordRepository: PaperCSVRecordRepository = mockk()
    private val objectMapper: ObjectMapper = ObjectMapper()

    private val validatePaperCSVJobResultFormatter = ValidatePaperCSVJobResultFormatter(paperCSVRecordRepository)

    @Test
    fun `Given a job execution, when status is pending, it returns an empty result`() {
        val jobExecution = createJobExecution()
        jobExecution.status = BatchStatus.STARTING
        val result = validatePaperCSVJobResultFormatter.getResult(
            jobExecution = jobExecution,
            status = Status.PENDING,
            pageable = PageRequest.of(0, 10),
            objectMapper = objectMapper
        )
        result shouldBe Optional.empty()
    }

    @Test
    fun `Given a job execution, when status is running, it returns an empty result`() {
        val jobExecution = createJobExecution()
        jobExecution.status = BatchStatus.STARTED
        val result = validatePaperCSVJobResultFormatter.getResult(
            jobExecution = jobExecution,
            status = Status.RUNNING,
            pageable = PageRequest.of(0, 10),
            objectMapper = objectMapper
        )
        result shouldBe Optional.empty()
    }

    @Test
    fun `Given a job execution, when status is stopped, it returns an empty result`() {
        val jobExecution = createJobExecution()
        jobExecution.status = BatchStatus.STOPPED
        val result = validatePaperCSVJobResultFormatter.getResult(
            jobExecution = jobExecution,
            status = Status.STOPPED,
            pageable = PageRequest.of(0, 10),
            objectMapper = objectMapper
        )
        result shouldBe Optional.empty()
    }

    @Test
    fun `Given a job execution, when status is failed, and execution context contains problem details, it throws an exception containing the problem details`() {
        val jobExecution = createJobExecution()
        jobExecution.status = BatchStatus.FAILED
        val problemDetails = listOf<ProblemDetail>(ProblemDetail.forStatus(400))
        jobExecution.executionContext.put(PROBLEMS, objectMapper.writeValueAsBytes(problemDetails))
        val pageable = PageRequest.of(0, 10)

        shouldThrow<JobException> {
            validatePaperCSVJobResultFormatter.getResult(jobExecution, Status.FAILED, pageable, objectMapper)
        }.asClue {
            it.problemDetails shouldBe problemDetails
        }
    }

    @Test
    fun `Given a job execution, when status is failed, and execution does not contain any problem details, it throws an exception`() {
        val jobExecution = createJobExecution()
        jobExecution.status = BatchStatus.FAILED
        val pageable = PageRequest.of(0, 10)

        shouldThrow<JobException> {
            validatePaperCSVJobResultFormatter.getResult(jobExecution, Status.FAILED, pageable, objectMapper)
        }.asClue {
            it.problemDetails shouldBe emptyList()
        }
    }

    @Test
    fun `Given a job execution, when status is done, it returns a page of result objects`() {
        val csvId = CSVID("bf59dd89-6a4b-424b-b9d5-36042661e837")
        val jobParameters = JobParametersBuilder().add(CSV_ID_FIELD, csvId).toJobParameters()
        val jobExecution = createJobExecution(jobParameters = jobParameters)
        jobExecution.status = BatchStatus.COMPLETED
        val pageable = PageRequest.of(0, 10)
        val expected = pageOf(createPaperCSVRecord(), pageable = pageable)

        every { paperCSVRecordRepository.findAllByCSVID(csvId, pageable) } returns expected

        val result = validatePaperCSVJobResultFormatter.getResult(
            jobExecution = jobExecution,
            status = Status.DONE,
            pageable = pageable,
            objectMapper = objectMapper
        )
        result shouldBe Optional.of(expected)

        verify(exactly = 1) { paperCSVRecordRepository.findAllByCSVID(csvId, pageable) }
    }
}
