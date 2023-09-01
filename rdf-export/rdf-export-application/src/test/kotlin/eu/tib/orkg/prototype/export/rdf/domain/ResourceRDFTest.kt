package eu.tib.orkg.prototype.export.rdf.domain

import eu.tib.orkg.prototype.asString
import eu.tib.orkg.prototype.createResource
import eu.tib.orkg.prototype.statements.api.Classes
import eu.tib.orkg.prototype.statements.domain.model.ThingId
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

@Suppress("HttpUrlsUsage")
internal class ResourceRDFTest {
    @Test
    fun `converts to NTriple format correctly`() {
        val expectedOutput = """
            |<http://orkg.org/orkg/resource/R100> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://orkg.org/orkg/class/Resource> .
            |<http://orkg.org/orkg/resource/R100> <http://www.w3.org/2000/01/rdf-schema#label> "some dummy label"^^<http://www.w3.org/2001/XMLSchema#string> .
            |
        """.trimMargin()
        val resource = createResource().copy(ThingId("R100"), label = "some dummy label")
        assertThat(resource::toNTriple.asString()).isEqualTo(expectedOutput)
    }

    @Test
    fun `converts list resources to NTriple format correctly`() {
        val expectedOutput = """
            |<http://orkg.org/orkg/resource/R100> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://www.w3.org/1999/02/22-rdf-syntax-ns#Seq> .
            |<http://orkg.org/orkg/resource/R100> <http://www.w3.org/2000/01/rdf-schema#label> "some dummy label"^^<http://www.w3.org/2001/XMLSchema#string> .
            |
        """.trimMargin()
        val resource = createResource().copy(ThingId("R100"), label = "some dummy label", classes = setOf(Classes.list))
        assertThat(resource::toNTriple.asString()).isEqualTo(expectedOutput)
    }
}
