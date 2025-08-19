package org.orkg.contenttypes.adapter.input.rest.exceptions

import org.junit.jupiter.api.Test
import org.orkg.common.ThingId
import org.orkg.contenttypes.domain.InvalidBounds
import org.orkg.contenttypes.domain.InvalidCardinality
import org.orkg.contenttypes.domain.InvalidDataType
import org.orkg.contenttypes.domain.InvalidMaxCount
import org.orkg.contenttypes.domain.InvalidMinCount
import org.orkg.contenttypes.domain.InvalidRegexPattern
import org.orkg.contenttypes.domain.TemplateAlreadyExistsForClass
import org.orkg.contenttypes.domain.TemplateClosed
import org.orkg.contenttypes.domain.TemplateNotFound
import org.orkg.graph.domain.Classes
import org.orkg.testing.configuration.FixedClockConfig
import org.orkg.testing.spring.MockMvcExceptionBaseTest
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest
import org.springframework.http.HttpStatus.BAD_REQUEST
import org.springframework.http.HttpStatus.NOT_FOUND
import org.springframework.test.context.ContextConfiguration

@WebMvcTest
@ContextConfiguration(classes = [FixedClockConfig::class])
internal class TemplateExceptionUnitTest : MockMvcExceptionBaseTest() {
    @Test
    fun templateNotFound() {
        documentedGetRequestTo(TemplateNotFound(ThingId("R123")))
            .andExpectErrorStatus(NOT_FOUND)
            .andExpectType("orkg:problem:template_not_found")
            .andExpectTitle("Not Found")
            .andExpectDetail("""Template "R123" not found.""")
            .andDocumentWithDefaultExceptionResponseFields()
    }

    @Test
    fun templateAlreadyExistsForClass() {
        documentedGetRequestTo(TemplateAlreadyExistsForClass(ThingId("C123"), ThingId("R123")))
            .andExpectErrorStatus(BAD_REQUEST)
            .andExpectType("orkg:problem:template_already_exists_for_class")
            .andExpectTitle("Bad Request")
            .andExpectDetail("""Class "C123" already has template "R123".""")
            .andDocumentWithDefaultExceptionResponseFields()
    }

    @Test
    fun invalidMinCount() {
        documentedGetRequestTo(InvalidMinCount(-1))
            .andExpectErrorStatus(BAD_REQUEST)
            .andExpectType("orkg:problem:invalid_min_count")
            .andExpectTitle("Bad Request")
            .andExpectDetail("""Invalid min count "-1". Must be at least 0.""")
            .andDocumentWithDefaultExceptionResponseFields()
    }

    @Test
    fun invalidMaxCount() {
        documentedGetRequestTo(InvalidMaxCount(-1))
            .andExpectErrorStatus(BAD_REQUEST)
            .andExpectType("orkg:problem:invalid_max_count")
            .andExpectTitle("Bad Request")
            .andExpectDetail("""Invalid max count "-1". Must be at least 0.""")
            .andDocumentWithDefaultExceptionResponseFields()
    }

    @Test
    fun invalidCardinality() {
        documentedGetRequestTo(InvalidCardinality(5, 2))
            .andExpectErrorStatus(BAD_REQUEST)
            .andExpectType("orkg:problem:invalid_cardinality")
            .andExpectTitle("Bad Request")
            .andExpectDetail("""Invalid cardinality. Min count must be less than max count. Found: min: "5", max: "2".""")
            .andDocumentWithDefaultExceptionResponseFields()
    }

    @Test
    fun invalidBounds() {
        documentedGetRequestTo(InvalidBounds(5, 2))
            .andExpectErrorStatus(BAD_REQUEST)
            .andExpectType("orkg:problem:invalid_bounds")
            .andExpectTitle("Bad Request")
            .andExpectDetail("""Invalid bounds. Min bound must be less than or equal to max bound. Found: min: "5", max: "2".""")
            .andDocumentWithDefaultExceptionResponseFields()
    }

    @Test
    fun invalidDataType() {
        documentedGetRequestTo(InvalidDataType(ThingId("C123"), Classes.boolean))
            .andExpectErrorStatus(BAD_REQUEST)
            .andExpectType("orkg:problem:invalid_data_type")
            .andExpectTitle("Bad Request")
            .andExpectDetail("""Invalid datatype. Found "C123", expected "Boolean".""")
            .andDocumentWithDefaultExceptionResponseFields()
    }

    @Test
    fun invalidRegexPattern() {
        documentedGetRequestTo(InvalidRegexPattern("\\", Exception("Invalid regex pattern")))
            .andExpectErrorStatus(BAD_REQUEST)
            .andExpectType("orkg:problem:invalid_regex_pattern")
            .andExpectTitle("Bad Request")
            .andExpectDetail("""Invalid regex pattern "\".""")
            .andDocumentWithDefaultExceptionResponseFields()
    }

    @Test
    fun templateClosed() {
        documentedGetRequestTo(TemplateClosed(ThingId("R123")))
            .andExpectErrorStatus(BAD_REQUEST)
            .andExpectType("orkg:problem:template_closed")
            .andExpectTitle("Bad Request")
            .andExpectDetail("""Template "R123" is closed.""")
            .andDocumentWithDefaultExceptionResponseFields()
    }
}
