package org.orkg.dataimport.adapter.input.rest.exceptions

import org.junit.jupiter.api.Test
import org.orkg.common.ThingId
import org.orkg.common.json.CommonJacksonModule
import org.orkg.dataimport.domain.BlankCSVHeaderValue
import org.orkg.dataimport.domain.DuplicateCSVHeaders
import org.orkg.dataimport.domain.EmptyCSVHeader
import org.orkg.dataimport.domain.InconsistentCSVColumnCount
import org.orkg.dataimport.domain.InvalidCSVValue
import org.orkg.dataimport.domain.UnexpectedCSVValueType
import org.orkg.dataimport.domain.UnknownCSVNamespace
import org.orkg.dataimport.domain.UnknownCSVNamespaceValue
import org.orkg.dataimport.domain.UnknownCSVValueType
import org.orkg.testing.configuration.FixedClockConfig
import org.orkg.testing.spring.MockMvcExceptionBaseTest
import org.orkg.testing.spring.restdocs.exceptionResponseFields
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest
import org.springframework.http.HttpStatus.BAD_REQUEST
import org.springframework.restdocs.payload.PayloadDocumentation.fieldWithPath
import org.springframework.restdocs.payload.PayloadDocumentation.responseFields
import org.springframework.restdocs.payload.PayloadDocumentation.subsectionWithPath
import org.springframework.test.context.ContextConfiguration
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath

@WebMvcTest
@ContextConfiguration(classes = [CommonJacksonModule::class, FixedClockConfig::class])
internal class CSVParsingExceptionUnitTest : MockMvcExceptionBaseTest() {
    @Test
    fun duplicateCSVHeaders() {
        documentedGetRequestTo(DuplicateCSVHeaders(mapOf("duplicate_header_name" to listOf(0, 2))))
            .andExpectErrorStatus(BAD_REQUEST)
            .andExpectType("orkg:problem:duplicate_csv_headers")
            .andExpectTitle("Bad Request")
            .andExpectDetail("""Duplicate CSV headers "duplicate_header_name" in columns [0, 2].""")
            .andExpect(jsonPath("$.csv_headers").exists())
            .andExpect(jsonPath("$.csv_headers.duplicate_header_name").isArray)
            .andExpect(jsonPath("$.csv_headers.duplicate_header_name[0]").value(0))
            .andExpect(jsonPath("$.csv_headers.duplicate_header_name[1]").value(2))
            .andDo(
                documentationHandler.document(
                    responseFields(exceptionResponseFields()).and(
                        subsectionWithPath("csv_headers").description("A mapping of header names to column indexes."),
                    )
                )
            )
    }

    @Test
    fun blankCSVHeaderValue() {
        documentedGetRequestTo(BlankCSVHeaderValue(2))
            .andExpectErrorStatus(BAD_REQUEST)
            .andExpectType("orkg:problem:blank_csv_header_value")
            .andExpectTitle("Bad Request")
            .andExpectDetail("""The CSV header value in column 2 must not be blank.""")
            .andExpect(jsonPath("$.csv_column").value("2"))
            .andDo(
                documentationHandler.document(
                    responseFields(exceptionResponseFields()).and(
                        fieldWithPath("csv_column").description("The column of the CSV."),
                    )
                )
            )
    }

    @Test
    fun emptyCSVHeader() {
        documentedGetRequestTo(EmptyCSVHeader())
            .andExpectErrorStatus(BAD_REQUEST)
            .andExpectType("orkg:problem:empty_csv_header")
            .andExpectTitle("Bad Request")
            .andExpectDetail("""The CSV header must not be empty.""")
            .andDocumentWithDefaultExceptionResponseFields()
    }

    @Test
    fun unknownCSVNamespace() {
        documentedGetRequestTo(UnknownCSVNamespace("invalid", "value", 1, 2))
            .andExpectErrorStatus(BAD_REQUEST)
            .andExpectType("orkg:problem:unknown_csv_namespace")
            .andExpectTitle("Bad Request")
            .andExpectDetail("""Unknown namespace "invalid" for value "value" in row 1, column 2.""")
            .andExpect(jsonPath("$.csv_namespace").value("invalid"))
            .andExpect(jsonPath("$.csv_value").value("value"))
            .andExpect(jsonPath("$.csv_row").value("1"))
            .andExpect(jsonPath("$.csv_column").value("2"))
            .andDo(
                documentationHandler.document(
                    responseFields(exceptionResponseFields()).and(
                        fieldWithPath("csv_namespace").description("The namespace of the value."),
                        fieldWithPath("csv_value").description("The value of the cell."),
                        fieldWithPath("csv_row").description("The row of the CSV."),
                        fieldWithPath("csv_column").description("The column of the CSV."),
                    )
                )
            )
    }

    @Test
    fun unknownCSVNamespaceValue() {
        documentedGetRequestTo(UnknownCSVNamespaceValue("invalid", "value", 1, 2))
            .andExpectErrorStatus(BAD_REQUEST)
            .andExpectType("orkg:problem:unknown_namespace_value")
            .andExpectTitle("Bad Request")
            .andExpectDetail("""Unknown value "value" for closed namespace "invalid" in row 1, column 2.""")
            .andExpect(jsonPath("$.csv_namespace").value("invalid"))
            .andExpect(jsonPath("$.csv_value").value("value"))
            .andExpect(jsonPath("$.csv_row").value("1"))
            .andExpect(jsonPath("$.csv_column").value("2"))
            .andDo(
                documentationHandler.document(
                    responseFields(exceptionResponseFields()).and(
                        fieldWithPath("csv_namespace").description("The namespace of the value."),
                        fieldWithPath("csv_value").description("The value of the cell."),
                        fieldWithPath("csv_row").description("The row of the CSV."),
                        fieldWithPath("csv_column").description("The column of the CSV."),
                    )
                )
            )
    }

