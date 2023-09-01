package eu.tib.orkg.prototype.export.rdf.domain

import eu.tib.orkg.prototype.asString
import eu.tib.orkg.prototype.createLiteral
import eu.tib.orkg.prototype.createPredicate
import eu.tib.orkg.prototype.createResource
import eu.tib.orkg.prototype.createStatement
import eu.tib.orkg.prototype.statements.api.Classes
import eu.tib.orkg.prototype.statements.api.Predicates
import eu.tib.orkg.prototype.statements.domain.model.ThingId
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Disabled
import org.junit.jupiter.api.Test

@Suppress("HttpUrlsUsage")
internal class StatementRDFTest {
    @Test
    fun `converts statement with resource object to NTriple format correctly`() {
        val expectedOutput = """
            |<http://orkg.org/orkg/resource/R100> <http://orkg.org/orkg/predicate/P200> <http://orkg.org/orkg/resource/R300> .
            |
        """.trimMargin()
        val statement = createStatement(
            subject = createResource().copy(id = ThingId("R100"), label = "subject"),
            predicate = createPredicate().copy(id = ThingId("P200")),
            `object` = createResource().copy(id = ThingId("R300"), label = "object")
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
            subject = createResource().copy(id = ThingId("R100"), label = "subject"),
            predicate = createPredicate().copy(id = ThingId("P200")),
            `object` = createLiteral().copy(id = ThingId("L300"), label = "object")
        )
        assertThat(statement::toNTriple.asString()).isEqualTo(expectedOutput)
    }

    @Test
    @Disabled("We need the full URI. This might need a change in the API and needs to be discussed.")
    fun `converts statement with literal object and custom datatype to NTriple format correctly`() {
        val expectedOutput = """
            |<http://orkg.org/orkg/resource/R100> <http://orkg.org/orkg/predicate/P200> "object"^^<http://example.org/myDataType> .
            |
        """.trimMargin()
        val statement = createStatement(
            subject = createResource().copy(id = ThingId("R100"), label = "subject"),
            predicate = createPredicate().copy(id = ThingId("P200")),
            `object` = createLiteral().copy(
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
            subject = createResource().copy(id = ThingId("R100"), label = "subject", classes = setOf(Classes.list)),
            predicate = createPredicate().copy(id = Predicates.hasListElement),
            `object` = createLiteral().copy(id = ThingId("L300"), label = "object")
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
            subject = createResource().copy(id = ThingId("R100"), label = "subject"),
            predicate = createPredicate().copy(id = Predicates.hasListElement),
            `object` = createLiteral().copy(id = ThingId("L300"), label = "object")
        )
        assertThat(statement::toNTriple.asString()).isEqualTo(expectedOutput)
    }
}
