package org.orkg.dataimport.domain.rdf

import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkStatic
import io.mockk.verify
import org.eclipse.rdf4j.model.impl.SimpleValueFactory
import org.junit.jupiter.api.Test
import org.orkg.common.testing.fixtures.MockkBaseTest
import java.util.UUID

internal class RdfPredicateParserUnitTest : MockkBaseTest {
    private val rdfFileId = UUID.fromString("9cb68baa-593d-4516-8473-ca0de261f3f4")
    private val rdfLabelResolverService: RdfLabelResolverService = mockk()

    private val rdfPredicateParser = RdfPredicateParser(rdfFileId, rdfLabelResolverService)

    @Test
    fun `Given a predicate iri, when parsing an rdf predicate, it parses the rdf predicate correctly`() {
        val id = UUID.fromString("5dcb3093-3cbb-46a7-bdfb-44de453d9560")
        val iri = SimpleValueFactory.getInstance().createIRI("https://example.org")
        val expected = RdfPredicate(
            id = id,
            rdfFileId = rdfFileId,
            iri = iri,
            label = "predicate label",
        )

        mockkStatic(UUID::class) {
            every { UUID.randomUUID() } returns id
            every { rdfLabelResolverService.resolveLabel(rdfFileId, iri) } returns "predicate label"

            rdfPredicateParser.process(iri) shouldBe expected

            verify(exactly = 1) { UUID.randomUUID() }
            verify(exactly = 1) { rdfLabelResolverService.resolveLabel(rdfFileId, iri) }
        }
    }

    @Test
    fun `Given a predicate iri, when parsing an rdf predicate and iri is an ORKG iri, it does not process the iri`() {
        val iri = SimpleValueFactory.getInstance().createIRI("$ORKG_VOCAB_PREFIX/predicate/P123")

        rdfPredicateParser.process(iri) shouldBe null
    }
}
