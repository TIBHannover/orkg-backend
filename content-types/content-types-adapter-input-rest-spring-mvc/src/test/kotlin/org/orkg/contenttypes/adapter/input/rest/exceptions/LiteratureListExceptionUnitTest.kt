package org.orkg.contenttypes.adapter.input.rest.exceptions

import org.junit.jupiter.api.Test
import org.orkg.common.ThingId
import org.orkg.common.thingIdConstraint
import org.orkg.contenttypes.domain.InvalidHeadingSize
import org.orkg.contenttypes.domain.InvalidListSectionEntry
import org.orkg.contenttypes.domain.LiteratureListAlreadyPublished
import org.orkg.contenttypes.domain.LiteratureListNotFound
import org.orkg.contenttypes.domain.LiteratureListNotModifiable
import org.orkg.contenttypes.domain.LiteratureListSectionTypeMismatch
import org.orkg.contenttypes.domain.PublishedLiteratureListContentNotFound
import org.orkg.contenttypes.domain.UnrelatedLiteratureListSection
import org.orkg.contenttypes.input.testing.fixtures.configuration.ContentTypeControllerExceptionUnitTestConfiguration
import org.orkg.graph.domain.Classes
import org.orkg.testing.spring.MockMvcExceptionBaseTest
import org.orkg.testing.spring.restdocs.arrayItemsType
import org.orkg.testing.spring.restdocs.constraints
import org.orkg.testing.spring.restdocs.exceptionResponseFields
import org.orkg.testing.spring.restdocs.type
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest
import org.springframework.http.HttpStatus.BAD_REQUEST
import org.springframework.http.HttpStatus.FORBIDDEN
import org.springframework.http.HttpStatus.NOT_FOUND
import org.springframework.restdocs.payload.PayloadDocumentation.fieldWithPath
import org.springframework.test.context.ContextConfiguration
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath

@WebMvcTest
@ContextConfiguration(classes = [ContentTypeControllerExceptionUnitTestConfiguration::class])
internal class LiteratureListExceptionUnitTest : MockMvcExceptionBaseTest() {
    @Test
    fun publishedLiteratureListContentNotFound() {
        val type = "orkg:problem:published_literature_list_content_not_found"
        documentedGetRequestTo(PublishedLiteratureListContentNotFound(ThingId("R123"), ThingId("R456")))
            .andExpectErrorStatus(NOT_FOUND)
            .andExpectType(type)
            .andExpectTitle("Not Found")
            .andExpectDetail("""Literature list content "R456" not found for literature list "R123".""")
            .andExpect(jsonPath("$.literature_list_id").value("R123"))
            .andExpect(jsonPath("$.literature_list_content_id").value("R456"))
            .andDocument {
                responseFields<PublishedLiteratureListContentNotFound>(
                    fieldWithPath("literature_list_id").description("The id of the literature list.").type<ThingId>(),
                    fieldWithPath("literature_list_content_id").description("The id of the requested content.").type<ThingId>(),
                    *exceptionResponseFields(type).toTypedArray(),
                )
            }
    }

    @Test
    fun literatureListAlreadyPublished() {
        val type = "orkg:problem:literature_list_already_published"
        documentedGetRequestTo(LiteratureListAlreadyPublished(ThingId("R123")))
            .andExpectErrorStatus(FORBIDDEN)
            .andExpectType(type)
            .andExpectTitle("Forbidden")
            .andExpectDetail("""Literature list "R123" is already published.""")
            .andExpect(jsonPath("$.literature_list_id").value("R123"))
            .andDocument {
                responseFields<LiteratureListAlreadyPublished>(
                    fieldWithPath("literature_list_id").description("The id of the literature list.").type<ThingId>(),
                    *exceptionResponseFields(type).toTypedArray(),
                )
            }
    }

    @Test
    fun literatureListNotFound() {
        val type = "orkg:problem:literature_list_not_found"
        documentedGetRequestTo(LiteratureListNotFound(ThingId("R123")))
            .andExpectErrorStatus(NOT_FOUND)
            .andExpectType(type)
            .andExpectTitle("Not Found")
            .andExpectDetail("""Literature list "R123" not found.""")
            .andExpect(jsonPath("$.literature_list_id").value("R123"))
            .andDocument {
                responseFields<LiteratureListNotFound>(
                    fieldWithPath("literature_list_id").description("The id of the literature list.").type<ThingId>(),
                    *exceptionResponseFields(type).toTypedArray(),
                )
            }
    }

