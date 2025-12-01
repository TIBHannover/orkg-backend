package org.orkg.contenttypes.adapter.input.rest.exceptions

import org.hamcrest.Matchers.`is`
import org.junit.jupiter.api.Test
import org.orkg.common.DOI
import org.orkg.common.ThingId
import org.orkg.contenttypes.domain.PaperAlreadyExists
import org.orkg.contenttypes.domain.PaperNotFound
import org.orkg.contenttypes.domain.PaperNotModifiable
import org.orkg.contenttypes.input.testing.fixtures.configuration.ContentTypeControllerExceptionUnitTestConfiguration
import org.orkg.testing.spring.MockMvcExceptionBaseTest
import org.orkg.testing.spring.restdocs.exceptionResponseFields
import org.orkg.testing.spring.restdocs.type
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest
import org.springframework.http.HttpStatus.BAD_REQUEST
import org.springframework.http.HttpStatus.FORBIDDEN
import org.springframework.http.HttpStatus.NOT_FOUND
import org.springframework.restdocs.payload.PayloadDocumentation.fieldWithPath
import org.springframework.test.context.ContextConfiguration
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath

@WebMvcTest
@ContextConfiguration(classes = [ContentTypeControllerExceptionUnitTestConfiguration::class])
internal class PaperExceptionUnitTest : MockMvcExceptionBaseTest() {
    @Test
    fun paperNotModifiable() {
        val type = "orkg:problem:paper_not_modifiable"
        documentedGetRequestTo(PaperNotModifiable(ThingId("R123")))
            .andExpectErrorStatus(FORBIDDEN)
            .andExpectType(type)
            .andExpectTitle("Forbidden")
            .andExpectDetail("""Paper "R123" is not modifiable.""")
            .andExpect(jsonPath("$.paper_id").value("R123"))
            .andDocument {
                responseFields<PaperNotModifiable>(
                    fieldWithPath("paper_id").description("The id of the paper.").type<ThingId>(),
                    *exceptionResponseFields(type).toTypedArray(),
                )
            }
    }

    @Test
    fun paperNotFound_withId() {
        val type = "orkg:problem:paper_not_found"
        documentedGetRequestTo(PaperNotFound.withId(ThingId("R123")))
            .andExpectErrorStatus(NOT_FOUND)
            .andExpectType(type)
            .andExpectTitle("Not Found")
            .andExpectDetail("""Paper "R123" not found.""")
            .andExpect(jsonPath("$.paper_id").value("R123"))
            .andDocument {
                responseFields<PaperNotFound>(
                    fieldWithPath("paper_id").description("The id of the paper. (optional, either `paper_id`, `paper_title` or `paper_doi` is present)").type<ThingId>(),
                    fieldWithPath("paper_paper_title").type("String").description("The title of the paper. (optional, either `paper_id`, `paper_title` or `doi` is present)").optional(),
                    fieldWithPath("paper_doi").type("String").description("The doi of the paper. (optional, either `paper_id`, `paper_title` or `paper_doi` is present)").type<DOI>().optional(),
                    *exceptionResponseFields(type).toTypedArray(),
                )
            }
    }

    @Test
    fun paperNotFound_withTitle() {
        get(PaperNotFound.withTitle("Paper title"))
            .andExpectErrorStatus(NOT_FOUND)
            .andExpectType("orkg:problem:paper_not_found")
            .andExpectTitle("Not Found")
            .andExpectDetail("""Paper with title "Paper title" not found.""")
            .andExpect(jsonPath("$.paper_title").value("Paper title"))
    }

    @Test
    fun paperNotFound_withDOI() {
        get(PaperNotFound.withDOI("10.123/456"))
            .andExpectErrorStatus(NOT_FOUND)
            .andExpectType("orkg:problem:paper_not_found")
            .andExpectTitle("Not Found")
            .andExpectDetail("""Paper with DOI "10.123/456" not found.""")
            .andExpect(jsonPath("$.paper_doi").value("10.123/456"))
    }

    @Test
    fun paperAlreadyExists_withTitle() {
        val type = "orkg:problem:paper_already_exists"
        documentedGetRequestTo(PaperAlreadyExists.withTitle("Paper title"))
            .andExpectErrorStatus(BAD_REQUEST)
            .andExpectType(type)
            .andExpectTitle("Bad Request")
            .andExpectDetail("""Paper with title "Paper title" already exists.""")
            .andExpect(jsonPath("$.paper_title", `is`("Paper title")))
            .andDocument {
                responseFields<PaperAlreadyExists>(
                    fieldWithPath("paper_title").description("The title of the paper. (optional, either `paper_title` or `paper_identifier` is present)"),
                    fieldWithPath("paper_identifier").type("String").description("The identifier of the paper. (optional, either `paper_title` or `paper_identifier` is present)").optional(),
                    *exceptionResponseFields(type).toTypedArray(),
                )
            }
    }

    @Test
    fun paperAlreadyExists_withIdentifier() {
        get(PaperAlreadyExists.withIdentifier("10.123/456"))
            .andExpectErrorStatus(BAD_REQUEST)
            .andExpectType("orkg:problem:paper_already_exists")
            .andExpectTitle("Bad Request")
            .andExpectDetail("""Paper with identifier "10.123/456" already exists.""")
            .andExpect(jsonPath("$.paper_identifier", `is`("10.123/456")))
    }
}
