package org.orkg.dataimport.adapter.input.rest.exceptions

import org.junit.jupiter.api.Test
import org.orkg.common.ThingId
import org.orkg.dataimport.domain.BlankCSVHeaderValue
import org.orkg.dataimport.domain.DuplicateCSVHeaders
import org.orkg.dataimport.domain.EmptyCSVHeader
import org.orkg.dataimport.domain.InconsistentCSVColumnCount
import org.orkg.dataimport.domain.InvalidCSVValue
import org.orkg.dataimport.domain.UnexpectedCSVValueType
import org.orkg.dataimport.domain.UnknownCSVNamespace
import org.orkg.dataimport.domain.UnknownCSVNamespaceValue
import org.orkg.dataimport.domain.UnknownCSVValueType
import org.orkg.dataimport.testing.fixtures.configuration.DataImportControllerExceptionUnitTestConfiguration
import org.orkg.testing.spring.MockMvcExceptionBaseTest
import org.orkg.testing.spring.restdocs.arrayItemsType
import org.orkg.testing.spring.restdocs.exceptionResponseFields
import org.orkg.testing.spring.restdocs.format
import org.orkg.testing.spring.restdocs.type
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest
import org.springframework.http.HttpStatus.BAD_REQUEST
import org.springframework.restdocs.payload.PayloadDocumentation.fieldWithPath
import org.springframework.test.context.ContextConfiguration
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath

@WebMvcTest
@ContextConfiguration(classes = [DataImportControllerExceptionUnitTestConfiguration::class])
internal class CSVParsingExceptionUnitTest : MockMvcExceptionBaseTest() {
    @Test
    fun duplicateCSVHeaders() {
        val type = "orkg:problem:duplicate_csv_headers"
        documentedGetRequestTo(DuplicateCSVHeaders(mapOf("duplicate_header_name" to listOf(0, 2))))
            .andExpectErrorStatus(BAD_REQUEST)
            .andExpectType(type)
            .andExpectTitle("Bad Request")
            .andExpectDetail("""Duplicate CSV headers "duplicate_header_name" in columns [0, 2].""")
            .andExpect(jsonPath("$.csv_headers").exists())
            .andExpect(jsonPath("$.csv_headers.duplicate_header_name").isArray)
            .andExpect(jsonPath("$.csv_headers.duplicate_header_name[0]").value(0))
            .andExpect(jsonPath("$.csv_headers.duplicate_header_name[1]").value(2))
            .andDocument {
                responseFields<DuplicateCSVHeaders>(
                    fieldWithPath("csv_headers").description("A key-value map of header names to column indexes."),
                    fieldWithPath("csv_headers.*").description("The list of column indexes."),
                    fieldWithPath("csv_headers.*[]").description("The list of column indexes.").arrayItemsType("integer").format("int64"),
                    *exceptionResponseFields(type).toTypedArray(),
                )
            }
    }

    @Test
    fun blankCSVHeaderValue() {
        val type = "orkg:problem:blank_csv_header_value"
        documentedGetRequestTo(BlankCSVHeaderValue(2))
            .andExpectErrorStatus(BAD_REQUEST)
            .andExpectType(type)
            .andExpectTitle("Bad Request")
            .andExpectDetail("""The CSV header value in column 2 must not be blank.""")
            .andExpect(jsonPath("$.csv_column").value("2"))
            .andDocument {
                responseFields<BlankCSVHeaderValue>(
                    fieldWithPath("csv_column").description("The column index of the CSV.").type<Long>(),
                    *exceptionResponseFields(type).toTypedArray(),
                )
            }
    }

    @Test
    fun emptyCSVHeader() {
        val type = "orkg:problem:empty_csv_header"
        documentedGetRequestTo(EmptyCSVHeader())
            .andExpectErrorStatus(BAD_REQUEST)
            .andExpectType(type)
            .andExpectTitle("Bad Request")
            .andExpectDetail("""The CSV header must not be empty.""")
            .andDocumentWithDefaultExceptionResponseFields<EmptyCSVHeader>(type)
    }

