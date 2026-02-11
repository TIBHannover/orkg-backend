package org.orkg.dataimport.adapter.input.rest.exceptions

import org.junit.jupiter.api.Test
import org.orkg.dataimport.domain.JobAlreadyComplete
import org.orkg.dataimport.domain.JobAlreadyRunning
import org.orkg.dataimport.domain.JobException
import org.orkg.dataimport.domain.JobNotComplete
import org.orkg.dataimport.domain.JobNotFound
import org.orkg.dataimport.domain.JobNotRunning
import org.orkg.dataimport.domain.JobRestartFailed
import org.orkg.dataimport.domain.JobResultNotFound
import org.orkg.dataimport.domain.jobs.JobId
import org.orkg.dataimport.testing.fixtures.configuration.DataImportControllerExceptionUnitTestConfiguration
import org.orkg.testing.spring.MockMvcExceptionBaseTest
import org.orkg.testing.spring.restdocs.arrayItemsType
import org.orkg.testing.spring.restdocs.exceptionResponseFields
import org.orkg.testing.spring.restdocs.exceptionResponseFieldsWithoutDetail
import org.orkg.testing.spring.restdocs.type
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest
import org.springframework.http.HttpStatus.BAD_REQUEST
import org.springframework.http.HttpStatus.NOT_FOUND
import org.springframework.http.ProblemDetail
import org.springframework.restdocs.payload.PayloadDocumentation.fieldWithPath
import org.springframework.restdocs.payload.PayloadDocumentation.subsectionWithPath
import org.springframework.test.context.ContextConfiguration
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath

@WebMvcTest
@ContextConfiguration(classes = [DataImportControllerExceptionUnitTestConfiguration::class])
internal class JobExceptionUnitTest : MockMvcExceptionBaseTest() {
    @Test
    fun jobException() {
        val type = "orkg:problem:job_execution_exception"
        documentedGetRequestTo(JobException(listOf(ProblemDetail.forStatusAndDetail(BAD_REQUEST, "Example detail message"))))
            .andExpectErrorStatus(BAD_REQUEST)
            .andExpectType(type)
            .andExpectTitle("Bad Request")
            .andExpect(jsonPath("$.errors").isArray)
            .andDocument {
                responseFields<JobException>(
                    subsectionWithPath("errors[]").description("A list of RFC 9457 problem details.").arrayItemsType("object"),
                    *exceptionResponseFieldsWithoutDetail(type).toTypedArray(),
                )
            }
    }

    @Test
    fun jobNotFound() {
        val type = "orkg:problem:job_not_found"
        documentedGetRequestTo(JobNotFound(JobId("1")))
            .andExpectErrorStatus(NOT_FOUND)
            .andExpectType(type)
            .andExpectTitle("Not Found")
            .andExpectDetail("""Job "1" not found.""")
            .andExpect(jsonPath("$.job_id").value("1"))
            .andDocument {
                responseFields<JobNotFound>(
                    fieldWithPath("job_id").description("The id of the job.").type<JobId>(),
                    *exceptionResponseFields(type).toTypedArray(),
                )
            }
    }

    @Test
    fun jobResultNotFound() {
        val type = "orkg:problem:job_result_not_found"
        documentedGetRequestTo(JobResultNotFound(JobId("1")))
            .andExpectErrorStatus(NOT_FOUND)
            .andExpectType(type)
            .andExpectTitle("Not Found")
            .andExpectDetail("""Result for job "1" not found.""".trimMargin())
            .andExpect(jsonPath("$.job_id").value("1"))
            .andDocument {
                responseFields<JobResultNotFound>(
                    fieldWithPath("job_id").description("The id of the job.").type<JobId>(),
                    *exceptionResponseFields(type).toTypedArray(),
                )
            }
    }

    @Test
    fun jobNotComplete() {
        val type = "orkg:problem:job_not_complete"
        documentedGetRequestTo(JobNotComplete(JobId("1")))
            .andExpectErrorStatus(BAD_REQUEST)
            .andExpectType(type)
            .andExpectTitle("Bad Request")
            .andExpectDetail("""Job "1" is not complete.""")
            .andExpect(jsonPath("$.job_id").value("1"))
            .andDocument {
                responseFields<JobNotComplete>(
                    fieldWithPath("job_id").description("The id of the job.").type<JobId>(),
                    *exceptionResponseFields(type).toTypedArray(),
                )
            }
    }

    @Test
    fun jobNotRunning() {
        val type = "orkg:problem:job_not_running"
        documentedGetRequestTo(JobNotRunning(JobId("1")))
            .andExpectErrorStatus(BAD_REQUEST)
            .andExpectType(type)
            .andExpectTitle("Bad Request")
            .andExpectDetail("""Job "1" is not running.""")
            .andExpect(jsonPath("$.job_id").value("1"))
            .andDocument {
                responseFields<JobNotRunning>(
                    fieldWithPath("job_id").description("The id of the job.").type<JobId>(),
                    *exceptionResponseFields(type).toTypedArray(),
                )
            }
    }

    @Test
    fun jobAlreadyRunning() {
        val type = "orkg:problem:job_already_running"
        documentedGetRequestTo(JobAlreadyRunning(JobId("1")))
            .andExpectErrorStatus(BAD_REQUEST)
            .andExpectType(type)
            .andExpectTitle("Bad Request")
            .andExpectDetail("""Job "1" is already running.""")
            .andExpect(jsonPath("$.job_id").value("1"))
            .andDocument {
                responseFields<JobAlreadyRunning>(
                    fieldWithPath("job_id").description("The id of the job.").type<JobId>(),
                    *exceptionResponseFields(type).toTypedArray(),
                )
            }
    }

    @Test
    fun jobAlreadyComplete() {
        val type = "orkg:problem:job_already_complete"
        documentedGetRequestTo(JobAlreadyComplete(JobId("1")))
            .andExpectErrorStatus(BAD_REQUEST)
            .andExpectType(type)
            .andExpectTitle("Bad Request")
            .andExpectDetail("""Job "1" is already complete.""")
            .andExpect(jsonPath("$.job_id").value("1"))
            .andDocument {
                responseFields<JobAlreadyComplete>(
                    fieldWithPath("job_id").description("The id of the job.").type<JobId>(),
                    *exceptionResponseFields(type).toTypedArray(),
                )
            }
    }

    @Test
    fun jobRestartFailed() {
        val type = "orkg:problem:job_restart_failed"
        documentedGetRequestTo(JobRestartFailed(JobId("1"), Exception("Error")))
            .andExpectErrorStatus(BAD_REQUEST)
            .andExpectType(type)
            .andExpectTitle("Bad Request")
            .andExpectDetail("""Could not restart job "1".""")
            .andExpect(jsonPath("$.job_id").value("1"))
            .andDocument {
                responseFields<JobRestartFailed>(
                    fieldWithPath("job_id").description("The id of the job.").type<JobId>(),
                    *exceptionResponseFields(type).toTypedArray(),
                )
            }
    }
}
