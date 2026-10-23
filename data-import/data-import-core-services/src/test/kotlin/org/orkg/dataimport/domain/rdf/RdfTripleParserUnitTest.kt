package org.orkg.dataimport.domain.rdf

import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.mockkStatic
import io.mockk.verify
import org.eclipse.rdf4j.model.impl.SimpleValueFactory
import org.junit.jupiter.api.Test
import org.orkg.common.testing.fixtures.MockkBaseTest
import java.util.UUID

internal class RdfTripleParserUnitTest : MockkBaseTest {
    private val rdfFileId = UUID.fromString("9cb68baa-593d-4516-8473-ca0de261f3f4")
    private val rdfTripleParser = RdfTripleParser(rdfFileId)

    @Test
    fun `Given a triple iri, when parsing an rdf triple, it parses the rdf triple correctly`() {
        val id = UUID.fromString("5dcb3093-3cbb-46a7-bdfb-44de453d9560")
        val subject = SimpleValueFactory.getInstance().createIRI("https://example.org/subject")
        val predicate = SimpleValueFactory.getInstance().createIRI("https://example.org/predicate")
        val `object` = SimpleValueFactory.getInstance().createIRI("https://example.org/object")
        val iri = SimpleValueFactory.getInstance().createStatement(subject, predicate, `object`)
        val expected = RdfTriple(
            id = id,
            rdfFileId = rdfFileId,
            subject = subject,
            predicate = predicate,
            `object` = `object`,
        )

        mockkStatic(UUID::class) {
            every { UUID.randomUUID() } returns id

            rdfTripleParser.process(iri) shouldBe expected

            verify(exactly = 1) { UUID.randomUUID() }
        }
    }
}
