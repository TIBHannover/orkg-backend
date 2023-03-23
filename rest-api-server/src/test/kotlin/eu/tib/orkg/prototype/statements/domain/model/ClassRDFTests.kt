package eu.tib.orkg.prototype.statements.domain.model

import eu.tib.orkg.prototype.createClass
import eu.tib.orkg.prototype.asString
import eu.tib.orkg.prototype.export.rdf.domain.toNTriple
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

@Suppress("HttpUrlsUsage")
class ClassRDFTests {
    @Test
    fun `converts to NTriple format correctly`() {
        val expectedOutput = """
            |<http://orkg.org/orkg/class/C100> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://www.w3.org/2002/07/owl#Class> .
            |<http://orkg.org/orkg/class/C100> <http://www.w3.org/2000/01/rdf-schema#label> "some dummy label"^^<http://www.w3.org/2001/XMLSchema#string> .
            |
        """.trimMargin()
        val `class` = createClass().copy(id = ThingId("C100"), label = "some dummy label", uri = null)
        assertThat(`class`::toNTriple.asString()).isEqualTo(expectedOutput)
    }

    @Test
    fun `converts to NTriple format correctly when URI is set`() {
        val expectedOutput = """
            |<http://orkg.org/orkg/class/C100> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://www.w3.org/2002/07/owl#Class> .
            |<http://orkg.org/orkg/class/C100> <http://www.w3.org/2002/07/owl#equivalentClass> <https://example.org/OK> .
            |<http://orkg.org/orkg/class/C100> <http://www.w3.org/2000/01/rdf-schema#label> "some dummy label"^^<http://www.w3.org/2001/XMLSchema#string> .
            |
        """.trimMargin()
        val `class` = createClass().copy(id = ThingId("C100"), label = "some dummy label")
        assertThat(`class`::toNTriple.asString()).isEqualTo(expectedOutput)
    }
}
