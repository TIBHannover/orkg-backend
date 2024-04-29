package org.orkg.export.domain

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.orkg.common.ThingId
import org.orkg.export.testing.fixtures.asString
import org.orkg.graph.domain.Classes
import org.orkg.graph.testing.fixtures.createResource

@Suppress("HttpUrlsUsage")
internal class ResourceRDFTest {
    @Test
    fun `converts to NTriple format correctly`() {
        val expectedOutput = """
            |<http://orkg.org/orkg/resource/R100> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://orkg.org/orkg/class/Resource> .
            |<http://orkg.org/orkg/resource/R100> <http://www.w3.org/2000/01/rdf-schema#label> "some dummy label"^^<http://www.w3.org/2001/XMLSchema#string> .
            |
        """.trimMargin()
        val resource = createResource(ThingId("R100"), label = "some dummy label")
        assertThat(resource::toNTriple.asString()).isEqualTo(expectedOutput)
    }

    @Test
    fun `converts list resources to NTriple format correctly`() {
        val expectedOutput = """
            |<http://orkg.org/orkg/resource/R100> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://www.w3.org/1999/02/22-rdf-syntax-ns#Seq> .
            |<http://orkg.org/orkg/resource/R100> <http://www.w3.org/2000/01/rdf-schema#label> "some dummy label"^^<http://www.w3.org/2001/XMLSchema#string> .
            |
        """.trimMargin()
        val resource = createResource(ThingId("R100"), label = "some dummy label", classes = setOf(Classes.list))
        assertThat(resource::toNTriple.asString()).isEqualTo(expectedOutput)
    }
}
