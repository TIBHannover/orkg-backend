package org.orkg.contenttypes.domain.actions.smartreviews

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
import org.orkg.common.ThingId
import org.orkg.contenttypes.domain.actions.PublishSmartReviewState
import org.orkg.contenttypes.domain.actions.SingleStatementPropertyCreator
import org.orkg.contenttypes.domain.identifiers.DOI
import org.orkg.contenttypes.domain.testing.fixtures.createDummySmartReview
import org.orkg.contenttypes.input.testing.fixtures.dummyPublishSmartReviewCommand
import org.orkg.contenttypes.output.DoiService
import org.orkg.graph.domain.Predicates

class SmartReviewVersionDoiPublisherUnitTest {
    private val singleStatementPropertyCreator: SingleStatementPropertyCreator = mockk()
    private val doiService: DoiService = mockk()

    private val smartReviewVersionArchiver = SmartReviewVersionDoiPublisher(
        singleStatementPropertyCreator = singleStatementPropertyCreator,
        doiService = doiService,
        smartReviewPublishBaseUri = "https://orkg.org/review/"
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
    fun `Given a smart review publish command, when a new doi should be assigned, it registers a new doi creates a hasDOI statement`() {
        val smartReview = createDummySmartReview()
        val command = dummyPublishSmartReviewCommand().copy(smartReviewId = smartReview.id)
        val smartReviewVersionId = ThingId("R321")
        val state = PublishSmartReviewState(
            smartReview = smartReview,
            smartReviewVersionId = smartReviewVersionId
        )
        val doi = "10.1000/182"

        every { doiService.register(any()) } returns DOI.of(doi)
        every { singleStatementPropertyCreator.create(any(), any(), any(), any()) } just runs

        smartReviewVersionArchiver(command, state).asClue {
            it.smartReview shouldBe smartReview
            it.smartReviewVersionId shouldBe smartReviewVersionId
        }

        verify(exactly = 1) {
            doiService.register(withArg {
                it.suffix shouldBe smartReviewVersionId.value
                it.title shouldBe smartReview.title
                it.subject shouldBe smartReview.researchFields.firstOrNull()?.label.orEmpty()
                it.description shouldBe command.description!!
                it.url shouldBe URI.create("https://orkg.org/review/${smartReviewVersionId.value}")
                it.creators shouldBe smartReview.authors
                it.resourceType shouldBe "Review"
                it.resourceTypeGeneral shouldBe "Preprint"
                it.relatedIdentifiers shouldBe emptyList()
            })
        }
        verify(exactly = 1) {
            singleStatementPropertyCreator.create(
                contributorId = command.contributorId,
                subjectId = smartReviewVersionId,
                predicateId = Predicates.hasDOI,
                label = doi
            )
        }
    }

    @Test
    fun `Given a smart review publish command, when no doi should be assigned, it does nothing`() {
        val smartReview = createDummySmartReview()
        val command = dummyPublishSmartReviewCommand().copy(smartReviewId = smartReview.id, assignDOI = false)
        val smartReviewVersionId = ThingId("R321")
        val state = PublishSmartReviewState(
            smartReview = smartReview,
            smartReviewVersionId = smartReviewVersionId
        )

        smartReviewVersionArchiver(command, state).asClue {
            it.smartReview shouldBe smartReview
            it.smartReviewVersionId shouldBe smartReviewVersionId
        }
    }
}
