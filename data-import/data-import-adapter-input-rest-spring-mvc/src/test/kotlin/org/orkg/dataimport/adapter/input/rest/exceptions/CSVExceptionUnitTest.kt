package org.orkg.dataimport.adapter.input.rest.exceptions

import org.junit.jupiter.api.Test
import org.orkg.dataimport.adapter.input.rest.json.DataImportJacksonModule
import org.orkg.dataimport.domain.CSVAlreadyExists
import org.orkg.dataimport.domain.CSVAlreadyImported
import org.orkg.dataimport.domain.CSVCannotBeBlank
import org.orkg.dataimport.domain.CSVNotFound
import org.orkg.dataimport.domain.csv.CSVID
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
}
