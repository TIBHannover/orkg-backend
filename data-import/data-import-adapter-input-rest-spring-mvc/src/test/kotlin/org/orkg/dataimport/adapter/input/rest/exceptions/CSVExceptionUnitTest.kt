package org.orkg.dataimport.adapter.input.rest.exceptions

import org.junit.jupiter.api.Test
import org.orkg.dataimport.adapter.input.rest.json.DataImportJacksonModule
import org.orkg.dataimport.domain.CSVAlreadyExists
import org.orkg.dataimport.domain.CSVAlreadyImported
import org.orkg.dataimport.domain.CSVAlreadyValidated
import org.orkg.dataimport.domain.CSVCannotBeBlank
import org.orkg.dataimport.domain.CSVImportAlreadyRunning
import org.orkg.dataimport.domain.CSVImportJobNotFound
import org.orkg.dataimport.domain.CSVImportRestartFailed
import org.orkg.dataimport.domain.CSVNotFound
import org.orkg.dataimport.domain.CSVNotValidated
import org.orkg.dataimport.domain.CSVValidationAlreadyRunning
import org.orkg.dataimport.domain.CSVValidationJobNotFound
import org.orkg.dataimport.domain.CSVValidationRestartFailed
import org.orkg.dataimport.domain.csv.CSVID
import org.orkg.dataimport.domain.jobs.JobId
import org.orkg.testing.configuration.FixedClockConfig
import org.orkg.testing.spring.MockMvcExceptionBaseTest
import org.orkg.testing.spring.restdocs.exceptionResponseFields
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest
import org.springframework.http.HttpStatus.BAD_REQUEST
import org.springframework.http.HttpStatus.NOT_FOUND
import org.springframework.restdocs.payload.PayloadDocumentation.fieldWithPath
import org.springframework.restdocs.payload.PayloadDocumentation.responseFields
import org.springframework.test.context.ContextConfiguration
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath

@WebMvcTest
@ContextConfiguration(classes = [DataImportJacksonModule::class, FixedClockConfig::class])
internal class CSVExceptionUnitTest : MockMvcExceptionBaseTest() {
    @Test
    fun csvAlreadyExists() {
        documentedGetRequestTo(CSVAlreadyExists())
            .andExpectErrorStatus(BAD_REQUEST)
            .andExpectType("orkg:problem:csv_already_exists")
            .andExpectTitle("Bad Request")
            .andExpectDetail("""A CSV with the same data already exists.""")
            .andDocumentWithDefaultExceptionResponseFields()
    }

    @Test
    fun csvCannotBeBlank() {
        documentedGetRequestTo(CSVCannotBeBlank())
            .andExpectErrorStatus(BAD_REQUEST)
            .andExpectType("orkg:problem:csv_cannot_be_blank")
            .andExpectTitle("Bad Request")
            .andExpectDetail("""The CSV can not be blank.""")
            .andDocumentWithDefaultExceptionResponseFields()
    }

    @Test
    fun csvVNotFound() {
        documentedGetRequestTo(CSVNotFound(CSVID("6d57f7fd-5f34-4f1a-985d-affc3e22194b")))
            .andExpectErrorStatus(NOT_FOUND)
            .andExpectType("orkg:problem:csv_not_found")
            .andExpectTitle("Not Found")
            .andExpectDetail("""CSV "6d57f7fd-5f34-4f1a-985d-affc3e22194b" not found.""")
            .andExpect(jsonPath("$.csv_id").value("6d57f7fd-5f34-4f1a-985d-affc3e22194b"))
            .andDo(
                documentationHandler.document(
                    responseFields(exceptionResponseFields()).and(
                        fieldWithPath("csv_id").description("The id of the CSV."),
                    )
                )
            )
    }

    @Test
    fun csvValidationJobNotFound() {
        documentedGetRequestTo(CSVValidationJobNotFound(CSVID("6d57f7fd-5f34-4f1a-985d-affc3e22194b"), JobId("1")))
            .andExpectErrorStatus(NOT_FOUND)
            .andExpectType("orkg:problem:csv_validation_job_not_found")
            .andExpectTitle("Not Found")
            .andExpectDetail("""CSV validation job not found.""")
            .andExpect(jsonPath("$.csv_id").value("6d57f7fd-5f34-4f1a-985d-affc3e22194b"))
            .andExpect(jsonPath("$.job_id").value("1"))
            .andDo(
                documentationHandler.document(
                    responseFields(exceptionResponseFields()).and(
                        fieldWithPath("csv_id").description("The id of the CSV."),
                        fieldWithPath("job_id").description("The id of the job."),
                    )
                )
            )
    }

    @Test
    fun csvImportJobNotFound() {
        documentedGetRequestTo(CSVImportJobNotFound(CSVID("6d57f7fd-5f34-4f1a-985d-affc3e22194b"), JobId("1")))
            .andExpectErrorStatus(NOT_FOUND)
            .andExpectType("orkg:problem:csv_import_job_not_found")
            .andExpectTitle("Not Found")
            .andExpectDetail("""CSV import job not found.""")
            .andExpect(jsonPath("$.csv_id").value("6d57f7fd-5f34-4f1a-985d-affc3e22194b"))
            .andExpect(jsonPath("$.job_id").value("1"))
            .andDo(
                documentationHandler.document(
                    responseFields(exceptionResponseFields()).and(
                        fieldWithPath("csv_id").description("The id of the CSV."),
                        fieldWithPath("job_id").description("The id of the job."),
                    )
                )
            )
    }