    @Test
    fun unexpectedCSVValueType() {
        documentedGetRequestTo(UnexpectedCSVValueType(ThingId("String"), ThingId("Boolean"), 1, 2))
            .andExpectErrorStatus(BAD_REQUEST)
            .andExpectType("orkg:problem:unexpected_csv_value_type")
            .andExpectTitle("Bad Request")
            .andExpectDetail("""Invalid type "String" for value in row 1, column 2. Expected type "Boolean".""")
            .andExpect(jsonPath("$.actual_csv_cell_value_type").value("String"))
            .andExpect(jsonPath("$.expected_csv_cell_value_type").value("Boolean"))
            .andExpect(jsonPath("$.csv_row").value("1"))
            .andExpect(jsonPath("$.csv_column").value("2"))
            .andDo(
                documentationHandler.document(
                    responseFields(exceptionResponseFields()).and(
                        fieldWithPath("actual_csv_cell_value_type").description("The actual data type of the value."),
                        fieldWithPath("expected_csv_cell_value_type").description("The expected data type of the value."),
                        fieldWithPath("csv_row").description("The row of the CSV."),
                        fieldWithPath("csv_column").description("The column of the CSV."),
                    )
                )
            )
    }

    @Test
    fun unknownCSVValueType() {
        documentedGetRequestTo(UnknownCSVValueType("NotAType", 1, 2))
            .andExpectErrorStatus(BAD_REQUEST)
            .andExpectType("orkg:problem:unknown_csv_value_type")
            .andExpectTitle("Bad Request")
            .andExpectDetail("""Unknown type "NotAType" for value in row 1, column 2.""")
            .andExpect(jsonPath("$.csv_cell_value_type").value("NotAType"))
            .andExpect(jsonPath("$.csv_row").value("1"))
            .andExpect(jsonPath("$.csv_column").value("2"))
            .andDo(
                documentationHandler.document(
                    responseFields(exceptionResponseFields()).and(
                        fieldWithPath("csv_cell_value_type").description("The data type of the value."),
                        fieldWithPath("csv_row").description("The row of the CSV."),
                        fieldWithPath("csv_column").description("The column of the CSV."),
                    )
                )
            )
    }

    @Test
    fun inconsistentCSVColumnCount() {
        documentedGetRequestTo(InconsistentCSVColumnCount(5, 8, 4))
            .andExpectErrorStatus(BAD_REQUEST)
            .andExpectType("orkg:problem:inconsistent_csv_column_count")
            .andExpectTitle("Bad Request")
            .andExpectDetail("""Inconsistent column count in row 4. Found 5, expected 8.""")
            .andExpect(jsonPath("$.actual_csv_column_count").value("5"))
            .andExpect(jsonPath("$.expected_csv_column_count").value("8"))
            .andExpect(jsonPath("$.csv_row").value("4"))
            .andDo(
                documentationHandler.document(
                    responseFields(exceptionResponseFields()).and(
                        fieldWithPath("actual_csv_column_count").description("The actual count of column values in the row."),
                        fieldWithPath("expected_csv_column_count").description("The expected count of column values in the row."),
                        fieldWithPath("csv_row").description("The row of the CSV."),
                    )
                )
            )
    }

    @Test
    fun invalidCSVValue_withReason() {
        documentedGetRequestTo(InvalidCSVValue("5.7", 1, 2, IllegalArgumentException("5.7 is not an Integer")))
            .andExpectErrorStatus(BAD_REQUEST)
            .andExpectType("orkg:problem:invalid_csv_value")
            .andExpectTitle("Bad Request")
            .andExpectDetail("""Invalid value "5.7" in row 1, column 2. Reason: 5.7 is not an Integer""")
            .andExpect(jsonPath("$.csv_cell_value").value("5.7"))
            .andExpect(jsonPath("$.csv_row").value("1"))
            .andExpect(jsonPath("$.csv_column").value("2"))
            .andExpect(jsonPath("$.reason").value("5.7 is not an Integer"))
            .andDo(
                documentationHandler.document(
                    responseFields(exceptionResponseFields()).and(
                        fieldWithPath("csv_cell_value").description("The value of the cell."),
                        fieldWithPath("csv_row").description("The row of the CSV."),
                        fieldWithPath("csv_column").description("The column of the CSV."),
                        fieldWithPath("reason").description("The reason why the value is invalid."),
                    )
                )
            )
    }

    @Test
    fun invalidCSVValue_withRequiredType() {
        get(InvalidCSVValue("5.7", 1, 2, ThingId("Integer")))
            .andExpectErrorStatus(BAD_REQUEST)
            .andExpectType("orkg:problem:invalid_csv_value")
            .andExpectTitle("Bad Request")
            .andExpectDetail("""Invalid value "5.7" in row 1, column 2. Reason: Value cannot be parsed as type "Integer".""")
            .andExpect(jsonPath("$.csv_cell_value").value("5.7"))
            .andExpect(jsonPath("$.csv_row").value("1"))
            .andExpect(jsonPath("$.csv_column").value("2"))
            .andExpect(jsonPath("$.reason").value("""Value cannot be parsed as type "Integer"."""))
    }
}
