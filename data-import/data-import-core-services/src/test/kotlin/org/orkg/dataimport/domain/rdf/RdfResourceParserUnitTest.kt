package org.orkg.dataimport.domain.rdf

import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkStatic
import io.mockk.verify
import org.eclipse.rdf4j.model.impl.SimpleValueFactory
import org.eclipse.rdf4j.model.vocabulary.RDF
import org.junit.jupiter.api.Test
import org.orkg.common.testing.fixtures.MockkBaseTest
import org.orkg.dataimport.output.RdfTripleRepository
import java.util.UUID

internal class RdfResourceParserUnitTest : MockkBaseTest {
    private val rdfFileId = UUID.fromString("9cb68baa-593d-4516-8473-ca0de261f3f4")
    private val rdfTripleRepository: RdfTripleRepository = mockk()
    private val rdfLabelResolverService: RdfLabelResolverService = mockk()

    private val rdfResourceParser = RdfResourceParser(rdfFileId, rdfTripleRepository, rdfLabelResolverService)

    @Test
    fun `Given a resource iri, when parsing an rdf resource, it parses the rdf resource correctly`() {
        val id = UUID.fromString("5dcb3093-3cbb-46a7-bdfb-44de453d9560")
        val iri = SimpleValueFactory.getInstance().createIRI("https://example.org")
        val class1 = SimpleValueFactory.getInstance().createIRI("https://example.org/class1")
        val expected = RdfResource(
            id = id,
            rdfFileId = rdfFileId,
            iri = iri,
            label = "resource label",
            classes = setOf(class1),
        )

        mockkStatic(UUID::class) {
            every { UUID.randomUUID() } returns id
            every { rdfLabelResolverService.resolveLabel(rdfFileId, iri) } returns "resource label"
            every {
                rdfTripleRepository.findAllDistinctObjectIrisByRdfFileIdAndSubjectAndPredicate(
                    rdfFileId = rdfFileId,
                    subject = iri,
                    predicate = RDF.TYPE,
                )
            } returns setOf(class1)

            rdfResourceParser.process(iri) shouldBe expected

            verify(exactly = 1) { UUID.randomUUID() }
            verify(exactly = 1) { rdfLabelResolverService.resolveLabel(rdfFileId, iri) }
            verify(exactly = 1) { rdfTripleRepository.findAllDistinctObjectIrisByRdfFileIdAndSubjectAndPredicate(rdfFileId, iri, RDF.TYPE) }
        }
    }

    @Test
    fun `Given a resource iri, when parsing an rdf resource and iri is an ORKG iri, it does not process the iri`() {
        val iri = SimpleValueFactory.getInstance().createIRI("$ORKG_VOCAB_PREFIX/resource/R123")

        rdfResourceParser.process(iri) shouldBe null
    }
}
