package org.orkg.dataimport.domain.rdf

import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkStatic
import io.mockk.verify
import org.eclipse.rdf4j.model.impl.SimpleValueFactory
import org.eclipse.rdf4j.model.vocabulary.RDFS
import org.junit.jupiter.api.Test
import org.orkg.common.testing.fixtures.MockkBaseTest
import org.orkg.dataimport.output.RdfTripleRepository
import java.util.UUID

internal class RdfClassParserUnitTest : MockkBaseTest {
    private val rdfFileId = UUID.fromString("9cb68baa-593d-4516-8473-ca0de261f3f4")
    private val rdfTripleRepository: RdfTripleRepository = mockk()

    private val rdfClassParser = RdfClassParser(rdfFileId, rdfTripleRepository)

    @Test
    fun `Given a class iri, when parsing an rdf class, it parses the rdf class correctly`() {
        val id = UUID.fromString("5dcb3093-3cbb-46a7-bdfb-44de453d9560")
        val iri = SimpleValueFactory.getInstance().createIRI("https://example.org")
        val expected = RdfClass(
            id = id,
            rdfFileId = rdfFileId,
            iri = iri,
            label = "class label",
        )

        mockkStatic(UUID::class) {
            every { UUID.randomUUID() } returns id
            every {
                rdfTripleRepository.findAllDistinctObjectLiteralsByRdfFileIdAndSubjectAndPredicate(rdfFileId, iri, RDFS.LABEL)
            } returns setOf(
                SimpleValueFactory.getInstance().createLiteral("class label"),
            )

            rdfClassParser.process(iri) shouldBe expected

            verify(exactly = 1) { UUID.randomUUID() }
            verify(exactly = 1) { rdfTripleRepository.findAllDistinctObjectLiteralsByRdfFileIdAndSubjectAndPredicate(rdfFileId, iri, RDFS.LABEL) }
        }
    }

    @Test
    fun `Given a class iri, when parsing an rdf class and iri is an ORKG iri, it does not process the iri`() {
        val iri = SimpleValueFactory.getInstance().createIRI("$ORKG_VOCAB_PREFIX/class/C123")

        rdfClassParser.process(iri) shouldBe null
    }
}
