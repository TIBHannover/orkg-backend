package org.orkg.dataimport.adapter.input.rest

import com.ninjasquad.springmockk.MockkBean
import io.mockk.every
import io.mockk.just
import io.mockk.runs
import io.mockk.verify
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.orkg.common.ContributorId
import org.orkg.dataimport.adapter.input.rest.mapping.JobResultRepresentationFactory
import org.orkg.dataimport.adapter.input.rest.mapping.jobs.ImportPaperCSVJobResultRepresentationFormatter
import org.orkg.dataimport.adapter.input.rest.mapping.jobs.ValidatePaperCSVJobResultRepresentationFormatter
import org.orkg.dataimport.domain.JobNotComplete
import org.orkg.dataimport.domain.JobNotFound
import org.orkg.dataimport.domain.JobNotRunning
import org.orkg.dataimport.domain.jobs.JobId
import org.orkg.dataimport.domain.jobs.JobNames
import org.orkg.dataimport.domain.jobs.JobResult
import org.orkg.dataimport.domain.jobs.JobStatus
import org.orkg.dataimport.domain.testing.fixtures.createJobResult
import org.orkg.dataimport.domain.testing.fixtures.createJobStatus
import org.orkg.dataimport.domain.testing.fixtures.createPaperCSVRecord
import org.orkg.dataimport.domain.testing.fixtures.createPaperCSVRecordImportResult
import org.orkg.dataimport.input.JobUseCases
import org.orkg.dataimport.testing.fixtures.configuration.DataImportControllerUnitTestConfiguration
import org.orkg.dataimport.testing.fixtures.jobStatusResponseFields
import org.orkg.dataimport.testing.fixtures.paperCSVRecordImportResultResponseFields
import org.orkg.dataimport.testing.fixtures.paperCSVRecordResponseFields
import org.orkg.testing.MockUserId
import org.orkg.testing.andExpectJobStatus
import org.orkg.testing.annotations.TestWithMockAdmin
import org.orkg.testing.pageOf
import org.orkg.testing.spring.MockMvcBaseTest
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest
import org.springframework.restdocs.payload.FieldDescriptor
import org.springframework.restdocs.request.RequestDocumentation.parameterWithName
import org.springframework.test.context.ContextConfiguration
import org.springframework.test.context.TestPropertySource
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import java.util.Optional
import kotlin.reflect.KClass

@ContextConfiguration(
    classes = [
        JobController::class,
        JobResultRepresentationFactory::class,
        ImportPaperCSVJobResultRepresentationFormatter::class,
        ValidatePaperCSVJobResultRepresentationFormatter::class,
        DataImportControllerUnitTestConfiguration::class
    ]
)
@TestPropertySource(properties = ["orkg.import.csv.enabled=true"])
@WebMvcTest(controllers = [JobController::class])
internal class JobControllerUnitTest : MockMvcBaseTest("jobs") {
    @MockkBean
    private lateinit var jobUseCases: JobUseCases

    @Test
    @TestWithMockAdmin
    @DisplayName("Given a job, when it is fetched by id, and service succeeds, then status is 200 OK and job is returned")
    fun findStatusById() {
        val jobStatus = createJobStatus()
        val contributorId = ContributorId(MockUserId.ADMIN)

        every { jobUseCases.findJobStatusById(jobStatus.jobId, contributorId) } returns Optional.of(jobStatus)

        documentedGetRequestTo("/api/jobs/{id}", jobStatus.jobId)
            .perform()
            .andExpect(status().isOk)
            .andExpectJobStatus()
            .andDocument {
                summary("Fetching job status")
                description(
                    """
                    A `GET` request provides information about a job.
                    """
                )
                pathParameters(
                    parameterWithName("id").description("The identifier of the job to retrieve."),
                )
                responseFields<JobStatusRepresentation>(jobStatusResponseFields())
                throws(JobNotFound::class)
            }

        verify(exactly = 1) { jobUseCases.findJobStatusById(jobStatus.jobId, contributorId) }
    }

