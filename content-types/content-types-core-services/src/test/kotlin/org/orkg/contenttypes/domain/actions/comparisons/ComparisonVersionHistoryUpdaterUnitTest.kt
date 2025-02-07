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
import org.orkg.contenttypes.input.testing.fixtures.publishComparisonCommand
import org.orkg.graph.domain.Classes
import org.orkg.graph.domain.Predicates
import org.orkg.graph.domain.StatementId
import org.orkg.graph.input.CreateStatementUseCase
import org.orkg.graph.input.UnsafeResourceUseCases
import org.orkg.graph.input.UnsafeStatementUseCases
import org.orkg.graph.input.UpdateResourceUseCase

internal class ComparisonVersionHistoryUpdaterUnitTest : MockkBaseTest {
    private val unsafeStatementUseCases: UnsafeStatementUseCases = mockk()
    private val unsafeResourceUseCases: UnsafeResourceUseCases = mockk()

    private val comparisonVersionHistoryUpdater = ComparisonVersionHistoryUpdater(unsafeStatementUseCases, unsafeResourceUseCases)

    @Test
    fun `Given a comparison publish command, it crates a new previous version statement and updates the previous version comparison class labels`() {
        val comparison = createComparison()
        val command = publishComparisonCommand().copy(id = comparison.id)
        val comparisonVersionId = ThingId("R165")
        val state = PublishComparisonState(comparison, comparisonVersionId)

        every {
            unsafeStatementUseCases.create(
                CreateStatementUseCase.CreateCommand(
                    contributorId = command.contributorId,
                    subjectId = comparison.id,
                    predicateId = Predicates.hasPublishedVersion,
                    objectId = comparisonVersionId
                )
            )
        } returns StatementId("S1")
        every { unsafeResourceUseCases.update(any()) } just runs

        comparisonVersionHistoryUpdater(command, state).asClue {
            it.comparison shouldBe comparison
            it.comparisonVersionId shouldBe comparisonVersionId
        }

        verify(exactly = 1) {
            unsafeStatementUseCases.create(
                CreateStatementUseCase.CreateCommand(
                    contributorId = command.contributorId,
                    subjectId = comparison.id,
                    predicateId = Predicates.hasPublishedVersion,
                    objectId = comparisonVersionId
                )
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
        val command = publishComparisonCommand().copy(id = comparison.id)
        val comparisonVersionId = ThingId("R165")
        val state = PublishComparisonState(comparison, comparisonVersionId)

        every {
            unsafeStatementUseCases.create(
                CreateStatementUseCase.CreateCommand(
                    contributorId = command.contributorId,
                    subjectId = comparison.id,
                    predicateId = Predicates.hasPublishedVersion,
                    objectId = comparisonVersionId
                )
            )
        } returns StatementId("S1")

        comparisonVersionHistoryUpdater(command, state).asClue {
            it.comparison shouldBe comparison
            it.comparisonVersionId shouldBe comparisonVersionId
        }

        verify(exactly = 1) {
            unsafeStatementUseCases.create(
                CreateStatementUseCase.CreateCommand(
                    contributorId = command.contributorId,
                    subjectId = comparison.id,
                    predicateId = Predicates.hasPublishedVersion,
                    objectId = comparisonVersionId
                )
            )
        }
    }
}
