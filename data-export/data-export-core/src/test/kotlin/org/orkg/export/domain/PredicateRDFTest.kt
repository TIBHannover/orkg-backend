package org.orkg.export.domain

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.orkg.export.testing.fixtures.asString
import org.orkg.graph.testing.fixtures.createPredicate

@Suppress("HttpUrlsUsage")
internal class PredicateRDFTest {
    @Test
    fun `converts to NTriple format correctly`() {
        val expectedOutput = """
            |<http://orkg.org/orkg/predicate/P1> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://orkg.org/orkg/class/Predicate> .
            |<http://orkg.org/orkg/predicate/P1> <http://www.w3.org/2000/01/rdf-schema#label> "some predicate label"^^<http://www.w3.org/2001/XMLSchema#string> .
            |
        """.trimMargin()
        val predicate = createPredicate()
        assertThat(predicate::toNTriple.asString()).isEqualTo(expectedOutput)
    }
}