    @Test
    fun unknownCSVNamespace() {
        val type = "orkg:problem:unknown_csv_namespace"
        documentedGetRequestTo(UnknownCSVNamespace("invalid", "value", 1, 2))
            .andExpectErrorStatus(BAD_REQUEST)
            .andExpectType(type)
            .andExpectTitle("Bad Request")
            .andExpectDetail("""Unknown namespace "invalid" for value "value" in row 1, column 2.""")
            .andExpect(jsonPath("$.csv_namespace").value("invalid"))
            .andExpect(jsonPath("$.csv_value").value("value"))
            .andExpect(jsonPath("$.csv_row").value("1"))
            .andExpect(jsonPath("$.csv_column").value("2"))
            .andDocument {
                responseFields<UnknownCSVNamespace>(
                    fieldWithPath("csv_namespace").description("The namespace of the value."),
                    fieldWithPath("csv_value").description("The value of the cell."),
                    fieldWithPath("csv_row").description("The row of the CSV.").type<Long>(),
                    fieldWithPath("csv_column").description("The column of the CSV.").type<Long>(),
                    *exceptionResponseFields(type).toTypedArray(),
                )
            }
    }

    @Test
    fun unknownCSVNamespaceValue() {
        val type = "orkg:problem:unknown_namespace_value"
        documentedGetRequestTo(UnknownCSVNamespaceValue("invalid", "value", 1, 2))
            .andExpectErrorStatus(BAD_REQUEST)
            .andExpectType(type)
            .andExpectTitle("Bad Request")
            .andExpectDetail("""Unknown value "value" for closed namespace "invalid" in row 1, column 2.""")
            .andExpect(jsonPath("$.csv_namespace").value("invalid"))
            .andExpect(jsonPath("$.csv_value").value("value"))
            .andExpect(jsonPath("$.csv_row").value("1"))
            .andExpect(jsonPath("$.csv_column").value("2"))
            .andDocument {
                responseFields<UnknownCSVNamespaceValue>(
                    fieldWithPath("csv_namespace").description("The namespace of the value."),
                    fieldWithPath("csv_value").description("The value of the cell."),
                    fieldWithPath("csv_row").description("The row of the CSV.").type<Long>(),
                    fieldWithPath("csv_column").description("The column of the CSV.").type<Long>(),
                    *exceptionResponseFields(type).toTypedArray(),
                )
            }
    }

    @Test
    fun unexpectedCSVValueType() {
        val type = "orkg:problem:unexpected_csv_value_type"
        documentedGetRequestTo(UnexpectedCSVValueType(ThingId("String"), ThingId("Boolean"), 1, 2))
            .andExpectErrorStatus(BAD_REQUEST)
            .andExpectType(type)
            .andExpectTitle("Bad Request")
            .andExpectDetail("""Invalid type "String" for value in row 1, column 2. Expected type "Boolean".""")
            .andExpect(jsonPath("$.actual_csv_cell_value_type").value("String"))
            .andExpect(jsonPath("$.expected_csv_cell_value_type").value("Boolean"))
            .andExpect(jsonPath("$.csv_row").value("1"))
            .andExpect(jsonPath("$.csv_column").value("2"))
            .andDocument {
                responseFields<UnexpectedCSVValueType>(
                    fieldWithPath("actual_csv_cell_value_type").description("The actual data type of the value.").type<ThingId>(),
                    fieldWithPath("expected_csv_cell_value_type").description("The expected data type of the value.").type<ThingId>(),
                    fieldWithPath("csv_row").description("The row of the CSV.").type<Long>(),
                    fieldWithPath("csv_column").description("The column of the CSV.").type<Long>(),
                    *exceptionResponseFields(type).toTypedArray(),
                )
            }
    }

