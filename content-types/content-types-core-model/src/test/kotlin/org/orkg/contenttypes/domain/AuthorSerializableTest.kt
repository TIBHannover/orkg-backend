package org.orkg.contenttypes.domain

import org.eclipse.rdf4j.common.net.ParsedIRI
import org.junit.jupiter.api.Test
import org.orkg.common.ThingId
import org.orkg.common.testSerialization

internal class AuthorSerializableTest {
    @Test
    fun `Given an Author instance, when serializing to byte array and deserializing back to Author, it successfully reconstructs the instance`() {
        val author = Author(
            id = ThingId("147"),
            name = "Josiah Stinkney Carberry",
            identifiers = mapOf(
                "orcid" to listOf("0000-0002-1825-0097")
            ),
            homepage = ParsedIRI("https://example.org")
        )
        testSerialization(author)
    }
}
