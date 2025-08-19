package org.orkg.contenttypes.adapter.input.rest.exceptions

import org.junit.jupiter.api.Test
import org.orkg.common.ThingId
import org.orkg.common.json.CommonJacksonModule
import org.orkg.contenttypes.domain.AmbiguousAuthor
import org.orkg.contenttypes.domain.Author
import org.orkg.contenttypes.domain.AuthorNotFound
import org.orkg.testing.configuration.FixedClockConfig
import org.orkg.testing.spring.MockMvcExceptionBaseTest
import org.orkg.testing.spring.restdocs.exceptionResponseFields
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest
import org.springframework.http.HttpStatus.BAD_REQUEST
import org.springframework.http.HttpStatus.NOT_FOUND
import org.springframework.restdocs.payload.PayloadDocumentation.fieldWithPath
import org.springframework.restdocs.payload.PayloadDocumentation.responseFields
import org.springframework.restdocs.payload.PayloadDocumentation.subsectionWithPath
import org.springframework.test.context.ContextConfiguration
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath

@WebMvcTest
@ContextConfiguration(classes = [CommonJacksonModule::class, FixedClockConfig::class])
internal class AuthorExceptionUnitTest : MockMvcExceptionBaseTest() {
    @Test
    fun authorNotFound() {
        documentedGetRequestTo(AuthorNotFound(ThingId("R123")))
            .andExpectErrorStatus(NOT_FOUND)
            .andExpectType("orkg:problem:author_not_found")
            .andExpectTitle("Not Found")
            .andExpectDetail("""Author "R123" not found.""")
            .andExpect(jsonPath("$.author_id").value("R123"))
            .andDo(
                documentationHandler.document(
                    responseFields(exceptionResponseFields()).and(
                        fieldWithPath("author_id").description("The id of the author."),
                    )
                )
            )
    }

    @Test
    fun ambiguousAuthor() {
        val author = Author(
            id = ThingId("R147"),
            name = "Josiah Stinkney Carberry",
            identifiers = mapOf(
                "orcid" to listOf("0000-0002-1825-0097")
            )
        )
        documentedGetRequestTo(AmbiguousAuthor(author))
            .andExpectErrorStatus(BAD_REQUEST)
            .andExpectType("orkg:problem:ambiguous_author")
            .andExpectTitle("Bad Request")
            .andExpectDetail("""Ambiguous author definition with identifiers {orcid=[0000-0002-1825-0097]}.""")
            .andExpect(jsonPath("$.author_id").value("R147"))
            .andExpect(jsonPath("$.author_identifiers.orcid[0]").value("0000-0002-1825-0097"))
            .andDo(
                documentationHandler.document(
                    responseFields(exceptionResponseFields()).and(
                        fieldWithPath("author_id").type("String").description("The id of the author. (optional)").optional(),
                        subsectionWithPath("author_identifiers").description("A map of associated author identifiers."),
                    )
                )
            )
    }
}
