package org.orkg.contenttypes.adapter.input.rest.exceptions

import org.junit.jupiter.api.Test
import org.orkg.common.ThingId
import org.orkg.contenttypes.domain.MissingTableHeaderValue
import org.orkg.contenttypes.domain.MissingTableRowValues
import org.orkg.contenttypes.domain.MissingTableRows
import org.orkg.contenttypes.domain.TableHeaderValueMustBeLiteral
import org.orkg.contenttypes.domain.TableNotFound
import org.orkg.contenttypes.domain.TableNotModifiable
import org.orkg.contenttypes.domain.TooManyTableRowValues
import org.orkg.testing.configuration.FixedClockConfig
import org.orkg.testing.spring.MockMvcExceptionBaseTest
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest
import org.springframework.http.HttpStatus.BAD_REQUEST
import org.springframework.http.HttpStatus.FORBIDDEN
import org.springframework.http.HttpStatus.NOT_FOUND
import org.springframework.test.context.ContextConfiguration

@WebMvcTest
@ContextConfiguration(classes = [FixedClockConfig::class])
internal class TableExceptionUnitTest : MockMvcExceptionBaseTest() {
    @Test
    fun tableNotFound() {
        documentedGetRequestTo(TableNotFound(ThingId("R123")))
            .andExpectErrorStatus(NOT_FOUND)
            .andExpectType("orkg:problem:table_not_found")
            .andExpectTitle("Not Found")
            .andExpectDetail("""Table "R123" not found.""")
            .andDocumentWithDefaultExceptionResponseFields()
    }

    @Test
    fun missingTableRows() {
        documentedGetRequestTo(MissingTableRows())
            .andExpectErrorStatus(BAD_REQUEST)
            .andExpectType("orkg:problem:missing_table_rows")
            .andExpectTitle("Bad Request")
            .andExpectDetail("""Missing table rows. At least one rows is required.""")
            .andDocumentWithDefaultExceptionResponseFields()
    }

    @Test
    fun missingTableHeaderValue() {
        documentedGetRequestTo(MissingTableHeaderValue(5))
            .andExpectErrorStatus(BAD_REQUEST)
            .andExpectType("orkg:problem:missing_table_header_value")
            .andExpectTitle("Bad Request")
            .andExpectDetail("""Missing table header value at index 5.""")
            .andDocumentWithDefaultExceptionResponseFields()
    }

    @Test
    fun tableHeaderValueMustBeLiteral() {
        documentedGetRequestTo(TableHeaderValueMustBeLiteral(5))
            .andExpectErrorStatus(BAD_REQUEST)
            .andExpectType("orkg:problem:table_header_value_must_be_literal")
            .andExpectTitle("Bad Request")
            .andExpectDetail("""Table header value at index 5 must be a literal.""")
            .andDocumentWithDefaultExceptionResponseFields()
    }

    @Test
    fun tooManyTableRowValues() {
        documentedGetRequestTo(TooManyTableRowValues(5, 10))
            .andExpectErrorStatus(BAD_REQUEST)
            .andExpectType("orkg:problem:too_many_table_row_values")
            .andExpectTitle("Bad Request")
            .andExpectDetail("""Row 5 has more values than the header. Expected exactly 10 values based on header.""")
            .andDocumentWithDefaultExceptionResponseFields()
    }

    @Test
    fun missingTableRowValues() {
        documentedGetRequestTo(MissingTableRowValues(10, 5))
            .andExpectErrorStatus(BAD_REQUEST)
            .andExpectType("orkg:problem:missing_table_row_values")
            .andExpectTitle("Bad Request")
            .andExpectDetail("""Row 10 has less values than the header. Expected exactly 5 values based on header.""")
            .andDocumentWithDefaultExceptionResponseFields()
    }

    @Test
    fun tableNotModifiable() {
        documentedGetRequestTo(TableNotModifiable(ThingId("R123")))
            .andExpectErrorStatus(FORBIDDEN)
            .andExpectType("orkg:problem:table_not_modifiable")
            .andExpectTitle("Forbidden")
            .andExpectDetail("""Table "R123" is not modifiable.""")
            .andDocumentWithDefaultExceptionResponseFields()
    }
}
