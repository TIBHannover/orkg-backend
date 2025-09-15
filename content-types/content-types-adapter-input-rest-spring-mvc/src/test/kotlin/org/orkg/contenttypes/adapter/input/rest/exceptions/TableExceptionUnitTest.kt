package org.orkg.contenttypes.adapter.input.rest.exceptions

import org.junit.jupiter.api.Test
import org.orkg.common.ThingId
import org.orkg.common.json.CommonJacksonModule
import org.orkg.contenttypes.domain.CannotDeleteTableHeader
import org.orkg.contenttypes.domain.InvalidTableRowIndex
import org.orkg.contenttypes.domain.MissingTableHeaderValue
import org.orkg.contenttypes.domain.MissingTableRowValues
import org.orkg.contenttypes.domain.MissingTableRows
import org.orkg.contenttypes.domain.TableColumnNotFound
import org.orkg.contenttypes.domain.TableHeaderValueMustBeLiteral
import org.orkg.contenttypes.domain.TableNotFound
import org.orkg.contenttypes.domain.TableNotModifiable
import org.orkg.contenttypes.domain.TableRowNotFound
import org.orkg.contenttypes.domain.TooFewTableRows
import org.orkg.contenttypes.domain.TooManyTableRowValues
import org.orkg.testing.configuration.FixedClockConfig
import org.orkg.testing.spring.MockMvcExceptionBaseTest
import org.orkg.testing.spring.restdocs.exceptionResponseFields
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest
import org.springframework.http.HttpStatus.BAD_REQUEST
import org.springframework.http.HttpStatus.FORBIDDEN
import org.springframework.http.HttpStatus.NOT_FOUND
import org.springframework.restdocs.payload.PayloadDocumentation.fieldWithPath
import org.springframework.restdocs.payload.PayloadDocumentation.responseFields
import org.springframework.test.context.ContextConfiguration
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath

@WebMvcTest
@ContextConfiguration(classes = [CommonJacksonModule::class, FixedClockConfig::class])
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
            .andDo(
                documentationHandler.document(
                    responseFields(exceptionResponseFields(type)).and(
                        fieldWithPath("table_id").description("The id of the table."),
                    )
                )
            )
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
            .andDo(
                documentationHandler.document(
                    responseFields(exceptionResponseFields(type)).and(
                        fieldWithPath("table_id").description("The id of the table."),
                        fieldWithPath("table_row_index").description("The row index of the table."),
                    )
                )
            )
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
            .andDo(
                documentationHandler.document(
                    responseFields(exceptionResponseFields(type)).and(
                        fieldWithPath("table_id").description("The id of the table."),
                        fieldWithPath("table_column_index").description("The column index of the table."),
                    )
                )
            )
    }

    @Test
    fun missingTableRows() {
        val type = "orkg:problem:missing_table_rows"
        documentedGetRequestTo(MissingTableRows())
            .andExpectErrorStatus(BAD_REQUEST)
            .andExpectType(type)
            .andExpectTitle("Bad Request")
            .andExpectDetail("""Missing table rows. At least one row is required.""")
            .andDocumentWithDefaultExceptionResponseFields(type)
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
            .andDo(
                documentationHandler.document(
                    responseFields(exceptionResponseFields(type)).and(
                        fieldWithPath("table_id").description("The id of the table."),
                    )
                )
            )
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
            .andDo(
                documentationHandler.document(
                    responseFields(exceptionResponseFields(type)).and(
                        fieldWithPath("table_row_index").description("The row index of the table. Always `0`."),
                        fieldWithPath("table_column_index").description("The column index of the table."),
                    )
                )
            )
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
            .andDo(
                documentationHandler.document(
                    responseFields(exceptionResponseFields(type)).and(
                        fieldWithPath("table_row_index").description("The row index of the table. Always `0`."),
                        fieldWithPath("table_column_index").description("The column index of the table."),
                    )
                )
            )
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
            .andDo(
                documentationHandler.document(
                    responseFields(exceptionResponseFields(type)).and(
                        fieldWithPath("table_row_index").description("The row index of the table."),
                        fieldWithPath("expected_table_row_count").description("The expected number of row elements."),
                    )
                )
            )
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
            .andDo(
                documentationHandler.document(
                    responseFields(exceptionResponseFields(type)).and(
                        fieldWithPath("table_row_index").description("The row index of the table."),
                        fieldWithPath("expected_table_row_count").description("The expected number of row elements."),
                    )
                )
            )
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
            .andDo(
                documentationHandler.document(
                    responseFields(exceptionResponseFields(type)).and(
                        fieldWithPath("table_id").description("The id of the table."),
                    )
                )
            )
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
            .andDo(
                documentationHandler.document(
                    responseFields(exceptionResponseFields(type)).and(
                        fieldWithPath("table_row_index").description("The row index of the table. (always `0`)"),
                    )
                )
            )
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
            .andDo(
                documentationHandler.document(
                    responseFields(exceptionResponseFields(type)).and(
                        fieldWithPath("table_row_index").description("The row index of the table. (always `0`)"),
                    )
                )
            )
    }
}
