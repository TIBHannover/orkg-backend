package org.orkg.contenttypes.adapter.input.rest.exceptions

import org.hamcrest.Matchers.`is`
import org.junit.jupiter.api.Test
import org.orkg.common.ThingId
import org.orkg.common.json.CommonJacksonModule
import org.orkg.contenttypes.domain.PaperAlreadyExists
import org.orkg.contenttypes.domain.PaperNotFound
import org.orkg.contenttypes.domain.PaperNotModifiable
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
internal class PaperExceptionUnitTest : MockMvcExceptionBaseTest() {
    @Test
    fun paperNotModifiable() {
        documentedGetRequestTo(PaperNotModifiable(ThingId("R123")))
            .andExpectErrorStatus(FORBIDDEN)
            .andExpectType("orkg:problem:paper_not_modifiable")
            .andExpectTitle("Forbidden")
            .andExpectDetail("""Paper "R123" is not modifiable.""")
            .andDocumentWithDefaultExceptionResponseFields()
    }

    @Test
    fun paperNotFound_withId() {
        documentedGetRequestTo(PaperNotFound.withId(ThingId("R123")))
            .andExpectErrorStatus(NOT_FOUND)
            .andExpectType("orkg:problem:paper_not_found")
            .andExpectTitle("Not Found")
            .andExpectDetail("""Paper "R123" not found.""")
            .andExpect(jsonPath("$.id").value("R123"))
            .andDo(
                documentationHandler.document(
                    responseFields(exceptionResponseFields()).and(
                        fieldWithPath("id").description("The id of the paper that could not be found. (optional, either `id`, `paper_title` or `doi` is present)"),
                        fieldWithPath("paper_title").type("String").description("The title of the paper that could not be found. (optional, either `id`, `paper_title` or `doi` is present)").optional(),
                        fieldWithPath("doi").type("String").description("The doi of the paper that could not be found. (optional, either `id`, `paper_title` or `doi` is present)").optional(),
                    )
                )
            )
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
            .andExpect(jsonPath("$.doi").value("10.123/456"))
    }

    @Test
    fun paperAlreadyExists_withTitle() {
        documentedGetRequestTo(PaperAlreadyExists.withTitle("Paper title"))
            .andExpectErrorStatus(BAD_REQUEST)
            .andExpectType("orkg:problem:paper_already_exists")
            .andExpectTitle("Bad Request")
            .andExpectDetail("""Paper with title "Paper title" already exists.""")
            .andExpect(jsonPath("$.paper_title", `is`("Paper title")))
            .andDo(
                documentationHandler.document(
                    responseFields(exceptionResponseFields()).and(
                        fieldWithPath("paper_title").description("The title of the paper that already exists. (optional, either `paper_title` or `identifier` is present)"),
                        fieldWithPath("identifier").type("String").description("The identifier of the paper that already exists. (optional, either `paper_title` or `identifier` is present)").optional(),
                    )
                )
            )
    }

    @Test
    fun paperAlreadyExists_withIdentifier() {
        get(PaperAlreadyExists.withIdentifier("10.123/456"))
            .andExpectErrorStatus(BAD_REQUEST)
            .andExpectType("orkg:problem:paper_already_exists")
            .andExpectTitle("Bad Request")
            .andExpectDetail("""Paper with identifier "10.123/456" already exists.""")
            .andExpect(jsonPath("$.identifier", `is`("10.123/456")))
    }
}
