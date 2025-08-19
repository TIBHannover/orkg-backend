package org.orkg.contenttypes.adapter.input.rest.exceptions

import org.junit.jupiter.api.Test
import org.orkg.common.ThingId
import org.orkg.common.json.CommonJacksonModule
import org.orkg.contenttypes.domain.InvalidSmartReviewTextSectionType
import org.orkg.contenttypes.domain.OntologyEntityNotFound
import org.orkg.contenttypes.domain.PublishedSmartReviewContentNotFound
import org.orkg.contenttypes.domain.SmartReviewAlreadyPublished
import org.orkg.contenttypes.domain.SmartReviewNotFound
import org.orkg.contenttypes.domain.SmartReviewNotModifiable
import org.orkg.contenttypes.domain.SmartReviewSectionTypeMismatch
import org.orkg.contenttypes.domain.UnrelatedSmartReviewSection
import org.orkg.graph.domain.Classes
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
internal class SmartReviewExceptionUnitTest : MockMvcExceptionBaseTest() {
    @Test
    fun publishedSmartReviewContentNotFound() {
        documentedGetRequestTo(PublishedSmartReviewContentNotFound(ThingId("R123"), ThingId("R456")))
            .andExpectErrorStatus(NOT_FOUND)
            .andExpectType("orkg:problem:published_smart_review_content_not_found")
            .andExpectTitle("Not Found")
            .andExpectDetail("""Smart review content "R456" not found for smart review "R123".""")
            .andExpect(jsonPath("$.smart_review_id").value("R123"))
            .andExpect(jsonPath("$.smart_review_content_id").value("R456"))
            .andDo(
                documentationHandler.document(
                    responseFields(exceptionResponseFields()).and(
                        fieldWithPath("smart_review_id").description("The id of the smart review."),
                        fieldWithPath("smart_review_content_id").description("The id of the requested content."),
                    )
                )
            )
    }

    @Test
    fun ontologyEntityNotFound() {
        documentedGetRequestTo(OntologyEntityNotFound(setOf(ThingId("not"), ThingId("found"))))
            .andExpectErrorStatus(NOT_FOUND)
            .andExpectType("orkg:problem:ontology_entity_not_found")
            .andExpectTitle("Not Found")
            .andExpectDetail("""Ontology entity not found among entities "not", "found".""")
            .andExpect(jsonPath("$.ontology_entities.length()").value(2))
            .andExpect(jsonPath("$.ontology_entities[0]").value("not"))
            .andExpect(jsonPath("$.ontology_entities[1]").value("found"))
            .andDo(
                documentationHandler.document(
                    responseFields(exceptionResponseFields()).and(
                        fieldWithPath("ontology_entities").description("The list of provided ontology entities."),
                    )
                )
            )
    }

    @Test
    fun invalidSmartReviewTextSectionType() {
        documentedGetRequestTo(InvalidSmartReviewTextSectionType(ThingId("comparison")))
            .andExpectErrorStatus(BAD_REQUEST)
            .andExpectType("orkg:problem:invalid_smart_review_text_section_type")
            .andExpectTitle("Bad Request")
            .andExpectDetail("""Invalid smart review text section type "comparison".""")
            .andExpect(jsonPath("$.smart_review_section_type").value("comparison"))
            .andDo(
                documentationHandler.document(
                    responseFields(exceptionResponseFields()).and(
                        fieldWithPath("smart_review_section_type").description("The provided smart review section type."),
                    )
                )
            )
    }

    @Test
    fun unrelatedSmartReviewSection() {
        documentedGetRequestTo(UnrelatedSmartReviewSection(ThingId("R123"), ThingId("R456")))
            .andExpectErrorStatus(BAD_REQUEST)
            .andExpectType("orkg:problem:unrelated_smart_review_section")
            .andExpectTitle("Bad Request")
            .andExpectDetail("""Smart review section "R456" does not belong to smart review "R123".""")
            .andExpect(jsonPath("$.smart_review_id").value("R123"))
            .andExpect(jsonPath("$.smart_review_section_id").value("R456"))
            .andDo(
                documentationHandler.document(
                    responseFields(exceptionResponseFields()).and(
                        fieldWithPath("smart_review_id").description("The id of the smart review."),
                        fieldWithPath("smart_review_section_id").description("The id of the smart review section."),
                    )
                )
            )
    }

    @Test
    fun smartReviewSectionTypeMismatch_mustBeComparisonSection() {
        documentedGetRequestTo(SmartReviewSectionTypeMismatch.mustBeComparisonSection())
            .andExpectErrorStatus(BAD_REQUEST)
            .andExpectType("orkg:problem:smart_review_section_type_mismatch")
            .andExpectTitle("Bad Request")
            .andExpectDetail("""Invalid smart review section type. Must be a comparison section.""")
            .andExpect(jsonPath("$.expected_smart_review_section_type").value(Classes.comparisonSection.value))
            .andDo(
                documentationHandler.document(
                    responseFields(exceptionResponseFields()).and(
                        fieldWithPath("expected_smart_review_section_type").description("The expected type of the smart review section."),
                    )
                )
            )
    }

