package eu.tib.orkg.prototype.statements.domain.model.rdf

import eu.tib.orkg.prototype.statements.domain.model.Class
import eu.tib.orkg.prototype.statements.domain.model.ClassId
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import java.net.URI

class RdfClassRepresentationTest {

    @Test
    fun classWithoutURL() {
        val entity = RdfClass(createClass())
        val output = RdfRepresentation(entity).render()
        val expectedOutput = """
            <http://orkg.org/orkg/class/C23> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://www.w3.org/2002/07/owl#Class> .
            <http://orkg.org/orkg/class/C23> <http://www.w3.org/2000/01/rdf-schema#label> "irrelevant"^^<http://www.w3.org/2001/XMLSchema#string> .
        """.trimIndent() + "\n"
        assertThat(output).isEqualTo(expectedOutput)
    }

    @Test
    fun classWithURL() {
        val entity = RdfClass(createClass(URI("http://example.org/SomeEntity")))
        val output = RdfRepresentation(entity).render()
        val expectedOutput = """
            <http://orkg.org/orkg/class/C23> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://www.w3.org/2002/07/owl#Class> .
            <http://orkg.org/orkg/class/C23> <http://www.w3.org/2000/01/rdf-schema#label> "irrelevant"^^<http://www.w3.org/2001/XMLSchema#string> .
            <http://orkg.org/orkg/class/C23> <http://www.w3.org/2002/07/owl#equivalentClass> <http://example.org/SomeEntity> .
        """.trimIndent() + "\n"
        assertThat(output).isEqualTo(expectedOutput)
    }

    private fun createClass(uri: URI? = null) =
        Class(label = "irrelevant", id = ClassId(23), createdAt = null, uri = uri)
}
