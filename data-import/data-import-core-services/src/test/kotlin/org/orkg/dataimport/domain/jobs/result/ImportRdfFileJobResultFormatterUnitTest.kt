package org.orkg.dataimport.domain.jobs.result

import io.kotest.assertions.asClue
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.Test
import org.orkg.common.ThingId
import org.orkg.common.testing.fixtures.MockkBaseTest
import org.orkg.dataimport.domain.JobException
import org.orkg.dataimport.domain.PROBLEMS
import org.orkg.dataimport.domain.RDF_CLASS_IRI_TO_ID
import org.orkg.dataimport.domain.RDF_PREDICATE_IRI_TO_ID
import org.orkg.dataimport.domain.RDF_RESOURCE_IRI_TO_ID
import org.orkg.dataimport.domain.RDF_STATEMENT_IDS
import org.orkg.dataimport.domain.jobs.JobStatus.Status
import org.orkg.dataimport.domain.testing.fixtures.createJobExecution
import org.orkg.dataimport.domain.testing.fixtures.createRdfFileImportResult
import org.orkg.graph.domain.StatementId
import org.springframework.batch.core.BatchStatus
import org.springframework.data.domain.PageRequest
import org.springframework.http.ProblemDetail
import tools.jackson.databind.ObjectMapper
import java.util.Optional

internal class ImportRdfFileJobResultFormatterUnitTest : MockkBaseTest {
    private val objectMapper: ObjectMapper = ObjectMapper()

    private val importRdfFileJobResultFormatter = ImportRdfFileJobResultFormatter()

    @Test
    fun `Given a job execution, when status is pending, it returns an empty result`() {
        val jobExecution = createJobExecution()
        jobExecution.status = BatchStatus.STARTING
        val result = importRdfFileJobResultFormatter.getResult(
            jobExecution = jobExecution,
            status = Status.PENDING,
            pageable = PageRequest.of(0, 10),
            objectMapper = objectMapper,
        )
        result shouldBe Optional.empty()
    }

    @Test
    fun `Given a job execution, when status is running, it returns an empty result`() {
        val jobExecution = createJobExecution()
        jobExecution.status = BatchStatus.STARTED
        val result = importRdfFileJobResultFormatter.getResult(
            jobExecution = jobExecution,
            status = Status.RUNNING,
            pageable = PageRequest.of(0, 10),
            objectMapper = objectMapper,
        )
        result shouldBe Optional.empty()
    }

    @Test
    fun `Given a job execution, when status is stopped, it returns an empty result`() {
        val jobExecution = createJobExecution()
        jobExecution.status = BatchStatus.STOPPED
        val result = importRdfFileJobResultFormatter.getResult(
            jobExecution = jobExecution,
            status = Status.STOPPED,
            pageable = PageRequest.of(0, 10),
            objectMapper = objectMapper,
        )
        result shouldBe Optional.empty()
    }

    @Test
    fun `Given a job execution, when status is failed, and execution context contains error messages, it returns a page of error objects`() {
        val jobExecution = createJobExecution()
        jobExecution.status = BatchStatus.FAILED
        val problemDetails = listOf(ProblemDetail.forStatus(400))
        jobExecution.executionContext.put(PROBLEMS, objectMapper.writeValueAsBytes(problemDetails))
        val pageable = PageRequest.of(0, 10)

        shouldThrow<JobException> {
            importRdfFileJobResultFormatter.getResult(jobExecution, Status.FAILED, pageable, objectMapper)
        }.asClue {
            it.problemDetails shouldBe problemDetails
        }
    }

    @Test
    fun `Given a job execution, when status is failed, and execution does not contain any error messages, it returns a page with a single default error object`() {
        val jobExecution = createJobExecution()
        jobExecution.status = BatchStatus.FAILED
        val pageable = PageRequest.of(0, 10)

        shouldThrow<JobException> {
            importRdfFileJobResultFormatter.getResult(jobExecution, Status.FAILED, pageable, objectMapper)
        }.asClue {
            it.problemDetails shouldBe emptyList()
        }
    }

    @Test
    fun `Given a job execution, when status is done, it returns a page of result objects`() {
        val jobExecution = createJobExecution().apply {
            executionContext.put(RDF_CLASS_IRI_TO_ID, mutableMapOf("https://example.org/C1" to ThingId("C1")))
            executionContext.put(
                RDF_PREDICATE_IRI_TO_ID,
                mutableMapOf(
                    "https://example.org/P1" to ThingId("P1"),
                    "https://example.org/P2" to ThingId("P2"),
                ),
            )
            executionContext.put(
                RDF_RESOURCE_IRI_TO_ID,
                mutableMapOf(
                    "https://example.org/R1" to ThingId("R1"),
                    "https://example.org/R2" to ThingId("R2"),
                    "https://example.org/R3" to ThingId("R3"),
                ),
            )
            executionContext.put(RDF_STATEMENT_IDS, setOf(StatementId("S1"), StatementId("S2"), StatementId("S3"), StatementId("S4")))
        }
        jobExecution.status = BatchStatus.COMPLETED
        val pageable = PageRequest.of(0, 10)
        val expected = createRdfFileImportResult()

        val result = importRdfFileJobResultFormatter.getResult(
            jobExecution = jobExecution,
            status = Status.DONE,
            pageable = pageable,
            objectMapper = objectMapper,
        )
        result shouldBe Optional.of(expected)
    }
}
