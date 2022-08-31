package eu.tib.orkg.prototype.statements.domain.model

import eu.tib.orkg.prototype.createPredicate
import eu.tib.orkg.prototype.export.rdf.domain.toNTriple
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

@Suppress("HttpUrlsUsage")
class PredicateRDFTests {
    @Test
    fun `converts to NTriple format correctly`() {
        val expectedOutput = """
            |<http://orkg.org/orkg/predicate/P1> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://orkg.org/orkg/class/Predicate> .
            |<http://orkg.org/orkg/predicate/P1> <http://www.w3.org/2000/01/rdf-schema#label> "some predicate label"^^<http://www.w3.org/2001/XMLSchema#string> .
            |
        """.trimMargin()
        val predicate = createPredicate()
        assertThat(predicate.toNTriple()).isEqualTo(expectedOutput)
    }
}
