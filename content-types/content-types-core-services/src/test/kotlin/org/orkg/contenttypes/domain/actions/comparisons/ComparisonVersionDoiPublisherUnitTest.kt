package org.orkg.contenttypes.domain.actions.comparisons

import io.kotest.assertions.asClue
import io.kotest.matchers.shouldBe
import io.mockk.clearAllMocks
import io.mockk.confirmVerified
import io.mockk.every
import io.mockk.just
import io.mockk.mockk
import io.mockk.runs
import io.mockk.verify
import java.net.URI
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.orkg.contenttypes.domain.actions.PublishComparisonState
import org.orkg.contenttypes.domain.actions.SingleStatementPropertyCreator
import org.orkg.contenttypes.domain.identifiers.DOI
import org.orkg.contenttypes.domain.testing.fixtures.createComparison
import org.orkg.contenttypes.input.testing.fixtures.dummyPublishComparisonCommand
import org.orkg.contenttypes.output.ComparisonRepository
import org.orkg.contenttypes.output.DoiService
import org.orkg.graph.domain.Classes
import org.orkg.graph.domain.Predicates

internal class ComparisonVersionDoiPublisherUnitTest {
    private val singleStatementPropertyCreator: SingleStatementPropertyCreator = mockk()
    private val comparisonRepository: ComparisonRepository = mockk()
    private val doiService: DoiService = mockk()

    private val comparisonVersionArchiver = ComparisonVersionDoiPublisher(
        singleStatementPropertyCreator = singleStatementPropertyCreator,
        comparisonRepository = comparisonRepository,
        doiService = doiService,
        comparisonPublishBaseUri = "https://orkg.org/review/"
    )

    @BeforeEach
    fun resetState() {
        clearAllMocks()
    }

    @AfterEach
    fun verifyMocks() {
        confirmVerified(singleStatementPropertyCreator, doiService)
    }

    @Test
    fun `Given a comparison publish command, when a new doi should be assigned, it registers a new doi and creates a hasDOI statement`() {
        val comparison = createComparison()
        val command = dummyPublishComparisonCommand().copy(id = comparison.id)
        val state = PublishComparisonState(comparison)
        val doi = "10.1000/182"
        val relatedDOIs = listOf("10.1000/a", "10.1000/b")

        every { doiService.register(any()) } returns DOI.of(doi)
        every { singleStatementPropertyCreator.create(any(), any(), any(), any()) } just runs
        every { comparisonRepository.findAllDOIsRelatedToComparison(comparison.id) } returns relatedDOIs

        comparisonVersionArchiver(command, state).asClue {
            it.comparison shouldBe comparison
        }

        verify(exactly = 1) {
            doiService.register(withArg {
                it.suffix shouldBe comparison.id.value
                it.title shouldBe comparison.title
                it.subject shouldBe command.subject
                it.description shouldBe command.description
                it.url shouldBe URI.create("https://orkg.org/review/${comparison.id}")
                it.creators shouldBe command.authors
                it.resourceType shouldBe Classes.comparison.value
                it.resourceTypeGeneral shouldBe "Dataset"
                it.relatedIdentifiers shouldBe relatedDOIs
            })
        }
        verify(exactly = 1) {
            singleStatementPropertyCreator.create(
                contributorId = command.contributorId,
                subjectId = comparison.id,
                predicateId = Predicates.hasDOI,
                label = doi
            )
        }
    }

    @Test
    fun `Given a comparison publish command, when no doi should be assigned, it does nothing`() {
        val comparison = createComparison()
        val command = dummyPublishComparisonCommand().copy(id = comparison.id, assignDOI = false)
        val state = PublishComparisonState(comparison)

        comparisonVersionArchiver(command, state).asClue {
            it.comparison shouldBe comparison
        }
    }
}
