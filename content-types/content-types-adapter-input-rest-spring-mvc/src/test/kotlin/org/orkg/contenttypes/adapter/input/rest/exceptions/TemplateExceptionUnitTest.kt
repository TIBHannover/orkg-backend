package org.orkg.contenttypes.adapter.input.rest.exceptions

import org.junit.jupiter.api.Test
import org.orkg.common.ThingId
import org.orkg.common.thingIdConstraint
import org.orkg.contenttypes.domain.DuplicateTemplatePropertyPaths
import org.orkg.contenttypes.domain.InvalidBounds
import org.orkg.contenttypes.domain.InvalidCardinality
import org.orkg.contenttypes.domain.InvalidDataType
import org.orkg.contenttypes.domain.InvalidMaxCount
import org.orkg.contenttypes.domain.InvalidMinCount
import org.orkg.contenttypes.domain.InvalidRegexPattern
import org.orkg.contenttypes.domain.TemplateAlreadyExistsForClass
import org.orkg.contenttypes.domain.TemplateClosed
import org.orkg.contenttypes.domain.TemplateNotFound
import org.orkg.contenttypes.input.testing.fixtures.configuration.ContentTypeControllerExceptionUnitTestConfiguration
import org.orkg.graph.domain.Classes
import org.orkg.testing.spring.MockMvcExceptionBaseTest
import org.orkg.testing.spring.restdocs.arrayItemsType
import org.orkg.testing.spring.restdocs.constraints
import org.orkg.testing.spring.restdocs.exceptionResponseFields
import org.orkg.testing.spring.restdocs.type
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest
import org.springframework.http.HttpStatus.BAD_REQUEST
import org.springframework.http.HttpStatus.NOT_FOUND
import org.springframework.restdocs.payload.PayloadDocumentation.fieldWithPath
import org.springframework.test.context.ContextConfiguration
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath

@WebMvcTest
@ContextConfiguration(classes = [ContentTypeControllerExceptionUnitTestConfiguration::class])
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
            .andDocument {
                responseFields<TemplateNotFound>(
                    fieldWithPath("template_id").description("The id of the template.").type<ThingId>(),
                    *exceptionResponseFields(type).toTypedArray(),
                )
            }
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
            .andDocument {
                responseFields<TemplateAlreadyExistsForClass>(
                    fieldWithPath("template_id").description("The id of the template.").type<ThingId>(),
                    fieldWithPath("template_target_class_id").description("The id of target class of the template.").type<ThingId>(),
                    *exceptionResponseFields(type).toTypedArray(),
                )
            }
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
            .andDocument {
                responseFields<InvalidMinCount>(
                    fieldWithPath("min_count").description("The provided min count.").type<Int>(),
                    *exceptionResponseFields(type).toTypedArray(),
                )
            }
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
            .andDocument {
                responseFields<InvalidMaxCount>(
                    fieldWithPath("max_count").description("The provided max count.").type<Int>(),
                    *exceptionResponseFields(type).toTypedArray(),
                )
            }
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
            .andDocument {
                responseFields<InvalidCardinality>(
                    fieldWithPath("min_cardinality").description("The provided min cardinality.").type<Int>(),
                    fieldWithPath("max_cardinality").description("The provided max cardinality.").type<Int>(),
                    *exceptionResponseFields(type).toTypedArray(),
                )
            }
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
            .andDocument {
                responseFields<InvalidBounds>(
                    fieldWithPath("min_count").description("The provided min count.").type<Int>(),
                    fieldWithPath("max_count").description("The provided max count.").type<Int>(),
                    *exceptionResponseFields(type).toTypedArray(),
                )
            }
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
            .andDocument {
                responseFields<InvalidDataType>(
                    fieldWithPath("actual_data_type").description("The provided data type.").type<ThingId>(),
                    fieldWithPath("expected_data_types[]").description("A list of expected data types.").arrayItemsType("string").constraints(thingIdConstraint),
                    *exceptionResponseFields(type).toTypedArray(),
                )
            }
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
            .andDocument {
                responseFields<InvalidRegexPattern>(
                    fieldWithPath("regex_pattern").description("The provided regex pattern."),
                    *exceptionResponseFields(type).toTypedArray(),
                )
            }
    }

    @Test
    fun duplicateTemplatePropertyPaths() {
        val type = "orkg:problem:duplicate_template_property_paths"
        documentedGetRequestTo(DuplicateTemplatePropertyPaths(mapOf(ThingId("P12") to 5)))
            .andExpectErrorStatus(BAD_REQUEST)
            .andExpectType(type)
            .andExpectTitle("Bad Request")
            .andExpectDetail("""Duplicate template property paths: P12=5.""")
            .andExpect(jsonPath("$.duplicate_template_property_paths['P12']").value("5"))
            .andDocument {
                responseFields<DuplicateTemplatePropertyPaths>(
                    fieldWithPath("duplicate_template_property_paths").description("A key-value map of template property path ids to their occurrence count."),
                    fieldWithPath("duplicate_template_property_paths.*").description("The occurrence count of the template property path.").type<Int>(),
                    *exceptionResponseFields(type).toTypedArray(),
                )
            }
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
            .andDocument {
                responseFields<TemplateClosed>(
                    fieldWithPath("template_id").description("The id of the template.").type<ThingId>(),
                    *exceptionResponseFields(type).toTypedArray(),
                )
            }
    }
}
