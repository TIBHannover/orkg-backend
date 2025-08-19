package org.orkg.contenttypes.adapter.input.rest.exceptions

import org.junit.jupiter.api.Test
import org.orkg.common.ThingId
import org.orkg.contenttypes.domain.AmbiguousAuthor
import org.orkg.contenttypes.domain.Author
import org.orkg.contenttypes.domain.AuthorNotFound
import org.orkg.testing.configuration.FixedClockConfig
import org.orkg.testing.spring.MockMvcExceptionBaseTest
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest
import org.springframework.http.HttpStatus.BAD_REQUEST
import org.springframework.http.HttpStatus.NOT_FOUND
import org.springframework.test.context.ContextConfiguration

@WebMvcTest
@ContextConfiguration(classes = [FixedClockConfig::class])
internal class AuthorExceptionUnitTest : MockMvcExceptionBaseTest() {
    @Test
    fun authorNotFound() {
        documentedGetRequestTo(AuthorNotFound(ThingId("R123")))
            .andExpectErrorStatus(NOT_FOUND)
            .andExpectType("orkg:problem:author_not_found")
            .andExpectTitle("Not Found")
            .andExpectDetail("""Author "R123" not found.""")
            .andDocumentWithDefaultExceptionResponseFields()
    }

    @Test
    fun ambiguousAuthor() {
        val author = Author(
            id = ThingId("147"),
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
            .andDocumentWithDefaultExceptionResponseFields()
    }
}
