package org.orkg.contenttypes.domain.actions.papers

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
import org.orkg.contenttypes.domain.actions.PublishPaperState
import org.orkg.contenttypes.domain.testing.fixtures.createPaper
import org.orkg.contenttypes.input.testing.fixtures.publishPaperCommand
import org.orkg.graph.domain.Classes
import org.orkg.graph.domain.Predicates
import org.orkg.graph.domain.StatementId
import org.orkg.graph.input.CreateStatementUseCase
import org.orkg.graph.input.UnsafeResourceUseCases
import org.orkg.graph.input.UnsafeStatementUseCases
import org.orkg.graph.input.UpdateResourceUseCase

internal class PaperVersionHistoryUpdaterUnitTest : MockkBaseTest {
    private val unsafeStatementUseCases: UnsafeStatementUseCases = mockk()
    private val unsafeResourceUseCases: UnsafeResourceUseCases = mockk()

    private val paperVersionHistoryUpdater = PaperVersionHistoryUpdater(unsafeStatementUseCases, unsafeResourceUseCases)

    @Test
    fun `Given a paper publish command, it crates a new previous version statement and updates the previous version paper class labels`() {
        val paper = createPaper()
        val command = publishPaperCommand().copy(id = paper.id)
        val paperVersionId = ThingId("R165")
        val state = PublishPaperState(paper, paperVersionId = paperVersionId)

        every {
            unsafeStatementUseCases.create(
                CreateStatementUseCase.CreateCommand(
                    contributorId = command.contributorId,
                    subjectId = paper.id,
                    predicateId = Predicates.hasPublishedVersion,
                    objectId = paperVersionId,
                ),
            )
        } returns StatementId("S1")
        every { unsafeResourceUseCases.update(any()) } just runs

        paperVersionHistoryUpdater(command, state).asClue {
            it.paper shouldBe paper
            it.paperVersionId shouldBe paperVersionId
        }

        verify(exactly = 1) {
            unsafeStatementUseCases.create(
                CreateStatementUseCase.CreateCommand(
                    contributorId = command.contributorId,
                    subjectId = paper.id,
                    predicateId = Predicates.hasPublishedVersion,
                    objectId = paperVersionId,
                ),
            )
        }
        verify(exactly = 1) {
            unsafeResourceUseCases.update(
                UpdateResourceUseCase.UpdateCommand(
                    id = paper.versions.published.first().id,
                    contributorId = command.contributorId,
                    classes = setOf(Classes.paperVersion),
                ),
            )
        }
    }

    @Test
    fun `Given a paper publish command, when no previous published version exists, it only crates a new previous version statement`() {
        val paper = createPaper().let {
            it.copy(versions = it.versions.copy(it.versions.head, emptyList()))
        }
        val command = publishPaperCommand().copy(id = paper.id)
        val paperVersionId = ThingId("R165")
        val state = PublishPaperState(paper, paperVersionId = paperVersionId)

        every {
            unsafeStatementUseCases.create(
                CreateStatementUseCase.CreateCommand(
                    contributorId = command.contributorId,
                    subjectId = paper.id,
                    predicateId = Predicates.hasPublishedVersion,
                    objectId = paperVersionId,
                ),
            )
        } returns StatementId("S1")

        paperVersionHistoryUpdater(command, state).asClue {
            it.paper shouldBe paper
            it.paperVersionId shouldBe paperVersionId
        }

        verify(exactly = 1) {
            unsafeStatementUseCases.create(
                CreateStatementUseCase.CreateCommand(
                    contributorId = command.contributorId,
                    subjectId = paper.id,
                    predicateId = Predicates.hasPublishedVersion,
                    objectId = paperVersionId,
                ),
            )
        }
    }
}
