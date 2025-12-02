package org.orkg.contenttypes.adapter.input.rest.exceptions

import org.junit.jupiter.api.Test
import org.orkg.common.ThingId
import org.orkg.contenttypes.domain.CannotDeleteTableHeader
import org.orkg.contenttypes.domain.InvalidTableColumnIndex
import org.orkg.contenttypes.domain.InvalidTableRowIndex
import org.orkg.contenttypes.domain.MissingTableColumnValues
import org.orkg.contenttypes.domain.MissingTableHeaderValue
import org.orkg.contenttypes.domain.MissingTableRowValues
import org.orkg.contenttypes.domain.MissingTableRows
import org.orkg.contenttypes.domain.TableColumnNotFound
import org.orkg.contenttypes.domain.TableHeaderValueMustBeLiteral
import org.orkg.contenttypes.domain.TableNotFound
import org.orkg.contenttypes.domain.TableNotModifiable
import org.orkg.contenttypes.domain.TableRowNotFound
import org.orkg.contenttypes.domain.TooFewTableColumns
import org.orkg.contenttypes.domain.TooFewTableRows
import org.orkg.contenttypes.domain.TooManyTableColumnValues
import org.orkg.contenttypes.domain.TooManyTableRowValues
import org.orkg.contenttypes.input.testing.fixtures.configuration.ContentTypeControllerExceptionUnitTestConfiguration
import org.orkg.testing.spring.MockMvcExceptionBaseTest
import org.orkg.testing.spring.restdocs.exceptionResponseFields
import org.orkg.testing.spring.restdocs.type
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest
import org.springframework.http.HttpStatus.BAD_REQUEST
import org.springframework.http.HttpStatus.FORBIDDEN
import org.springframework.http.HttpStatus.NOT_FOUND
import org.springframework.restdocs.payload.PayloadDocumentation.fieldWithPath
import org.springframework.test.context.ContextConfiguration
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath

@WebMvcTest
@ContextConfiguration(classes = [ContentTypeControllerExceptionUnitTestConfiguration::class])
internal class TableExceptionUnitTest : MockMvcExceptionBaseTest() {
    @Test
    fun tableNotFound() {
        val type = "orkg:problem:table_not_found"
        documentedGetRequestTo(TableNotFound(ThingId("R123")))
            .andExpectErrorStatus(NOT_FOUND)
            .andExpectType(type)
            .andExpectTitle("Not Found")
            .andExpectDetail("""Table "R123" not found.""")
            .andExpect(jsonPath("$.table_id").value("R123"))
            .andDocument {
                responseFields<TableNotFound>(
                    fieldWithPath("table_id").description("The id of the table.").type<ThingId>(),
                    *exceptionResponseFields(type).toTypedArray(),
                )
            }
    }

    @Test
    fun tableRowNotFound() {
        val type = "orkg:problem:table_row_not_found"
        documentedGetRequestTo(TableRowNotFound(ThingId("R123"), 5))
            .andExpectErrorStatus(NOT_FOUND)
            .andExpectType(type)
            .andExpectTitle("Not Found")
            .andExpectDetail("""Table row 5 for table "R123" not found.""")
            .andExpect(jsonPath("$.table_id").value("R123"))
            .andExpect(jsonPath("$.table_row_index").value("5"))
            .andDocument {
                responseFields<TableRowNotFound>(
                    fieldWithPath("table_id").description("The id of the table.").type<ThingId>(),
                    fieldWithPath("table_row_index").description("The row index of the table.").type<Int>(),
                    *exceptionResponseFields(type).toTypedArray(),
                )
            }
    }

    @Test
    fun tableColumnNotFound() {
        val type = "orkg:problem:table_column_not_found"
        documentedGetRequestTo(TableColumnNotFound(ThingId("R123"), 5))
            .andExpectErrorStatus(NOT_FOUND)
            .andExpectType(type)
            .andExpectTitle("Not Found")
            .andExpectDetail("""Table column 5 for table "R123" not found.""")
            .andExpect(jsonPath("$.table_id").value("R123"))
            .andExpect(jsonPath("$.table_column_index").value("5"))
            .andDocument {
                responseFields<TableColumnNotFound>(
                    fieldWithPath("table_id").description("The id of the table.").type<ThingId>(),
                    fieldWithPath("table_column_index").description("The column index of the table.").type<Int>(),
                    *exceptionResponseFields(type).toTypedArray(),
                )
            }
    }

    @Test
    fun missingTableRows() {
        val type = "orkg:problem:missing_table_rows"
        documentedGetRequestTo(MissingTableRows())
            .andExpectErrorStatus(BAD_REQUEST)
            .andExpectType(type)
            .andExpectTitle("Bad Request")
            .andExpectDetail("""Missing table rows. At least one row is required.""")
            .andDocumentWithDefaultExceptionResponseFields<MissingTableRows>(type)
    }

