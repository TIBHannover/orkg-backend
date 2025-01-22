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
import org.orkg.contenttypes.domain.testing.fixtures.createComparison
import org.orkg.contenttypes.input.testing.fixtures.dummyPublishComparisonCommand
import org.orkg.graph.domain.Classes
import org.orkg.graph.domain.Predicates
import org.orkg.graph.input.StatementUseCases
import org.orkg.graph.input.UnsafeResourceUseCases
import org.orkg.graph.input.UpdateResourceUseCase

internal class ComparisonVersionHistoryUpdaterUnitTest : MockkBaseTest {
    private val statementService: StatementUseCases = mockk()
    private val unsafeResourceUseCases: UnsafeResourceUseCases = mockk()

    private val comparisonVersionHistoryUpdater = ComparisonVersionHistoryUpdater(statementService, unsafeResourceUseCases)

    @Test
    fun `Given a comparison publish command, it crates a new previous version statement and updates the previous version comparison class labels`() {
        val comparison = createComparison()
        val command = dummyPublishComparisonCommand().copy(id = comparison.id)
        val comparisonVersionId = ThingId("R165")
        val state = PublishComparisonState(comparison, comparisonVersionId)

        every {
            statementService.add(
                userId = command.contributorId,
                subject = comparison.id,
                predicate = Predicates.hasPublishedVersion,
                `object` = comparisonVersionId
            )
        } just runs
        every { unsafeResourceUseCases.update(any()) } just runs

        comparisonVersionHistoryUpdater(command, state).asClue {
            it.comparison shouldBe comparison
            it.comparisonVersionId shouldBe comparisonVersionId
        }

        verify(exactly = 1) {
            statementService.add(
                userId = command.contributorId,
                subject = comparison.id,
                predicate = Predicates.hasPublishedVersion,
                `object` = comparisonVersionId
            )
        }
        verify(exactly = 1) {
            unsafeResourceUseCases.update(
                UpdateResourceUseCase.UpdateCommand(
                    id = comparison.versions.published.first().id,
                    contributorId = command.contributorId,
                    classes = setOf(Classes.comparisonPublished)
                )
            )
        }
    }

    @Test
    fun `Given a comparison publish command, when no previous published version exists, it only crates a new previous version statement`() {
        val comparison = createComparison().let {
            it.copy(versions = it.versions.copy(it.versions.head, emptyList()))
        }
        val command = dummyPublishComparisonCommand().copy(id = comparison.id)
        val comparisonVersionId = ThingId("R165")
        val state = PublishComparisonState(comparison, comparisonVersionId)

        every {
            statementService.add(
                userId = command.contributorId,
                subject = comparison.id,
                predicate = Predicates.hasPublishedVersion,
                `object` = comparisonVersionId
            )
        } just runs

        comparisonVersionHistoryUpdater(command, state).asClue {
            it.comparison shouldBe comparison
            it.comparisonVersionId shouldBe comparisonVersionId
        }

        verify(exactly = 1) {
            statementService.add(
                userId = command.contributorId,
                subject = comparison.id,
                predicate = Predicates.hasPublishedVersion,
                `object` = comparisonVersionId
            )
        }
    }
}
