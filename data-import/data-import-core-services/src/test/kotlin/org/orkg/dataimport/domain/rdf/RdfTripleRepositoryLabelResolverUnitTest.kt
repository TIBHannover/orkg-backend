package org.orkg.dataimport.domain.rdf

import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.eclipse.rdf4j.model.impl.SimpleValueFactory
import org.eclipse.rdf4j.model.vocabulary.RDFS
import org.junit.jupiter.api.Test
import org.orkg.common.testing.fixtures.MockkBaseTest
import org.orkg.dataimport.output.RdfTripleRepository
import java.util.UUID

internal class RdfTripleRepositoryLabelResolverUnitTest : MockkBaseTest {
    private val rdfTripleRepository = mockk<RdfTripleRepository>()

    private val rdfLabelResolverService = RdfTripleRepositoryLabelResolver(rdfTripleRepository)

    @Test
    fun `Given an rdf file id and a resource, when resolving a label and a single label is found, it returns the label`() {
        val rdfFileId = UUID.randomUUID()
        val resource = SimpleValueFactory.getInstance().createIRI("https://example.org/OK")
        val expected = "single label"

        every {
            rdfTripleRepository.findAllDistinctObjectLiteralsByRdfFileIdAndSubjectAndPredicate(rdfFileId, resource, RDFS.LABEL)
        } returns setOf(
            SimpleValueFactory.getInstance().createLiteral("single label"),
        )

        rdfLabelResolverService.resolve(rdfFileId, resource) shouldBe expected

        verify(exactly = 1) {
            rdfTripleRepository.findAllDistinctObjectLiteralsByRdfFileIdAndSubjectAndPredicate(rdfFileId, resource, RDFS.LABEL)
        }
    }

    @Test
    fun `Given an rdf file id and a resource, when resolving a label and no label is found, it returns null`() {
        val rdfFileId = UUID.randomUUID()
        val resource = SimpleValueFactory.getInstance().createIRI("https://example.org/OK")
        val expected = null

        every {
            rdfTripleRepository.findAllDistinctObjectLiteralsByRdfFileIdAndSubjectAndPredicate(rdfFileId, resource, RDFS.LABEL)
        } returns emptySet()

        rdfLabelResolverService.resolve(rdfFileId, resource) shouldBe expected

        verify(exactly = 1) {
            rdfTripleRepository.findAllDistinctObjectLiteralsByRdfFileIdAndSubjectAndPredicate(rdfFileId, resource, RDFS.LABEL)
        }
    }

    @Test
    fun `Given an rdf file id and a resource, when resolving a label and multiple labels are found, it prefers the english label`() {
        val rdfFileId = UUID.randomUUID()
        val resource = SimpleValueFactory.getInstance().createIRI("https://example.org/OK")
        val expected = "english label"

        every {
            rdfTripleRepository.findAllDistinctObjectLiteralsByRdfFileIdAndSubjectAndPredicate(rdfFileId, resource, RDFS.LABEL)
        } returns setOf(
            SimpleValueFactory.getInstance().createLiteral("german label", "de"),
            SimpleValueFactory.getInstance().createLiteral("english label", "en"),
            SimpleValueFactory.getInstance().createLiteral("arabic label", "ar"),
        )

        rdfLabelResolverService.resolve(rdfFileId, resource) shouldBe expected

        verify(exactly = 1) {
            rdfTripleRepository.findAllDistinctObjectLiteralsByRdfFileIdAndSubjectAndPredicate(rdfFileId, resource, RDFS.LABEL)
        }
    }

    @Test
    fun `Given an rdf file id and a resource, when resolving a label and multiple non-english labels are found, it returns the first label`() {
        val rdfFileId = UUID.randomUUID()
        val resource = SimpleValueFactory.getInstance().createIRI("https://example.org/OK")
        val expected = "german label"

        every {
            rdfTripleRepository.findAllDistinctObjectLiteralsByRdfFileIdAndSubjectAndPredicate(rdfFileId, resource, RDFS.LABEL)
        } returns setOf(
            SimpleValueFactory.getInstance().createLiteral("german label", "de"),
            SimpleValueFactory.getInstance().createLiteral("arabic label", "ar"),
        )

        rdfLabelResolverService.resolve(rdfFileId, resource) shouldBe expected

        verify(exactly = 1) {
            rdfTripleRepository.findAllDistinctObjectLiteralsByRdfFileIdAndSubjectAndPredicate(rdfFileId, resource, RDFS.LABEL)
        }
    }
}
