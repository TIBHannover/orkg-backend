package org.orkg.dataimport.adapter.input.rest

import com.ninjasquad.springmockk.MockkBean
import io.mockk.every
import io.mockk.just
import io.mockk.runs
import io.mockk.verify
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.orkg.common.ContributorId
import org.orkg.common.json.CommonJacksonModule
import org.orkg.dataimport.adapter.input.rest.json.DataImportJacksonModule
import org.orkg.dataimport.adapter.input.rest.mapping.JobResultRepresentationFactory
import org.orkg.dataimport.domain.jobs.JobId
import org.orkg.dataimport.domain.jobs.JobStatus
import org.orkg.dataimport.domain.testing.asciidoc.allowedJobStatusValues
import org.orkg.dataimport.domain.testing.fixtures.createJobResult
import org.orkg.dataimport.domain.testing.fixtures.createJobStatus
import org.orkg.dataimport.input.JobUseCases
import org.orkg.testing.MockUserId
import org.orkg.testing.andExpectJobStatus
import org.orkg.testing.annotations.TestWithMockAdmin
import org.orkg.testing.configuration.ExceptionTestConfiguration
import org.orkg.testing.configuration.FixedClockConfig
import org.orkg.testing.spring.MockMvcBaseTest
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest
import org.springframework.restdocs.payload.PayloadDocumentation.fieldWithPath
import org.springframework.restdocs.payload.PayloadDocumentation.responseFields
import org.springframework.restdocs.payload.PayloadDocumentation.subsectionWithPath
import org.springframework.restdocs.request.RequestDocumentation.parameterWithName
import org.springframework.restdocs.request.RequestDocumentation.pathParameters
import org.springframework.test.context.ContextConfiguration
import org.springframework.test.context.TestPropertySource
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import java.util.Optional

@ContextConfiguration(
    classes = [
        JobController::class,
        ExceptionTestConfiguration::class,
        CommonJacksonModule::class,
        DataImportJacksonModule::class,
        JobResultRepresentationFactory::class,
        FixedClockConfig::class
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
    fun getStatus() {
        val jobStatus = createJobStatus()
        val contributorId = ContributorId(MockUserId.ADMIN)

        every { jobUseCases.findJobStatusById(jobStatus.jobId, contributorId) } returns Optional.of(jobStatus)

        documentedGetRequestTo("/api/jobs/{id}", jobStatus.jobId)
            .perform()
            .andExpect(status().isOk)
            .andExpectJobStatus()
            .andDo(
                documentationHandler.document(
                    pathParameters(
                        parameterWithName("id").description("The identifier of the job to retrieve."),
                    ),
                    responseFields(
                        fieldWithPath("job_id").description("The identifier of the job."),
                        fieldWithPath("job_name").description("The name of the job."),
                        subsectionWithPath("context").description("The context of the job. The actual contents depend on the type of job."),
                        fieldWithPath("status").description("The status of the job. Either of $allowedJobStatusValues."),
                    )
                )
            )
            .andDo(generateDefaultDocSnippets())

        verify(exactly = 1) { jobUseCases.findJobStatusById(jobStatus.jobId, contributorId) }
    }

    @Test
    @TestWithMockAdmin
    @DisplayName("Given a job, when its result is fetched by id, and service succeeds, then status is 200 OK and job is returned")
    fun getResults() {
        val jobId = JobId(123)
        val jobResult = createJobResult()
        val contributorId = ContributorId(MockUserId.ADMIN)

        every { jobUseCases.findJobResultById(jobId, contributorId, any()) } returns Optional.of(jobResult)

        documentedGetRequestTo("/api/jobs/{id}/results", jobId)
            .perform()
            .andExpect(status().isOk)
            .andDo(
                documentationHandler.document(
                    pathParameters(
                        parameterWithName("id").description("The identifier of the job to retrieve the results for."),
                    )
                )
            )
            .andDo(generateDefaultDocSnippets())

        verify(exactly = 1) { jobUseCases.findJobResultById(jobId, contributorId, any()) }
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
    fun stop() {
        val id = JobId(123)
        val contributorId = ContributorId(MockUserId.ADMIN)

        every { jobUseCases.stopJob(id, contributorId) } just runs

        documentedDeleteRequestTo("/api/jobs/{id}", id)
            .perform()
            .andExpect(status().isNoContent)
            .andDo(
                documentationHandler.document(
                    pathParameters(
                        parameterWithName("id").description("The identifier of the job to stop."),
                    )
                )
            )
            .andDo(generateDefaultDocSnippets())

        verify(exactly = 1) { jobUseCases.stopJob(id, contributorId) }
    }
}