    @Test
    fun tooFewTableRows() {
        val type = "orkg:problem:too_few_table_rows"
        documentedGetRequestTo(TooFewTableRows(ThingId("R123")))
            .andExpectErrorStatus(BAD_REQUEST)
            .andExpectType(type)
            .andExpectTitle("Bad Request")
            .andExpectDetail("""Too few table rows for table "R123". At least one row is required.""")
            .andExpect(jsonPath("$.table_id").value("R123"))
            .andDocument {
                responseFields<TooFewTableRows>(
                    fieldWithPath("table_id").description("The id of the table.").type<ThingId>(),
                    *exceptionResponseFields(type).toTypedArray(),
                )
            }
    }

    @Test
    fun tooFewTableColumns() {
        val type = "orkg:problem:too_few_table_columns"
        documentedGetRequestTo(TooFewTableColumns(ThingId("R123")))
            .andExpectErrorStatus(BAD_REQUEST)
            .andExpectType(type)
            .andExpectTitle("Bad Request")
            .andExpectDetail("""The table "R123" has too few columns. At least one column is required.""")
            .andExpect(jsonPath("$.table_id").value("R123"))
            .andDocument {
                responseFields<TooFewTableColumns>(
                    fieldWithPath("table_id").description("The id of the table.").type<ThingId>(),
                    *exceptionResponseFields(type).toTypedArray(),
                )
            }
    }

    @Test
    fun missingTableHeaderValue() {
        val type = "orkg:problem:missing_table_header_value"
        documentedGetRequestTo(MissingTableHeaderValue(5))
            .andExpectErrorStatus(BAD_REQUEST)
            .andExpectType(type)
            .andExpectTitle("Bad Request")
            .andExpectDetail("""Missing table header value at index 5.""")
            .andExpect(jsonPath("$.table_row_index").value("0"))
            .andExpect(jsonPath("$.table_column_index").value("5"))
            .andDocument {
                responseFields<MissingTableHeaderValue>(
                    fieldWithPath("table_row_index").description("The row index of the table. Always `0`.").type<Int>(),
                    fieldWithPath("table_column_index").description("The column index of the table.").type<Int>(),
                    *exceptionResponseFields(type).toTypedArray(),
                )
            }
    }

    @Test
    fun tableHeaderValueMustBeLiteral() {
        val type = "orkg:problem:table_header_value_must_be_literal"
        documentedGetRequestTo(TableHeaderValueMustBeLiteral(5))
            .andExpectErrorStatus(BAD_REQUEST)
            .andExpectType(type)
            .andExpectTitle("Bad Request")
            .andExpectDetail("""Table header value at index 5 must be a literal.""")
            .andExpect(jsonPath("$.table_row_index").value("0"))
            .andExpect(jsonPath("$.table_column_index").value("5"))
            .andDocument {
                responseFields<TableHeaderValueMustBeLiteral>(
                    fieldWithPath("table_row_index").description("The row index of the table. Always `0`.").type<Int>(),
                    fieldWithPath("table_column_index").description("The column index of the table.").type<Int>(),
                    *exceptionResponseFields(type).toTypedArray(),
                )
            }
    }

    @Test
    fun tooManyTableRowValues() {
        val type = "orkg:problem:too_many_table_row_values"
        documentedGetRequestTo(TooManyTableRowValues(5, 10))
            .andExpectErrorStatus(BAD_REQUEST)
            .andExpectType(type)
            .andExpectTitle("Bad Request")
            .andExpectDetail("""Row 5 has more values than the header. Expected exactly 10 values based on header.""")
            .andExpect(jsonPath("$.table_row_index").value("5"))
            .andExpect(jsonPath("$.expected_table_row_count").value("10"))
            .andDocument {
                responseFields<TooManyTableRowValues>(
                    fieldWithPath("table_row_index").description("The row index of the table.").type<Int>(),
                    fieldWithPath("expected_table_row_count").description("The expected number of row elements.").type<Int>(),
                    *exceptionResponseFields(type).toTypedArray(),
                )
            }
    }

    @Test
    fun missingTableRowValues() {
        val type = "orkg:problem:missing_table_row_values"
        documentedGetRequestTo(MissingTableRowValues(10, 5))
            .andExpectErrorStatus(BAD_REQUEST)
            .andExpectType(type)
            .andExpectTitle("Bad Request")
            .andExpectDetail("""Row 10 has less values than the header. Expected exactly 5 values based on header.""")
            .andExpect(jsonPath("$.table_row_index").value("10"))
            .andExpect(jsonPath("$.expected_table_row_count").value("5"))
            .andDocument {
                responseFields<MissingTableRowValues>(
                    fieldWithPath("table_row_index").description("The row index of the table.").type<Int>(),
                    fieldWithPath("expected_table_row_count").description("The expected number of row elements.").type<Int>(),
                    *exceptionResponseFields(type).toTypedArray(),
                )
            }
    }