    private fun findResultsById(schemaClass: KClass<*>, responseFields: List<FieldDescriptor>, jobResult: JobResult) {
        val jobId = JobId(123)
        val contributorId = ContributorId(MockUserId.ADMIN)

        every { jobUseCases.findJobResultById(jobId, contributorId, any()) } returns Optional.of(jobResult)

        documentedGetRequestTo("/api/jobs/{id}/results", jobId)
            .perform()
            .andExpect(status().isOk)
            .andPrint()
            .andDocument {
                summary("Fetching job results")
                description(
                    """
                    A `GET` request provides information about a the results of a job.
                    
                    [NOTE]
                    ====
                    Results are only available for completed jobs.
                    ====
                    """
                )
                pathParameters(
                    parameterWithName("id").description("The identifier of the job to retrieve the results for."),
                )
                pagedQueryParameters()
                pagedResponseFields(schemaClass, responseFields)
                throws(JobNotComplete::class, JobNotFound::class)
            }

        verify(exactly = 1) { jobUseCases.findJobResultById(jobId, contributorId, any()) }
    }

    @Test
    @TestWithMockAdmin
    @DisplayName("Given a job, when its result is fetched by id (paper csv record), and service succeeds, then status is 200 OK and job is returned")
    fun findResultsById_validatePaperCSV_done() {
        findResultsById(
            schemaClass = PaperCSVRecordRepresentation::class,
            responseFields = paperCSVRecordResponseFields(),
            jobResult = createJobResult(
                jobId = JobId(123),
                jobName = JobNames.VALIDATE_PAPER_CSV,
                value = Optional.of(pageOf(createPaperCSVRecord()))
            )
        )
    }

    @Test
    @TestWithMockAdmin
    @DisplayName("Given a job, when its result is fetched by id (paper csv record import result), and service succeeds, then status is 200 OK and job is returned")
    fun findResultsById_importPaperCSV_done() {
        findResultsById(
            schemaClass = PaperCSVRecordImportResultRepresentation::class,
            responseFields = paperCSVRecordImportResultResponseFields(),
            jobResult = createJobResult(
                jobId = JobId(123),
                jobName = JobNames.IMPORT_PAPER_CSV,
                value = Optional.of(pageOf(createPaperCSVRecordImportResult()))
            )
        )
    }

    @Test
    @TestWithMockAdmin
    fun `Given an unfinished job, when its result is fetched by id, then status is 404 NOT FOUND`() {
        val jobId = JobId(123)
        val jobResult = createJobResult().copy(
            status = JobStatus.Status.RUNNING,
            value = Optional.empty()
        )
        val contributorId = ContributorId(MockUserId.ADMIN)

        every { jobUseCases.findJobResultById(jobId, contributorId, any()) } returns Optional.of(jobResult)

        get("/api/jobs/{id}/results", jobId)
            .perform()
            .andExpect(status().isNotFound)

        verify(exactly = 1) { jobUseCases.findJobResultById(jobId, contributorId, any()) }
    }

    @Test
    @TestWithMockAdmin
    @DisplayName("Given a job, when stopping by id, and service succeeds, then status is 204 NO CONTENT")
    fun stopById() {
        val id = JobId(123)
        val contributorId = ContributorId(MockUserId.ADMIN)

        every { jobUseCases.stopJob(id, contributorId) } just runs

        documentedDeleteRequestTo("/api/jobs/{id}", id)
            .perform()
            .andExpect(status().isNoContent)
            .andDocument {
                summary("Stopping jobs")
                description(
                    """
                    A `DELETE` request attempts to stop a running job.
                    The response will be `201 NO CONTENT` when successful.
                    """
                )
                pathParameters(
                    parameterWithName("id").description("The identifier of the job to stop."),
                )
                throws(JobNotFound::class, JobNotRunning::class)
            }

        verify(exactly = 1) { jobUseCases.stopJob(id, contributorId) }
    }
}
