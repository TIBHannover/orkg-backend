package org.orkg.contenttypes.adapter.input.rest.exceptions

import org.junit.jupiter.api.Test
import org.orkg.common.ThingId
import org.orkg.common.json.CommonJacksonModule
import org.orkg.contenttypes.domain.InvalidSmartReviewTextSectionType
import org.orkg.contenttypes.domain.OntologyEntityNotFound
import org.orkg.contenttypes.domain.PublishedSmartReviewContentNotFound
import org.orkg.contenttypes.domain.SmartReviewAlreadyPublished
import org.orkg.contenttypes.domain.SmartReviewNotModifiable
import org.orkg.contenttypes.domain.SmartReviewSectionTypeMismatch
import org.orkg.contenttypes.domain.UnrelatedSmartReviewSection
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
            .andDocumentWithDefaultExceptionResponseFields()
    }

    @Test
    fun ontologyEntityNotFound() {
        documentedGetRequestTo(OntologyEntityNotFound(setOf(ThingId("not"), ThingId("found"))))
            .andExpectErrorStatus(NOT_FOUND)
            .andExpectType("orkg:problem:ontology_entity_not_found")
            .andExpectTitle("Not Found")
            .andExpectDetail("""Ontology entity not found among entities "not", "found".""")
            .andDocumentWithDefaultExceptionResponseFields()
    }

    @Test
    fun invalidSmartReviewTextSectionType() {
        documentedGetRequestTo(InvalidSmartReviewTextSectionType(ThingId("comparison")))
            .andExpectErrorStatus(BAD_REQUEST)
            .andExpectType("orkg:problem:invalid_smart_review_text_section_type")
            .andExpectTitle("Bad Request")
            .andExpectDetail("""Invalid smart review text section type "comparison".""")
            .andDocumentWithDefaultExceptionResponseFields()
    }

    @Test
    fun unrelatedSmartReviewSection() {
        documentedGetRequestTo(UnrelatedSmartReviewSection(ThingId("R123"), ThingId("R456")))
            .andExpectErrorStatus(BAD_REQUEST)
            .andExpectType("orkg:problem:unrelated_smart_review_section")
            .andExpectTitle("Bad Request")
            .andExpectDetail("""Smart review section "R456" does not belong to smart review "R123".""")
            .andDocumentWithDefaultExceptionResponseFields()
    }

    @Test
    fun smartReviewSectionTypeMismatchMustBeComparisonSection() {
        documentedGetRequestTo(SmartReviewSectionTypeMismatch.mustBeComparisonSection())
            .andExpectErrorStatus(BAD_REQUEST)
            .andExpectType("orkg:problem:smart_review_section_type_mismatch")
            .andExpectTitle("Bad Request")
            .andExpectDetail("""Invalid smart review section type. Must be a comparison section.""")
            .andDo(
                documentationHandler.document(
                    responseFields(exceptionResponseFields()).and(
                        fieldWithPath("expected_type").description("The expected type of the smart review section."),
                    )
                )
            )
    }

    @Test
    fun smartReviewSectionTypeMismatchMustBeVisualizationSection() {
        get(SmartReviewSectionTypeMismatch.mustBeVisualizationSection())
            .andExpectErrorStatus(BAD_REQUEST)
            .andExpectType("orkg:problem:smart_review_section_type_mismatch")
            .andExpectTitle("Bad Request")
            .andExpectDetail("""Invalid smart review section type. Must be a visualization section.""")
    }

    @Test
    fun smartReviewSectionTypeMismatchMustBeResourceSection() {
        get(SmartReviewSectionTypeMismatch.mustBeResourceSection())
            .andExpectErrorStatus(BAD_REQUEST)
            .andExpectType("orkg:problem:smart_review_section_type_mismatch")
            .andExpectTitle("Bad Request")
            .andExpectDetail("""Invalid smart review section type. Must be a resource section.""")
    }

    @Test
    fun smartReviewSectionTypeMismatchMustBePredicateSection() {
        get(SmartReviewSectionTypeMismatch.mustBePredicateSection())
            .andExpectErrorStatus(BAD_REQUEST)
            .andExpectType("orkg:problem:smart_review_section_type_mismatch")
            .andExpectTitle("Bad Request")
            .andExpectDetail("""Invalid smart review section type. Must be a predicate section.""")
    }

    @Test
    fun smartReviewSectionTypeMismatchMustBeOntologySection() {
        get(SmartReviewSectionTypeMismatch.mustBeOntologySection())
            .andExpectErrorStatus(BAD_REQUEST)
            .andExpectType("orkg:problem:smart_review_section_type_mismatch")
            .andExpectTitle("Bad Request")
            .andExpectDetail("""Invalid smart review section type. Must be an ontology section.""")
    }

    @Test
    fun smartReviewSectionTypeMismatchMustBeTextSection() {
        get(SmartReviewSectionTypeMismatch.mustBeTextSection())
            .andExpectErrorStatus(BAD_REQUEST)
            .andExpectType("orkg:problem:smart_review_section_type_mismatch")
            .andExpectTitle("Bad Request")
            .andExpectDetail("""Invalid smart review section type. Must be a text section.""")
    }

    @Test
    fun smartReviewAlreadyPublished() {
        documentedGetRequestTo(SmartReviewAlreadyPublished(ThingId("R123")))
            .andExpectErrorStatus(FORBIDDEN)
            .andExpectType("orkg:problem:smart_review_already_published")
            .andExpectTitle("Forbidden")
            .andExpectDetail("""Smart review "R123" is already published.""")
            .andDocumentWithDefaultExceptionResponseFields()
    }

    @Test
    fun smartReviewNotModifiable() {
        documentedGetRequestTo(SmartReviewNotModifiable(ThingId("R123")))
            .andExpectErrorStatus(FORBIDDEN)
            .andExpectType("orkg:problem:smart_review_not_modifiable")
            .andExpectTitle("Forbidden")
            .andExpectDetail("""Smart review "R123" is not modifiable.""")
            .andDocumentWithDefaultExceptionResponseFields()
    }
}