    @Test
    fun tableNotModifiable() {
        val type = "orkg:problem:table_not_modifiable"
        documentedGetRequestTo(TableNotModifiable(ThingId("R123")))
            .andExpectErrorStatus(FORBIDDEN)
            .andExpectType(type)
            .andExpectTitle("Forbidden")
            .andExpectDetail("""Table "R123" is not modifiable.""")
            .andExpect(jsonPath("$.table_id").value("R123"))
            .andDocument {
                responseFields<TableNotModifiable>(
                    fieldWithPath("table_id").description("The id of the table.").type<ThingId>(),
                    *exceptionResponseFields(type).toTypedArray(),
                )
            }
    }

    @Test
    fun cannotDeleteTableHeader() {
        val type = "orkg:problem:cannot_delete_table_header"
        documentedGetRequestTo(CannotDeleteTableHeader())
            .andExpectErrorStatus(FORBIDDEN)
            .andExpectType(type)
            .andExpectTitle("Forbidden")
            .andExpectDetail("""The table header cannot be deleted.""")
            .andExpect(jsonPath("$.table_row_index").value("0"))
            .andDocument {
                responseFields<CannotDeleteTableHeader>(
                    fieldWithPath("table_row_index").description("The row index of the table. (always `0`)").type<Int>(),
                    *exceptionResponseFields(type).toTypedArray(),
                )
            }
    }

    @Test
    fun invalidTableRowIndex() {
        val type = "orkg:problem:invalid_table_row_index"
        documentedGetRequestTo(InvalidTableRowIndex(-1))
            .andExpectErrorStatus(BAD_REQUEST)
            .andExpectType(type)
            .andExpectTitle("Bad Request")
            .andExpectDetail("""Invalid table row index -1.""")
            .andExpect(jsonPath("$.table_row_index").value("-1"))
            .andDocument {
                responseFields<InvalidTableRowIndex>(
                    fieldWithPath("table_row_index").description("The row index of the table.").type<Int>(),
                    *exceptionResponseFields(type).toTypedArray(),
                )
            }
    }

    @Test
    fun invalidTableColumnIndex() {
        val type = "orkg:problem:invalid_table_column_index"
        documentedGetRequestTo(InvalidTableColumnIndex(-1))
            .andExpectErrorStatus(BAD_REQUEST)
            .andExpectType(type)
            .andExpectTitle("Bad Request")
            .andExpectDetail("""Invalid table column index -1.""")
            .andExpect(jsonPath("$.table_column_index").value("-1"))
            .andDocument {
                responseFields<InvalidTableColumnIndex>(
                    fieldWithPath("table_column_index").description("The column index of the table.").type<Int>(),
                    *exceptionResponseFields(type).toTypedArray(),
                )
            }
    }

    @Test
    fun missingTableColumnValues() {
        val type = "orkg:problem:missing_table_column_values"
        documentedGetRequestTo(MissingTableColumnValues(5, 3))
            .andExpectErrorStatus(BAD_REQUEST)
            .andExpectType(type)
            .andExpectTitle("Bad Request")
            .andExpectDetail("""Table column 5 is missing values. Expected exactly 3 values.""")
            .andExpect(jsonPath("$.table_column_index").value("5"))
            .andExpect(jsonPath("$.expected_table_row_count").value("3"))
            .andDocument {
                responseFields<MissingTableColumnValues>(
                    fieldWithPath("table_column_index").description("The column index of the table.").type<Int>(),
                    fieldWithPath("expected_table_row_count").description("The expected number of column values.").type<Int>(),
                    *exceptionResponseFields(type).toTypedArray(),
                )
            }
    }

    @Test
    fun tooManyTableColumnValues() {
        val type = "orkg:problem:too_many_table_column_values"
        documentedGetRequestTo(TooManyTableColumnValues(5, 3))
            .andExpectErrorStatus(BAD_REQUEST)
            .andExpectType(type)
            .andExpectTitle("Bad Request")
            .andExpectDetail("""Table column 5 has too many values. Expected exactly 3 values.""")
            .andExpect(jsonPath("$.table_column_index").value("5"))
            .andExpect(jsonPath("$.expected_table_row_count").value("3"))
            .andDocument {
                responseFields<TooManyTableColumnValues>(
                    fieldWithPath("table_column_index").description("The column index of the table.").type<Int>(),
                    fieldWithPath("expected_table_row_count").description("The expected number of column values.").type<Int>(),
                    *exceptionResponseFields(type).toTypedArray(),
                )
            }
    }
}