    @Test
    fun unknownCSVValueType() {
        val type = "orkg:problem:unknown_csv_value_type"
        documentedGetRequestTo(UnknownCSVValueType("NotAType", 1, 2))
            .andExpectErrorStatus(BAD_REQUEST)
            .andExpectType(type)
            .andExpectTitle("Bad Request")
            .andExpectDetail("""Unknown type "NotAType" for value in row 1, column 2.""")
            .andExpect(jsonPath("$.csv_cell_value_type").value("NotAType"))
            .andExpect(jsonPath("$.csv_row").value("1"))
            .andExpect(jsonPath("$.csv_column").value("2"))
            .andDocument {
                responseFields<UnknownCSVValueType>(
                    fieldWithPath("csv_cell_value_type").description("The data type of the value."),
                    fieldWithPath("csv_row").description("The row of the CSV.").type<Long>(),
                    fieldWithPath("csv_column").description("The column of the CSV.").type<Long>(),
                    *exceptionResponseFields(type).toTypedArray(),
                )
            }
    }

    @Test
    fun inconsistentCSVColumnCount() {
        val type = "orkg:problem:inconsistent_csv_column_count"
        documentedGetRequestTo(InconsistentCSVColumnCount(5, 8, 4))
            .andExpectErrorStatus(BAD_REQUEST)
            .andExpectType(type)
            .andExpectTitle("Bad Request")
            .andExpectDetail("""Inconsistent column count in row 4. Found 5, expected 8.""")
            .andExpect(jsonPath("$.actual_csv_column_count").value("5"))
            .andExpect(jsonPath("$.expected_csv_column_count").value("8"))
            .andExpect(jsonPath("$.csv_row").value("4"))
            .andDocument {
                responseFields<InconsistentCSVColumnCount>(
                    fieldWithPath("actual_csv_column_count").description("The actual count of column values in the row.").type<Int>(),
                    fieldWithPath("expected_csv_column_count").description("The expected count of column values in the row.").type<Int>(),
                    fieldWithPath("csv_row").description("The row of the CSV.").type<Long>(),
                    *exceptionResponseFields(type).toTypedArray(),
                )
            }
    }

    @Test
    fun invalidCSVValue_withReason() {
        val type = "orkg:problem:invalid_csv_value"
        documentedGetRequestTo(InvalidCSVValue("5.7", 1, 2, IllegalArgumentException("5.7 is not an Integer")))
            .andExpectErrorStatus(BAD_REQUEST)
            .andExpectType(type)
            .andExpectTitle("Bad Request")
            .andExpectDetail("""Invalid value "5.7" in row 1, column 2. Reason: 5.7 is not an Integer""")
            .andExpect(jsonPath("$.csv_cell_value").value("5.7"))
            .andExpect(jsonPath("$.csv_row").value("1"))
            .andExpect(jsonPath("$.csv_column").value("2"))
            .andExpect(jsonPath("$.reason").value("5.7 is not an Integer"))
            .andDocument {
                responseFields<InvalidCSVValue>(
                    fieldWithPath("csv_cell_value").description("The value of the cell."),
                    fieldWithPath("csv_row").description("The row of the CSV.").type<Long>(),
                    fieldWithPath("csv_column").description("The column of the CSV.").type<Long>(),
                    fieldWithPath("reason").description("The reason why the value is invalid."),
                    *exceptionResponseFields(type).toTypedArray(),
                )
            }
    }

    @Test
    fun invalidCSVValue_withRequiredType() {
        val type = "orkg:problem:invalid_csv_value"
        get(InvalidCSVValue("5.7", 1, 2, ThingId("Integer")))
            .andExpectErrorStatus(BAD_REQUEST)
            .andExpectType(type)
            .andExpectTitle("Bad Request")
            .andExpectDetail("""Invalid value "5.7" in row 1, column 2. Reason: Value cannot be parsed as type "Integer".""")
            .andExpect(jsonPath("$.csv_cell_value").value("5.7"))
            .andExpect(jsonPath("$.csv_row").value("1"))
            .andExpect(jsonPath("$.csv_column").value("2"))
            .andExpect(jsonPath("$.reason").value("""Value cannot be parsed as type "Integer"."""))
    }
}
