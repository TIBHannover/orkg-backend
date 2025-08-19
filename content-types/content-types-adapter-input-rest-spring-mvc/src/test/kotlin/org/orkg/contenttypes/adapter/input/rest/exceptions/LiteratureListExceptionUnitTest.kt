package org.orkg.contenttypes.adapter.input.rest.exceptions

import org.junit.jupiter.api.Test
import org.orkg.common.ThingId
import org.orkg.contenttypes.domain.InvalidHeadingSize
import org.orkg.contenttypes.domain.InvalidListSectionEntry
import org.orkg.contenttypes.domain.LiteratureListAlreadyPublished
import org.orkg.contenttypes.domain.LiteratureListNotFound
import org.orkg.contenttypes.domain.LiteratureListNotModifiable
import org.orkg.contenttypes.domain.LiteratureListSectionTypeMismatch
import org.orkg.contenttypes.domain.PublishedLiteratureListContentNotFound
import org.orkg.contenttypes.domain.UnrelatedLiteratureListSection
import org.orkg.testing.configuration.FixedClockConfig
import org.orkg.testing.spring.MockMvcExceptionBaseTest
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest
import org.springframework.http.HttpStatus.BAD_REQUEST
import org.springframework.http.HttpStatus.FORBIDDEN
import org.springframework.http.HttpStatus.NOT_FOUND
import org.springframework.test.context.ContextConfiguration

@WebMvcTest
@ContextConfiguration(classes = [FixedClockConfig::class])
internal class LiteratureListExceptionUnitTest : MockMvcExceptionBaseTest() {
    @Test
    fun publishedLiteratureListContentNotFound() {
        documentedGetRequestTo(PublishedLiteratureListContentNotFound(ThingId("R123"), ThingId("R456")))
            .andExpectErrorStatus(NOT_FOUND)
            .andExpectType("orkg:problem:published_literature_list_content_not_found")
            .andExpectTitle("Not Found")
            .andExpectDetail("""Literature list content "R456" not found for literature list "R123".""")
            .andDocumentWithDefaultExceptionResponseFields()
    }

    @Test
    fun literatureListAlreadyPublished() {
        documentedGetRequestTo(LiteratureListAlreadyPublished(ThingId("R123")))
            .andExpectErrorStatus(FORBIDDEN)
            .andExpectType("orkg:problem:literature_list_already_published")
            .andExpectTitle("Forbidden")
            .andExpectDetail("""Literature list "R123" is already published.""")
            .andDocumentWithDefaultExceptionResponseFields()
    }

    @Test
    fun literatureListNotFound() {
        documentedGetRequestTo(LiteratureListNotFound(ThingId("R123")))
            .andExpectErrorStatus(NOT_FOUND)
            .andExpectType("orkg:problem:literature_list_not_found")
            .andExpectTitle("Not Found")
            .andExpectDetail("""Literature list "R123" not found.""")
            .andDocumentWithDefaultExceptionResponseFields()
    }

    @Test
    fun invalidListSectionEntry() {
        documentedGetRequestTo(InvalidListSectionEntry(ThingId("R123"), setOf(ThingId("C1"), ThingId("C2"))))
            .andExpectErrorStatus(BAD_REQUEST)
            .andExpectType("orkg:problem:invalid_list_section_entry")
            .andExpectTitle("Bad Request")
            .andExpectDetail("""Invalid list section entry "R123". Must be an instance of either "C1", "C2".""")
            .andDocumentWithDefaultExceptionResponseFields()
    }

    @Test
    fun invalidHeadingSize() {
        documentedGetRequestTo(InvalidHeadingSize(5))
            .andExpectErrorStatus(BAD_REQUEST)
            .andExpectType("orkg:problem:invalid_heading_size")
            .andExpectTitle("Bad Request")
            .andExpectDetail("""Invalid heading size "5". Must be at least 1.""")
            .andDocumentWithDefaultExceptionResponseFields()
    }

    @Test
    fun unrelatedLiteratureListSection() {
        documentedGetRequestTo(UnrelatedLiteratureListSection(ThingId("R123"), ThingId("R456")))
            .andExpectErrorStatus(BAD_REQUEST)
            .andExpectType("orkg:problem:unrelated_literature_list_section")
            .andExpectTitle("Bad Request")
            .andExpectDetail("""Literature list section "R456" does not belong to literature list "R123".""")
            .andDocumentWithDefaultExceptionResponseFields()
    }

    @Test
    fun literatureListSectionTypeMismatch_mustBeTextSection() {
        documentedGetRequestTo(LiteratureListSectionTypeMismatch.mustBeTextSection())
            .andExpectErrorStatus(BAD_REQUEST)
            .andExpectType("orkg:problem:literature_list_section_type_mismatch")
            .andExpectTitle("Bad Request")
            .andExpectDetail("""Invalid literature list section type. Must be a text section.""")
            .andDocumentWithDefaultExceptionResponseFields()
    }

    @Test
    fun literatureListSectionTypeMismatch_mustBeListSection() {
        // TODO: add field
        get(LiteratureListSectionTypeMismatch.mustBeListSection())
            .andExpectErrorStatus(BAD_REQUEST)
            .andExpectType("orkg:problem:literature_list_section_type_mismatch")
            .andExpectTitle("Bad Request")
            .andExpectDetail("""Invalid literature list section type. Must be a list section.""")
    }

    @Test
    fun literatureListNotModifiable() {
        documentedGetRequestTo(LiteratureListNotModifiable(ThingId("R123")))
            .andExpectErrorStatus(FORBIDDEN)
            .andExpectType("orkg:problem:literature_list_not_modifiable")
            .andExpectTitle("Forbidden")
            .andExpectDetail("""Literature list "R123" is not modifiable.""")
            .andDocumentWithDefaultExceptionResponseFields()
    }
}