    @Test
    fun smartReviewSectionTypeMismatch_mustBeVisualizationSection() {
        get(SmartReviewSectionTypeMismatch.mustBeVisualizationSection())
            .andExpectErrorStatus(BAD_REQUEST)
            .andExpectType("orkg:problem:smart_review_section_type_mismatch")
            .andExpectTitle("Bad Request")
            .andExpectDetail("""Invalid smart review section type. Must be a visualization section.""")
            .andExpect(jsonPath("$.expected_smart_review_section_type").value(Classes.visualizationSection.value))
    }

    @Test
    fun smartReviewSectionTypeMismatch_mustBeResourceSection() {
        get(SmartReviewSectionTypeMismatch.mustBeResourceSection())
            .andExpectErrorStatus(BAD_REQUEST)
            .andExpectType("orkg:problem:smart_review_section_type_mismatch")
            .andExpectTitle("Bad Request")
            .andExpectDetail("""Invalid smart review section type. Must be a resource section.""")
            .andExpect(jsonPath("$.expected_smart_review_section_type").value(Classes.resourceSection.value))
    }

    @Test
    fun smartReviewSectionTypeMismatch_mustBePredicateSection() {
        get(SmartReviewSectionTypeMismatch.mustBePredicateSection())
            .andExpectErrorStatus(BAD_REQUEST)
            .andExpectType("orkg:problem:smart_review_section_type_mismatch")
            .andExpectTitle("Bad Request")
            .andExpectDetail("""Invalid smart review section type. Must be a predicate section.""")
            .andExpect(jsonPath("$.expected_smart_review_section_type").value(Classes.propertySection.value))
    }

    @Test
    fun smartReviewSectionTypeMismatch_mustBeOntologySection() {
        get(SmartReviewSectionTypeMismatch.mustBeOntologySection())
            .andExpectErrorStatus(BAD_REQUEST)
            .andExpectType("orkg:problem:smart_review_section_type_mismatch")
            .andExpectTitle("Bad Request")
            .andExpectDetail("""Invalid smart review section type. Must be an ontology section.""")
            .andExpect(jsonPath("$.expected_smart_review_section_type").value(Classes.ontologySection.value))
    }

    @Test
    fun smartReviewSectionTypeMismatch_mustBeTextSection() {
        get(SmartReviewSectionTypeMismatch.mustBeTextSection())
            .andExpectErrorStatus(BAD_REQUEST)
            .andExpectType("orkg:problem:smart_review_section_type_mismatch")
            .andExpectTitle("Bad Request")
            .andExpectDetail("""Invalid smart review section type. Must be a text section.""")
            .andExpect(jsonPath("$.expected_smart_review_section_type").value(Classes.section.value))
    }

    @Test
    fun smartReviewAlreadyPublished() {
        documentedGetRequestTo(SmartReviewAlreadyPublished(ThingId("R123")))
            .andExpectErrorStatus(FORBIDDEN)
            .andExpectType("orkg:problem:smart_review_already_published")
            .andExpectTitle("Forbidden")
            .andExpectDetail("""Smart review "R123" is already published.""")
            .andExpect(jsonPath("$.smart_review_id").value("R123"))
            .andDo(
                documentationHandler.document(
                    responseFields(exceptionResponseFields()).and(
                        fieldWithPath("smart_review_id").description("The id of the smart review."),
                    )
                )
            )
    }

    @Test
    fun smartReviewNotModifiable() {
        documentedGetRequestTo(SmartReviewNotModifiable(ThingId("R123")))
            .andExpectErrorStatus(FORBIDDEN)
            .andExpectType("orkg:problem:smart_review_not_modifiable")
            .andExpectTitle("Forbidden")
            .andExpectDetail("""Smart review "R123" is not modifiable.""")
            .andExpect(jsonPath("$.smart_review_id").value("R123"))
            .andDo(
                documentationHandler.document(
                    responseFields(exceptionResponseFields()).and(
                        fieldWithPath("smart_review_id").description("The id of the smart review."),
                    )
                )
            )
    }

    @Test
    fun smartReviewNotFound() {
        documentedGetRequestTo(SmartReviewNotFound(ThingId("R123")))
            .andExpectErrorStatus(NOT_FOUND)
            .andExpectType("orkg:problem:smart_review_not_found")
            .andExpectTitle("Not Found")
            .andExpectDetail("""Smart review "R123" not found.""")
            .andExpect(jsonPath("$.smart_review_id").value("R123"))
            .andDo(
                documentationHandler.document(
                    responseFields(exceptionResponseFields()).and(
                        fieldWithPath("smart_review_id").description("The id of the smart review."),
                    )
                )
            )
    }
}
