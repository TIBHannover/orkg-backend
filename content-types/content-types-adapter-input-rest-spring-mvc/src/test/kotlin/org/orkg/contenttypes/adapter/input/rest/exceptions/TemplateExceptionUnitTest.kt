package org.orkg.contenttypes.adapter.input.rest.exceptions

import org.junit.jupiter.api.Test
import org.orkg.common.ThingId
import org.orkg.common.json.CommonJacksonModule
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
import org.orkg.testing.spring.restdocs.exceptionResponseFields
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest
import org.springframework.http.HttpStatus.BAD_REQUEST
import org.springframework.http.HttpStatus.NOT_FOUND
import org.springframework.restdocs.payload.PayloadDocumentation.fieldWithPath
import org.springframework.restdocs.payload.PayloadDocumentation.responseFields
import org.springframework.test.context.ContextConfiguration
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath

@WebMvcTest
@ContextConfiguration(classes = [CommonJacksonModule::class, FixedClockConfig::class])
internal class TemplateExceptionUnitTest : MockMvcExceptionBaseTest() {
    @Test
    fun templateNotFound() {
        val type = "orkg:problem:template_not_found"
        documentedGetRequestTo(TemplateNotFound(ThingId("R123")))
            .andExpectErrorStatus(NOT_FOUND)
            .andExpectType(type)
            .andExpectTitle("Not Found")
            .andExpectDetail("""Template "R123" not found.""")
            .andExpect(jsonPath("$.template_id").value("R123"))
            .andDo(
                documentationHandler.document(
                    responseFields(exceptionResponseFields(type)).and(
                        fieldWithPath("template_id").description("The id of the template."),
                    )
                )
            )
    }

    @Test
    fun templateAlreadyExistsForClass() {
        val type = "orkg:problem:template_already_exists_for_class"
        documentedGetRequestTo(TemplateAlreadyExistsForClass(ThingId("C123"), ThingId("R123")))
            .andExpectErrorStatus(BAD_REQUEST)
            .andExpectType(type)
            .andExpectTitle("Bad Request")
            .andExpectDetail("""Class "C123" already has template "R123".""")
            .andExpect(jsonPath("$.template_id").value("R123"))
            .andExpect(jsonPath("$.template_target_class_id").value("C123"))
            .andDo(
                documentationHandler.document(
                    responseFields(exceptionResponseFields(type)).and(
                        fieldWithPath("template_id").description("The id of the template."),
                        fieldWithPath("template_target_class_id").description("The target class of the template."),
                    )
                )
            )
    }

    @Test
    fun invalidMinCount() {
        val type = "orkg:problem:invalid_min_count"
        documentedGetRequestTo(InvalidMinCount(-1))
            .andExpectErrorStatus(BAD_REQUEST)
            .andExpectType(type)
            .andExpectTitle("Bad Request")
            .andExpectDetail("""Invalid min count "-1". Must be at least 0.""")
            .andExpect(jsonPath("$.min_count").value("-1"))
            .andDo(
                documentationHandler.document(
                    responseFields(exceptionResponseFields(type)).and(
                        fieldWithPath("min_count").description("The provided min count."),
                    )
                )
            )
    }

    @Test
    fun invalidMaxCount() {
        val type = "orkg:problem:invalid_max_count"
        documentedGetRequestTo(InvalidMaxCount(-1))
            .andExpectErrorStatus(BAD_REQUEST)
            .andExpectType(type)
            .andExpectTitle("Bad Request")
            .andExpectDetail("""Invalid max count "-1". Must be at least 0.""")
            .andExpect(jsonPath("$.max_count").value("-1"))
            .andDo(
                documentationHandler.document(
                    responseFields(exceptionResponseFields(type)).and(
                        fieldWithPath("max_count").description("The provided max count."),
                    )
                )
            )
    }

    @Test
    fun invalidCardinality() {
        val type = "orkg:problem:invalid_cardinality"
        documentedGetRequestTo(InvalidCardinality(5, 2))
            .andExpectErrorStatus(BAD_REQUEST)
            .andExpectType(type)
            .andExpectTitle("Bad Request")
            .andExpectDetail("""Invalid cardinality. Min count must be less than max count. Found: min: "5", max: "2".""")
            .andExpect(jsonPath("$.min_cardinality").value("5"))
            .andExpect(jsonPath("$.max_cardinality").value("2"))
            .andDo(
                documentationHandler.document(
                    responseFields(exceptionResponseFields(type)).and(
                        fieldWithPath("min_cardinality").description("The provided min cardinality."),
                        fieldWithPath("max_cardinality").description("The provided max cardinality."),
                    )
                )
            )
    }

    @Test
    fun invalidBounds() {
        val type = "orkg:problem:invalid_bounds"
        documentedGetRequestTo(InvalidBounds(5, 2))
            .andExpectErrorStatus(BAD_REQUEST)
            .andExpectType(type)
            .andExpectTitle("Bad Request")
            .andExpectDetail("""Invalid bounds. Min bound must be less than or equal to max bound. Found: min: "5", max: "2".""")
            .andExpect(jsonPath("$.min_count").value("5"))
            .andExpect(jsonPath("$.max_count").value("2"))
            .andDo(
                documentationHandler.document(
                    responseFields(exceptionResponseFields(type)).and(
                        fieldWithPath("min_count").description("The provided min count."),
                        fieldWithPath("max_count").description("The provided max count."),
                    )
                )
            )
    }

    @Test
    fun invalidDataType() {
        val type = "orkg:problem:invalid_data_type"
        documentedGetRequestTo(InvalidDataType(ThingId("C123"), Classes.boolean))
            .andExpectErrorStatus(BAD_REQUEST)
            .andExpectType(type)
            .andExpectTitle("Bad Request")
            .andExpectDetail("""Invalid datatype. Found "C123", expected "Boolean".""")
            .andExpect(jsonPath("$.actual_data_type").value("C123"))
            .andExpect(jsonPath("$.expected_data_types[0]").value(Classes.boolean.value))
            .andDo(
                documentationHandler.document(
                    responseFields(exceptionResponseFields(type)).and(
                        fieldWithPath("actual_data_type").description("The provided data type."),
                        fieldWithPath("expected_data_types").description("A list of expected data types."),
                    )
                )
            )
    }

    @Test
    fun invalidRegexPattern() {
        val type = "orkg:problem:invalid_regex_pattern"
        documentedGetRequestTo(InvalidRegexPattern("\\", Exception("Invalid regex pattern")))
            .andExpectErrorStatus(BAD_REQUEST)
            .andExpectType(type)
            .andExpectTitle("Bad Request")
            .andExpectDetail("""Invalid regex pattern "\".""")
            .andExpect(jsonPath("$.regex_pattern").value("\\"))
            .andDo(
                documentationHandler.document(
                    responseFields(exceptionResponseFields(type)).and(
                        fieldWithPath("regex_pattern").description("The provided regex pattern."),
                    )
                )
            )
    }

    @Test
    fun templateClosed() {
        val type = "orkg:problem:template_closed"
        documentedGetRequestTo(TemplateClosed(ThingId("R123")))
            .andExpectErrorStatus(BAD_REQUEST)
            .andExpectType(type)
            .andExpectTitle("Bad Request")
            .andExpectDetail("""Template "R123" is closed.""")
            .andExpect(jsonPath("$.template_id").value("R123"))
            .andDo(
                documentationHandler.document(
                    responseFields(exceptionResponseFields(type)).and(
                        fieldWithPath("template_id").description("The id of the template."),
                    )
                )
            )
    }
}
