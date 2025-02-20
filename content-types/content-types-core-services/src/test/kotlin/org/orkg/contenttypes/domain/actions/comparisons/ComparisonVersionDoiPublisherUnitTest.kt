package org.orkg.contenttypes.domain.actions.comparisons

import io.kotest.assertions.asClue
import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.just
import io.mockk.mockk
import io.mockk.runs
import io.mockk.verify
import org.junit.jupiter.api.Test
import org.orkg.common.ThingId
import org.orkg.common.testing.fixtures.MockkBaseTest
import org.orkg.contenttypes.domain.actions.PublishComparisonState
import org.orkg.contenttypes.domain.actions.SingleStatementPropertyCreator
import org.orkg.contenttypes.domain.identifiers.DOI
import org.orkg.contenttypes.domain.testing.fixtures.createComparison
import org.orkg.contenttypes.input.testing.fixtures.publishComparisonCommand
import org.orkg.contenttypes.output.ComparisonRepository
import org.orkg.contenttypes.output.DoiService
import org.orkg.graph.domain.Classes
import org.orkg.graph.domain.Predicates
import java.net.URI

internal class ComparisonVersionDoiPublisherUnitTest : MockkBaseTest {
    private val singleStatementPropertyCreator: SingleStatementPropertyCreator = mockk()
    private val comparisonRepository: ComparisonRepository = mockk()
    private val doiService: DoiService = mockk()

    private val comparisonVersionArchiver = ComparisonVersionDoiPublisher(
        singleStatementPropertyCreator = singleStatementPropertyCreator,
        comparisonRepository = comparisonRepository,
        doiService = doiService,
        comparisonPublishBaseUri = "https://orkg.org/review/"
    )

    @Test
    fun `Given a comparison publish command, when a new doi should be assigned, it registers a new doi and creates a hasDOI statement`() {
        val comparison = createComparison()
        val comparisonVersionId = ThingId("R45214")
        val command = publishComparisonCommand().copy(id = comparison.id)
        val state = PublishComparisonState(comparison, comparisonVersionId)
        val doi = "10.1000/$comparisonVersionId"
        val relatedDOIs = listOf("10.1000/a", "10.1000/b")

        every { doiService.register(any()) } returns DOI.of(doi)
        every { singleStatementPropertyCreator.create(any(), any(), any(), any()) } just runs
        every { comparisonRepository.findAllDOIsRelatedToComparison(comparison.id) } returns relatedDOIs

        comparisonVersionArchiver(command, state).asClue {
            it.comparison shouldBe comparison
            it.comparisonVersionId shouldBe comparisonVersionId
        }

        verify(exactly = 1) {
            doiService.register(
                withArg {
                    it.suffix shouldBe comparisonVersionId.value
                    it.title shouldBe comparison.title
                    it.subject shouldBe command.subject
                    it.description shouldBe command.description
                    it.url shouldBe URI.create("https://orkg.org/review/$comparisonVersionId")
                    it.creators shouldBe command.authors
                    it.resourceType shouldBe Classes.comparison.value
                    it.resourceTypeGeneral shouldBe "Dataset"
                    it.relatedIdentifiers shouldBe relatedDOIs
                }
            )
        }
        verify(exactly = 1) {
            singleStatementPropertyCreator.create(
                contributorId = command.contributorId,
                subjectId = comparisonVersionId,
                predicateId = Predicates.hasDOI,
                label = doi
            )
        }
        verify(exactly = 1) {
            comparisonRepository.findAllDOIsRelatedToComparison(comparison.id)
        }
    }

    @Test
    fun `Given a comparison publish command, when no doi should be assigned, it does nothing`() {
        val comparison = createComparison()
        val command = publishComparisonCommand().copy(id = comparison.id, assignDOI = false)
        val state = PublishComparisonState(comparison)

        comparisonVersionArchiver(command, state).asClue {
            it.comparison shouldBe comparison
        }
    }
}