    @Test
    fun csvAlreadyValidated() {
        documentedGetRequestTo(CSVAlreadyValidated(CSVID("6d57f7fd-5f34-4f1a-985d-affc3e22194b")))
            .andExpectErrorStatus(BAD_REQUEST)
            .andExpectType("orkg:problem:csv_already_validated")
            .andExpectTitle("Bad Request")
            .andExpectDetail("""CSV "6d57f7fd-5f34-4f1a-985d-affc3e22194b" was already validated.""")
            .andExpect(jsonPath("$.csv_id").value("6d57f7fd-5f34-4f1a-985d-affc3e22194b"))
            .andDo(
                documentationHandler.document(
                    responseFields(exceptionResponseFields()).and(
                        fieldWithPath("csv_id").description("The id of the CSV."),
                    )
                )
            )
    }

    @Test
    fun csvValidationAlreadyRunning() {
        documentedGetRequestTo(CSVValidationAlreadyRunning(CSVID("6d57f7fd-5f34-4f1a-985d-affc3e22194b")))
            .andExpectErrorStatus(BAD_REQUEST)
            .andExpectType("orkg:problem:csv_validation_already_running")
            .andExpectTitle("Bad Request")
            .andExpectDetail("""Validation for CSV "6d57f7fd-5f34-4f1a-985d-affc3e22194b" is already running.""")
            .andExpect(jsonPath("$.csv_id").value("6d57f7fd-5f34-4f1a-985d-affc3e22194b"))
            .andDo(
                documentationHandler.document(
                    responseFields(exceptionResponseFields()).and(
                        fieldWithPath("csv_id").description("The id of the CSV."),
                    )
                )
            )
    }

    @Test
    fun csvValidationRestartFailed() {
        documentedGetRequestTo(CSVValidationRestartFailed(CSVID("6d57f7fd-5f34-4f1a-985d-affc3e22194b"), Exception("Error")))
            .andExpectErrorStatus(BAD_REQUEST)
            .andExpectType("orkg:problem:csv_validation_restart_failed")
            .andExpectTitle("Bad Request")
            .andExpectDetail("""Could not restart validation for CSV "6d57f7fd-5f34-4f1a-985d-affc3e22194b".""")
            .andExpect(jsonPath("$.csv_id").value("6d57f7fd-5f34-4f1a-985d-affc3e22194b"))
            .andDo(
                documentationHandler.document(
                    responseFields(exceptionResponseFields()).and(
                        fieldWithPath("csv_id").description("The id of the CSV."),
                    )
                )
            )
    }

    @Test
    fun csvNotValidated() {
        documentedGetRequestTo(CSVNotValidated(CSVID("6d57f7fd-5f34-4f1a-985d-affc3e22194b")))
            .andExpectErrorStatus(BAD_REQUEST)
            .andExpectType("orkg:problem:csv_not_validated")
            .andExpectTitle("Bad Request")
            .andExpectDetail("""CSV "6d57f7fd-5f34-4f1a-985d-affc3e22194b" must be validated before import.""")
            .andExpect(jsonPath("$.csv_id").value("6d57f7fd-5f34-4f1a-985d-affc3e22194b"))
            .andDo(
                documentationHandler.document(
                    responseFields(exceptionResponseFields()).and(
                        fieldWithPath("csv_id").description("The id of the CSV."),
                    )
                )
            )
    }

    @Test
    fun csvAlreadyImported() {
        documentedGetRequestTo(CSVAlreadyImported(CSVID("6d57f7fd-5f34-4f1a-985d-affc3e22194b")))
            .andExpectErrorStatus(BAD_REQUEST)
            .andExpectType("orkg:problem:csv_already_imported")
            .andExpectTitle("Bad Request")
            .andExpectDetail("""CSV "6d57f7fd-5f34-4f1a-985d-affc3e22194b" was already imported.""")
            .andExpect(jsonPath("$.csv_id").value("6d57f7fd-5f34-4f1a-985d-affc3e22194b"))
            .andDo(
                documentationHandler.document(
                    responseFields(exceptionResponseFields()).and(
                        fieldWithPath("csv_id").description("The id of the CSV."),
                    )
                )
            )
    }

    @Test
    fun csvImportAlreadyRunning() {
        documentedGetRequestTo(CSVImportAlreadyRunning(CSVID("6d57f7fd-5f34-4f1a-985d-affc3e22194b")))
            .andExpectErrorStatus(BAD_REQUEST)
            .andExpectType("orkg:problem:csv_import_already_running")
            .andExpectTitle("Bad Request")
            .andExpectDetail("""Import for CSV "6d57f7fd-5f34-4f1a-985d-affc3e22194b" is already running.""")
            .andExpect(jsonPath("$.csv_id").value("6d57f7fd-5f34-4f1a-985d-affc3e22194b"))
            .andDo(
                documentationHandler.document(
                    responseFields(exceptionResponseFields()).and(
                        fieldWithPath("csv_id").description("The id of the CSV."),
                    )
                )
            )
    }

    @Test
    fun csvImportRestartFailed() {
        documentedGetRequestTo(CSVImportRestartFailed(CSVID("6d57f7fd-5f34-4f1a-985d-affc3e22194b"), Exception("Error")))
            .andExpectErrorStatus(BAD_REQUEST)
            .andExpectType("orkg:problem:csv_import_restart_failed")
            .andExpectTitle("Bad Request")
            .andExpectDetail("""Could not restart import for CSV "6d57f7fd-5f34-4f1a-985d-affc3e22194b".""")
            .andExpect(jsonPath("$.csv_id").value("6d57f7fd-5f34-4f1a-985d-affc3e22194b"))
            .andDo(
                documentationHandler.document(
                    responseFields(exceptionResponseFields()).and(
                        fieldWithPath("csv_id").description("The id of the CSV."),
                    )
                )
            )
    }
}
