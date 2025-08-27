package org.orkg.dataimport.adapter.input.rest.exceptions

import org.junit.jupiter.api.Test
import org.orkg.dataimport.adapter.input.rest.json.DataImportJacksonModule
import org.orkg.dataimport.domain.JobAlreadyComplete
import org.orkg.dataimport.domain.JobAlreadyRunning
import org.orkg.dataimport.domain.JobException
import org.orkg.dataimport.domain.JobNotComplete
import org.orkg.dataimport.domain.JobNotFound
import org.orkg.dataimport.domain.JobNotRunning
import org.orkg.dataimport.domain.JobRestartFailed
import org.orkg.dataimport.domain.JobResultNotFound
import org.orkg.dataimport.domain.jobs.JobId
import org.orkg.testing.configuration.FixedClockConfig
import org.orkg.testing.spring.MockMvcExceptionBaseTest
import org.orkg.testing.spring.restdocs.exceptionResponseFields
import org.orkg.testing.spring.restdocs.exceptionResponseFieldsWithoutDetail
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest
import org.springframework.http.HttpStatus.BAD_REQUEST
import org.springframework.http.HttpStatus.NOT_FOUND
import org.springframework.http.ProblemDetail
import org.springframework.restdocs.payload.PayloadDocumentation.fieldWithPath
import org.springframework.restdocs.payload.PayloadDocumentation.responseFields
import org.springframework.restdocs.payload.PayloadDocumentation.subsectionWithPath
import org.springframework.test.context.ContextConfiguration
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath

@WebMvcTest
@ContextConfiguration(classes = [DataImportJacksonModule::class, FixedClockConfig::class])
internal class JobExceptionUnitTest : MockMvcExceptionBaseTest() {
    @Test
    fun jobException() {
        documentedGetRequestTo(JobException(listOf(ProblemDetail.forStatusAndDetail(BAD_REQUEST, "Example detail message"))))
            .andExpectErrorStatus(BAD_REQUEST)
            .andExpectType("orkg:problem:job_execution_exception")
            .andExpectTitle("Bad Request")
            .andExpect(jsonPath("$.errors").isArray)
            .andDo(
                documentationHandler.document(
                    responseFields(exceptionResponseFieldsWithoutDetail()).and(
                        subsectionWithPath("errors[]").description("A list of RFC 9457 problem details."),
                    )
                )
            )
    }

    @Test
    fun jobNotFound() {
        documentedGetRequestTo(JobNotFound(JobId("1")))
            .andExpectErrorStatus(NOT_FOUND)
            .andExpectType("orkg:problem:job_not_found")
            .andExpectTitle("Not Found")
            .andExpectDetail("""Job "1" not found.""")
            .andExpect(jsonPath("$.job_id").value("1"))
            .andDo(
                documentationHandler.document(
                    responseFields(exceptionResponseFields()).and(
                        fieldWithPath("job_id").description("The id of the job."),
                    )
                )
            )
    }

    @Test
    fun jobResultNotFound() {
        documentedGetRequestTo(JobResultNotFound(JobId("1")))
            .andExpectErrorStatus(NOT_FOUND)
            .andExpectType("orkg:problem:job_result_not_found")
            .andExpectTitle("Not Found")
            .andExpectDetail("""Result for job "1" not found.""".trimMargin())
            .andExpect(jsonPath("$.job_id").value("1"))
            .andDo(
                documentationHandler.document(
                    responseFields(exceptionResponseFields()).and(
                        fieldWithPath("job_id").description("The id of the job."),
                    )
                )
            )
    }

    @Test
    fun jobNotComplete() {
        documentedGetRequestTo(JobNotComplete(JobId("1")))
            .andExpectErrorStatus(BAD_REQUEST)
            .andExpectType("orkg:problem:job_not_complete")
            .andExpectTitle("Bad Request")
            .andExpectDetail("""Job "1" is not complete.""")
            .andExpect(jsonPath("$.job_id").value("1"))
            .andDo(
                documentationHandler.document(
                    responseFields(exceptionResponseFields()).and(
                        fieldWithPath("job_id").description("The id of the job."),
                    )
                )
            )
    }

    @Test
    fun jobNotRunning() {
        documentedGetRequestTo(JobNotRunning(JobId("1")))
            .andExpectErrorStatus(BAD_REQUEST)
            .andExpectType("orkg:problem:job_not_running")
            .andExpectTitle("Bad Request")
            .andExpectDetail("""Job "1" is not running.""")
            .andExpect(jsonPath("$.job_id").value("1"))
            .andDo(
                documentationHandler.document(
                    responseFields(exceptionResponseFields()).and(
                        fieldWithPath("job_id").description("The id of the job."),
                    )
                )
            )
    }

    @Test
    fun jobAlreadyRunning() {
        documentedGetRequestTo(JobAlreadyRunning(JobId("1")))
            .andExpectErrorStatus(BAD_REQUEST)
            .andExpectType("orkg:problem:job_already_running")
            .andExpectTitle("Bad Request")
            .andExpectDetail("""Job "1" is already running.""")
            .andExpect(jsonPath("$.job_id").value("1"))
            .andDo(
                documentationHandler.document(
                    responseFields(exceptionResponseFields()).and(
                        fieldWithPath("job_id").description("The id of the job."),
                    )
                )
            )
    }

    @Test
    fun jobAlreadyComplete() {
        documentedGetRequestTo(JobAlreadyComplete(JobId("1")))
            .andExpectErrorStatus(BAD_REQUEST)
            .andExpectType("orkg:problem:job_already_complete")
            .andExpectTitle("Bad Request")
            .andExpectDetail("""Job "1" is already complete.""")
            .andExpect(jsonPath("$.job_id").value("1"))
            .andDo(
                documentationHandler.document(
                    responseFields(exceptionResponseFields()).and(
                        fieldWithPath("job_id").description("The id of the job."),
                    )
                )
            )
    }

    @Test
    fun jobRestartFailed() {
        documentedGetRequestTo(JobRestartFailed(JobId("1"), Exception("Error")))
            .andExpectErrorStatus(BAD_REQUEST)
            .andExpectType("orkg:problem:job_restart_failed")
            .andExpectTitle("Bad Request")
            .andExpectDetail("""Could not restart job "1".""")
            .andExpect(jsonPath("$.job_id").value("1"))
            .andDo(
                documentationHandler.document(
                    responseFields(exceptionResponseFields()).and(
                        fieldWithPath("job_id").description("The id of the job."),
                    )
                )
            )
    }
}
