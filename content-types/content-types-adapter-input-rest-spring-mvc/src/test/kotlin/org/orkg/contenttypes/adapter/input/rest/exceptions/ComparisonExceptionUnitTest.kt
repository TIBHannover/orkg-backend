package org.orkg.contenttypes.adapter.input.rest.exceptions

import org.junit.jupiter.api.Test
import org.orkg.common.ThingId
import org.orkg.common.json.CommonJacksonModule
import org.orkg.contenttypes.domain.ComparisonAlreadyPublished
import org.orkg.contenttypes.domain.ComparisonNotFound
import org.orkg.contenttypes.domain.ComparisonNotModifiable
import org.orkg.contenttypes.domain.ComparisonRelatedFigureNotFound
import org.orkg.contenttypes.domain.ComparisonRelatedFigureNotModifiable
import org.orkg.contenttypes.domain.ComparisonRelatedResourceNotFound
import org.orkg.contenttypes.domain.ComparisonRelatedResourceNotModifiable
import org.orkg.testing.configuration.FixedClockConfig
import org.orkg.testing.spring.MockMvcExceptionBaseTest
import org.orkg.testing.spring.restdocs.exceptionResponseFields
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest
import org.springframework.http.HttpStatus
import org.springframework.http.HttpStatus.FORBIDDEN
import org.springframework.restdocs.payload.PayloadDocumentation.fieldWithPath
import org.springframework.test.context.ContextConfiguration
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath

@WebMvcTest
@ContextConfiguration(classes = [CommonJacksonModule::class, FixedClockConfig::class])
internal class ComparisonExceptionUnitTest : MockMvcExceptionBaseTest() {
    @Test
    fun comparisonAlreadyPublished() {
        val type = "orkg:problem:comparison_already_published"
        documentedGetRequestTo(ComparisonAlreadyPublished(ThingId("R123")))
            .andExpectErrorStatus(FORBIDDEN)
            .andExpectType(type)
            .andExpectTitle("Forbidden")
            .andExpectDetail("""Comparison "R123" is already published.""")
            .andExpect(jsonPath("$.comparison_id").value("R123"))
            .andDocument {
                responseFields<ComparisonAlreadyPublished>(
                    fieldWithPath("comparison_id").description("The id of the comparison."),
                    *exceptionResponseFields(type).toTypedArray(),
                )
            }
    }

    @Test
    fun comparisonNotModifiable() {
        val type = "orkg:problem:comparison_not_modifiable"
        documentedGetRequestTo(ComparisonNotModifiable(ThingId("R123")))
            .andExpectErrorStatus(FORBIDDEN)
            .andExpectType(type)
            .andExpectTitle("Forbidden")
            .andExpectDetail("""Comparison "R123" is not modifiable.""")
            .andExpect(jsonPath("$.comparison_id").value("R123"))
            .andDocument {
                responseFields<ComparisonNotModifiable>(
                    fieldWithPath("comparison_id").description("The id of the comparison."),
                    *exceptionResponseFields(type).toTypedArray(),
                )
            }
    }

    @Test
    fun comparisonRelatedResourceNotModifiable() {
        val type = "orkg:problem:comparison_related_resource_not_modifiable"
        documentedGetRequestTo(ComparisonRelatedResourceNotModifiable(ThingId("R123")))
            .andExpectErrorStatus(FORBIDDEN)
            .andExpectType(type)
            .andExpectTitle("Forbidden")
            .andExpectDetail("""Comparison related resource "R123" is not modifiable.""")
            .andExpect(jsonPath("$.comparison_related_resource_id").value("R123"))
            .andDocument {
                responseFields<ComparisonRelatedResourceNotModifiable>(
                    fieldWithPath("comparison_related_resource_id").description("The id of the comparison related resurce."),
                    *exceptionResponseFields(type).toTypedArray(),
                )
            }
    }

    @Test
    fun comparisonRelatedFigureNotModifiable() {
        val type = "orkg:problem:comparison_related_figure_not_modifiable"
        documentedGetRequestTo(ComparisonRelatedFigureNotModifiable(ThingId("R123")))
            .andExpectErrorStatus(FORBIDDEN)
            .andExpectType(type)
            .andExpectTitle("Forbidden")
            .andExpectDetail("""Comparison related figure "R123" is not modifiable.""")
            .andExpect(jsonPath("$.comparison_related_figure_id").value("R123"))
            .andDocument {
                responseFields<ComparisonRelatedFigureNotModifiable>(
                    fieldWithPath("comparison_related_figure_id").description("The id of the comparison related figure."),
                    *exceptionResponseFields(type).toTypedArray(),
                )
            }
    }

    @Test
    fun comparisonNotFound() {
        val type = "orkg:problem:comparison_not_found"
        documentedGetRequestTo(ComparisonNotFound(ThingId("R123")))
            .andExpectErrorStatus(HttpStatus.NOT_FOUND)
            .andExpectType(type)
            .andExpectTitle("Not Found")
            .andExpectDetail("""Comparison "R123" not found.""")
            .andExpect(jsonPath("$.comparison_id").value("R123"))
            .andDocument {
                responseFields<ComparisonNotFound>(
                    fieldWithPath("comparison_id").description("The id of the comparison."),
                    *exceptionResponseFields(type).toTypedArray(),
                )
            }
    }

    @Test
    fun comparisonRelatedResourceNotFound() {
        val type = "orkg:problem:comparison_related_resource_not_found"
        documentedGetRequestTo(ComparisonRelatedResourceNotFound(ThingId("R123")))
            .andExpectErrorStatus(HttpStatus.NOT_FOUND)
            .andExpectType(type)
            .andExpectTitle("Not Found")
            .andExpectDetail("""Comparison related resource "R123" not found.""")
            .andExpect(jsonPath("$.comparison_related_resource_id").value("R123"))
            .andDocument {
                responseFields<ComparisonRelatedResourceNotFound>(
                    fieldWithPath("comparison_related_resource_id").description("The id of the comparison related resurce."),
                    *exceptionResponseFields(type).toTypedArray(),
                )
            }
    }

    @Test
    fun comparisonRelatedFigureNotFound() {
        val type = "orkg:problem:comparison_related_figure_not_found"
        documentedGetRequestTo(ComparisonRelatedFigureNotFound(ThingId("R123")))
            .andExpectErrorStatus(HttpStatus.NOT_FOUND)
            .andExpectType(type)
            .andExpectTitle("Not Found")
            .andExpectDetail("""Comparison related figure "R123" not found.""")
            .andExpect(jsonPath("$.comparison_related_figure_id").value("R123"))
            .andDocument {
                responseFields<ComparisonRelatedFigureNotFound>(
                    fieldWithPath("comparison_related_figure_id").description("The id of the comparison related figure."),
                    *exceptionResponseFields(type).toTypedArray(),
                )
            }
    }
}
