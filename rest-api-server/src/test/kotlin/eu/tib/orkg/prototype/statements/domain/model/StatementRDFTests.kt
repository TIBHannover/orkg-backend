package eu.tib.orkg.prototype.statements.domain.model

import eu.tib.orkg.prototype.createLiteral
import eu.tib.orkg.prototype.createPredicate
import eu.tib.orkg.prototype.createResource
import eu.tib.orkg.prototype.createStatement
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Disabled
import org.junit.jupiter.api.Test

@Suppress("HttpUrlsUsage")
class StatementRDFTests {
    @Test
    fun `converts statement with resource object to NTriple format correctly`() {
        val expectedOutput = """
            |<http://orkg.org/orkg/resource/R100> <http://orkg.org/orkg/predicate/P200> <http://orkg.org/orkg/resource/R300> .
            |
        """.trimMargin()
        val statement = createStatement(
            subject = createResource().copy(id = ResourceId(100), label = "subject"),
            predicate = createPredicate().copy(id = PredicateId(200)),
            `object` = createResource().copy(id = ResourceId(300), label = "object")
        )
        assertThat(statement.toNTriple()).isEqualTo(expectedOutput)
    }

    @Test
    fun `converts statement with literal object to NTriple format correctly`() {
        val expectedOutput = """
            |<http://orkg.org/orkg/resource/R100> <http://orkg.org/orkg/predicate/P200> "object"^^<http://www.w3.org/2001/XMLSchema#string> .
            |
        """.trimMargin()
        val statement = createStatement(
            subject = createResource().copy(id = ResourceId(100), label = "subject"),
            predicate = createPredicate().copy(id = PredicateId(200)),
            `object` = createLiteral().copy(id = LiteralId(300), label = "object")
        )
        assertThat(statement.toNTriple()).isEqualTo(expectedOutput)
    }

    @Test
    @Disabled("We need the full URI. This might need a change in the API and needs to be discussed.")
    fun `converts statement with literal object and custom datatype to NTriple format correctly`() {
        val expectedOutput = """
            |<http://orkg.org/orkg/resource/R100> <http://orkg.org/orkg/predicate/P200> "object"^^<http://example.org/myDataType> .
            |
        """.trimMargin()
        val statement = createStatement(
            subject = createResource().copy(id = ResourceId(100), label = "subject"),
            predicate = createPredicate().copy(id = PredicateId(200)),
            `object` = createLiteral().copy(id = LiteralId(300), label = "object", datatype = "http://example.org/myDataType")
        )
        assertThat(statement.toNTriple()).isEqualTo(expectedOutput)
    }
}
