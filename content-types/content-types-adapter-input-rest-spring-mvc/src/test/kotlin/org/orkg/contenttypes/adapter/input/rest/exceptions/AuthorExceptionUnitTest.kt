package org.orkg.contenttypes.adapter.input.rest.exceptions

import org.junit.jupiter.api.Test
import org.orkg.common.ThingId
import org.orkg.contenttypes.domain.AmbiguousAuthor
import org.orkg.contenttypes.domain.Author
import org.orkg.contenttypes.domain.AuthorNotFound
import org.orkg.contenttypes.input.testing.fixtures.authorIdentifierFields
import org.orkg.contenttypes.input.testing.fixtures.configuration.ContentTypeControllerExceptionUnitTestConfiguration
import org.orkg.testing.spring.MockMvcExceptionBaseTest
import org.orkg.testing.spring.restdocs.exceptionResponseFields
import org.orkg.testing.spring.restdocs.type
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest
import org.springframework.http.HttpStatus.BAD_REQUEST
import org.springframework.http.HttpStatus.NOT_FOUND
import org.springframework.restdocs.payload.PayloadDocumentation.fieldWithPath
import org.springframework.test.context.ContextConfiguration
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath

@WebMvcTest
@ContextConfiguration(classes = [ContentTypeControllerExceptionUnitTestConfiguration::class])
internal class AuthorExceptionUnitTest : MockMvcExceptionBaseTest() {
    @Test
    fun authorNotFound() {
        val type = "orkg:problem:author_not_found"
        documentedGetRequestTo(AuthorNotFound(ThingId("R123")))
            .andExpectErrorStatus(NOT_FOUND)
            .andExpectType(type)
            .andExpectTitle("Not Found")
            .andExpectDetail("""Author "R123" not found.""")
            .andExpect(jsonPath("$.author_id").value("R123"))
            .andDocument {
                responseFields<AuthorNotFound>(
                    fieldWithPath("author_id").description("The id of the author.").type<ThingId>(),
                    *exceptionResponseFields(type).toTypedArray(),
                )
            }
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
        val type = "orkg:problem:ambiguous_author"
        documentedGetRequestTo(AmbiguousAuthor(author))
            .andExpectErrorStatus(BAD_REQUEST)
            .andExpectType(type)
            .andExpectTitle("Bad Request")
            .andExpectDetail("""Ambiguous author definition with identifiers {orcid=[0000-0002-1825-0097]}.""")
            .andExpect(jsonPath("$.author_id").value("R147"))
            .andExpect(jsonPath("$.author_identifiers.orcid[0]").value("0000-0002-1825-0097"))
            .andDocument {
                responseFields<AmbiguousAuthor>(
                    fieldWithPath("author_id").description("The id of the author. (optional)").type<ThingId>().optional(),
                    *authorIdentifierFields("author_identifiers").toTypedArray(),
                    *exceptionResponseFields(type).toTypedArray(),
                )
            }
    }
}
