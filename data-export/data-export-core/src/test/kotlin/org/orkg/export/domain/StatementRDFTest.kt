package org.orkg.export.domain

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.orkg.common.ThingId
import org.orkg.export.testing.fixtures.asString
import org.orkg.graph.domain.Classes
import org.orkg.graph.domain.Predicates
import org.orkg.graph.testing.fixtures.createLiteral
import org.orkg.graph.testing.fixtures.createPredicate
import org.orkg.graph.testing.fixtures.createResource
import org.orkg.graph.testing.fixtures.createStatement

@Suppress("HttpUrlsUsage")
internal class StatementRDFTest {
    @Test
    fun `converts statement with resource object to NTriple format correctly`() {
        val expectedOutput = """
            |<http://orkg.org/orkg/resource/R100> <http://orkg.org/orkg/predicate/P200> <http://orkg.org/orkg/resource/R300> .
            |
        """.trimMargin()
        val statement = createStatement(
            subject = createResource(id = ThingId("R100"), label = "subject"),
            predicate = createPredicate(id = ThingId("P200")),
            `object` = createResource(id = ThingId("R300"), label = "object")
        )
        assertThat(statement::toNTriple.asString()).isEqualTo(expectedOutput)
    }

    @Test
    fun `converts statement with literal object to NTriple format correctly`() {
        val expectedOutput = """
            |<http://orkg.org/orkg/resource/R100> <http://orkg.org/orkg/predicate/P200> "object"^^<http://www.w3.org/2001/XMLSchema#string> .
            |
        """.trimMargin()
        val statement = createStatement(
            subject = createResource(id = ThingId("R100"), label = "subject"),
            predicate = createPredicate(id = ThingId("P200")),
            `object` = createLiteral(id = ThingId("L300"), label = "object")
        )
        assertThat(statement::toNTriple.asString()).isEqualTo(expectedOutput)
    }

    @Test
    fun `converts statement with literal object and XSD datatype to NTriple format correctly`() {
        val expectedOutput = """
            |<http://orkg.org/orkg/resource/R100> <http://orkg.org/orkg/predicate/P200> "object"^^<http://www.w3.org/2001/XMLSchema#integer> .
            |
        """.trimMargin()
        val statement = createStatement(
            subject = createResource(id = ThingId("R100"), label = "subject"),
            predicate = createPredicate(id = ThingId("P200")),
            `object` = createLiteral(id = ThingId("L300"), label = "object", datatype = "xsd:integer")
        )
        assertThat(statement::toNTriple.asString()).isEqualTo(expectedOutput)
    }

    @Test
    fun `converts statement with literal object and custom datatype to NTriple format correctly`() {
        val expectedOutput = """
            |<http://orkg.org/orkg/resource/R100> <http://orkg.org/orkg/predicate/P200> "object"^^<http://example.org/myDataType> .
            |
        """.trimMargin()
        val statement = createStatement(
            subject = createResource(id = ThingId("R100"), label = "subject"),
            predicate = createPredicate(id = ThingId("P200")),
            `object` = createLiteral(
                id = ThingId("L300"), label = "object", datatype = "http://example.org/myDataType"
            )
        )
        assertThat(statement::toNTriple.asString()).isEqualTo(expectedOutput)
    }

    @Test
    fun `converts 'has list element' statements to NTriple format correctly`() {
        val expectedOutput = """
            |<http://orkg.org/orkg/resource/R100> <http://www.w3.org/1999/02/22-rdf-syntax-ns#_6> "object"^^<http://www.w3.org/2001/XMLSchema#string> .
            |
        """.trimMargin()
        val statement = createStatement(
            subject = createResource(id = ThingId("R100"), label = "subject", classes = setOf(Classes.list)),
            predicate = createPredicate(id = Predicates.hasListElement),
            `object` = createLiteral(id = ThingId("L300"), label = "object")
        ).copy(index = 5)
        assertThat(statement::toNTriple.asString()).isEqualTo(expectedOutput)
    }

    @Test
    fun `does not map 'has list element' statements when not used in a list`() {
        val expectedOutput = """
            |<http://orkg.org/orkg/resource/R100> <http://orkg.org/orkg/predicate/hasListElement> "object"^^<http://www.w3.org/2001/XMLSchema#string> .
            |
        """.trimMargin()
        val statement = createStatement(
            subject = createResource(id = ThingId("R100"), label = "subject"),
            predicate = createPredicate(id = Predicates.hasListElement),
            `object` = createLiteral(id = ThingId("L300"), label = "object")
        )
        assertThat(statement::toNTriple.asString()).isEqualTo(expectedOutput)
    }
}
