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
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.orkg.common.ThingId
import org.orkg.contenttypes.domain.actions.PublishSmartReviewState
import org.orkg.contenttypes.domain.testing.fixtures.createSmartReview
import org.orkg.contenttypes.input.testing.fixtures.dummyPublishSmartReviewCommand
import org.orkg.contenttypes.output.SmartReviewPublishedRepository
import org.orkg.graph.domain.Bundle
import org.orkg.graph.domain.BundleConfiguration
import org.orkg.graph.domain.Classes
import org.orkg.graph.input.StatementUseCases
import org.orkg.graph.testing.fixtures.createStatement
import org.springframework.data.domain.Sort

internal class SmartReviewVersionArchiverUnitTest {
    private val statementService: StatementUseCases = mockk()
    private val smartReviewPublishedRepository: SmartReviewPublishedRepository = mockk()

    private val smartReviewVersionArchiver = SmartReviewVersionArchiver(statementService, smartReviewPublishedRepository)

    @BeforeEach
    fun resetState() {
        clearAllMocks()
    }

    @AfterEach
    fun verifyMocks() {
        confirmVerified(statementService, smartReviewPublishedRepository)
    }

    @Test
    fun `Given a smart review publish command, it archives all statements about the smart review`() {
        val smartReview = createSmartReview()
        val command = dummyPublishSmartReviewCommand().copy(smartReviewId = smartReview.id)
        val smartReviewVersionId = ThingId("R321")
        val state = PublishSmartReviewState(
            smartReview = smartReview,
            smartReviewVersionId = smartReviewVersionId
        )
        val bundleConfiguration = BundleConfiguration(
            minLevel = null,
            maxLevel = 10,
            blacklist = listOf(Classes.researchField),
            whitelist = emptyList()
        )

        every {
            statementService.fetchAsBundle(
                thingId = smartReview.id,
                configuration = bundleConfiguration,
                includeFirst = true,
                sort = Sort.unsorted()
            )
        } returns Bundle(
            rootId = smartReview.id,
            bundle = mutableListOf(createStatement())
        )
        every { smartReviewPublishedRepository.save(any()) } just runs

        smartReviewVersionArchiver(command, state).asClue {
            it.smartReview shouldBe smartReview
            it.smartReviewVersionId shouldBe smartReviewVersionId
        }

        verify(exactly = 1) {
            statementService.fetchAsBundle(
                thingId = smartReview.id,
                configuration = bundleConfiguration,
                includeFirst = true,
                sort = Sort.unsorted()
            )
        }
        verify(exactly = 1) {
            smartReviewPublishedRepository.save(withArg {
                it.id shouldBe state.smartReviewVersionId!!
                it.rootId shouldBe command.smartReviewId
                it.subgraph shouldBe listOf(createStatement())
            })
        }
    }
}
