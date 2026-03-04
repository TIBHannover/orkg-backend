package org.orkg.contenttypes.domain.actions.smartreviews

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
import org.orkg.common.testing.fixtures.fixedClock
import org.orkg.contenttypes.domain.SnapshotId
import org.orkg.contenttypes.domain.SnapshotIdGenerator
import org.orkg.contenttypes.domain.actions.PublishSmartReviewState
import org.orkg.contenttypes.domain.testing.fixtures.createSmartReview
import org.orkg.contenttypes.input.testing.fixtures.publishSmartReviewCommand
import org.orkg.contenttypes.output.SmartReviewSnapshotRepository
import org.orkg.graph.domain.Bundle
import org.orkg.graph.domain.BundleConfiguration
import org.orkg.graph.domain.Classes
import org.orkg.graph.input.StatementUseCases
import org.orkg.graph.testing.fixtures.createStatement
import org.springframework.data.domain.Sort
import java.time.OffsetDateTime

internal class SmartReviewVersionArchiverUnitTest : MockkBaseTest {
    private val statementService: StatementUseCases = mockk()
    private val smartReviewSnapshotRepository: SmartReviewSnapshotRepository = mockk()
    private val snapshotIdGenerator: SnapshotIdGenerator = mockk()

    private val smartReviewVersionArchiver = SmartReviewVersionArchiver(
        statementService,
        smartReviewSnapshotRepository,
        snapshotIdGenerator,
        fixedClock,
    )

    @Test
    fun `Given a smart review publish command, it archives all statements about the smart review`() {
        val smartReview = createSmartReview()
        val command = publishSmartReviewCommand().copy(smartReviewId = smartReview.id)
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
        val snapshotId = SnapshotId("ABC")

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
        every { snapshotIdGenerator.nextIdentity() } returns snapshotId
        every { smartReviewSnapshotRepository.save(any()) } just runs

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
        verify(exactly = 1) { snapshotIdGenerator.nextIdentity() }
        verify(exactly = 1) {
            smartReviewSnapshotRepository.save(
                withArg {
                    it.id shouldBe snapshotId
                    it.createdAt shouldBe OffsetDateTime.now(fixedClock)
                    it.createdBy shouldBe command.contributorId
                    it.resourceId shouldBe state.smartReviewVersionId!!
                    it.rootId shouldBe command.smartReviewId
                    it.subgraph shouldBe listOf(createStatement())
                }
            )
        }
    }
}