    @Test
    fun invalidListSectionEntry() {
        val type = "orkg:problem:invalid_list_section_entry"
        documentedGetRequestTo(InvalidListSectionEntry(ThingId("R123"), setOf(ThingId("C1"), ThingId("C2"))))
            .andExpectErrorStatus(BAD_REQUEST)
            .andExpectType(type)
            .andExpectTitle("Bad Request")
            .andExpectDetail("""Invalid list section entry "R123". Must be an instance of either "C1", "C2".""")
            .andExpect(jsonPath("$.literature_list_section_id").value("R123"))
            .andDocument {
                responseFields<InvalidListSectionEntry>(
                    fieldWithPath("literature_list_section_id").description("The id of the literature list section.").type<ThingId>(),
                    fieldWithPath("expected_classes[]").description("A list of expected class ids.").arrayItemsType("string").constraints(thingIdConstraint),
                    *exceptionResponseFields(type).toTypedArray(),
                )
            }
    }

    @Test
    fun invalidHeadingSize() {
        val type = "orkg:problem:invalid_heading_size"
        documentedGetRequestTo(InvalidHeadingSize(5))
            .andExpectErrorStatus(BAD_REQUEST)
            .andExpectType(type)
            .andExpectTitle("Bad Request")
            .andExpectDetail("""Invalid heading size "5". Must be at least 1.""")
            .andExpect(jsonPath("$.heading_size").value("5"))
            .andDocument {
                responseFields<InvalidHeadingSize>(
                    fieldWithPath("heading_size").description("The provided heading size.").type<Int>(),
                    *exceptionResponseFields(type).toTypedArray(),
                )
            }
    }

    @Test
    fun unrelatedLiteratureListSection() {
        val type = "orkg:problem:unrelated_literature_list_section"
        documentedGetRequestTo(UnrelatedLiteratureListSection(ThingId("R123"), ThingId("R456")))
            .andExpectErrorStatus(BAD_REQUEST)
            .andExpectType(type)
            .andExpectTitle("Bad Request")
            .andExpectDetail("""Literature list section "R456" does not belong to literature list "R123".""")
            .andExpect(jsonPath("$.literature_list_id").value("R123"))
            .andExpect(jsonPath("$.literature_list_section_id").value("R456"))
            .andDocument {
                responseFields<UnrelatedLiteratureListSection>(
                    fieldWithPath("literature_list_id").description("The id of the literature list.").type<ThingId>(),
                    fieldWithPath("literature_list_section_id").description("The id of the literature list section.").type<ThingId>(),
                    *exceptionResponseFields(type).toTypedArray(),
                )
            }
    }

    @Test
    fun literatureListSectionTypeMismatch_mustBeTextSection() {
        val type = "orkg:problem:literature_list_section_type_mismatch"
        documentedGetRequestTo(LiteratureListSectionTypeMismatch.mustBeTextSection())
            .andExpectErrorStatus(BAD_REQUEST)
            .andExpectType(type)
            .andExpectTitle("Bad Request")
            .andExpectDetail("""Invalid literature list section type. Must be a text section.""")
            .andExpect(jsonPath("$.expected_literature_list_section_type").value(Classes.textSection.value))
            .andDocument {
                responseFields<LiteratureListSectionTypeMismatch>(
                    fieldWithPath("expected_literature_list_section_type").description("The expected type of the literature list section.").type<ThingId>(),
                    *exceptionResponseFields(type).toTypedArray(),
                )
            }
    }

    @Test
    fun literatureListSectionTypeMismatch_mustBeListSection() {
        get(LiteratureListSectionTypeMismatch.mustBeListSection())
            .andExpectErrorStatus(BAD_REQUEST)
            .andExpectType("orkg:problem:literature_list_section_type_mismatch")
            .andExpectTitle("Bad Request")
            .andExpectDetail("""Invalid literature list section type. Must be a list section.""")
            .andExpect(jsonPath("$.expected_literature_list_section_type").value(Classes.listSection.value))
    }

    @Test
    fun literatureListNotModifiable() {
        val type = "orkg:problem:literature_list_not_modifiable"
        documentedGetRequestTo(LiteratureListNotModifiable(ThingId("R123")))
            .andExpectErrorStatus(FORBIDDEN)
            .andExpectType(type)
            .andExpectTitle("Forbidden")
            .andExpectDetail("""Literature list "R123" is not modifiable.""")
            .andExpect(jsonPath("$.literature_list_id").value("R123"))
            .andDocument {
                responseFields<LiteratureListNotModifiable>(
                    fieldWithPath("literature_list_id").description("The id of the literature list.").type<ThingId>(),
                    *exceptionResponseFields(type).toTypedArray(),
                )
            }
    }
}
