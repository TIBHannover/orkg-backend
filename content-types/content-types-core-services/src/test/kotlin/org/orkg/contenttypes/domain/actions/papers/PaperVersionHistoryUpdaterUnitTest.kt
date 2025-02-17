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
import org.orkg.graph.input.StatementUseCases
import org.orkg.graph.input.UnsafeStatementUseCases
import org.orkg.graph.testing.fixtures.createPredicate
import org.orkg.graph.testing.fixtures.createResource
import org.orkg.graph.testing.fixtures.createStatement

internal class PaperVersionHistoryUpdaterUnitTest : MockkBaseTest {
    private val statementService: StatementUseCases = mockk()
    private val unsafeStatementUseCases: UnsafeStatementUseCases = mockk()

    private val paperVersionHistoryUpdater = PaperVersionHistoryUpdater(statementService, unsafeStatementUseCases)

    @Test
    fun `Given a paper publish command, when paper does not yet have a published version, it creates a new hasPreviousVersion statement`() {
        val paper = createPaper()
        val command = publishPaperCommand().copy(id = paper.id)
        val statements = listOf(createStatement()).groupBy { it.subject.id }
        val paperVersionId = ThingId("R321")
        val state = PublishPaperState(
            paper = paper,
            statements = statements,
            paperVersionId = paperVersionId
        )

        every {
            unsafeStatementUseCases.create(
                CreateStatementUseCase.CreateCommand(
                    contributorId = command.contributorId,
                    subjectId = command.id,
                    predicateId = Predicates.hasPreviousVersion,
                    objectId = state.paperVersionId!!
                )
            )
        } returns StatementId("S1")

        paperVersionHistoryUpdater(command, state).asClue {
            it.paper shouldBe paper
            it.statements shouldBe statements
            it.paperVersionId shouldBe paperVersionId
        }

        verify(exactly = 1) {
            unsafeStatementUseCases.create(
                CreateStatementUseCase.CreateCommand(
                    contributorId = command.contributorId,
                    subjectId = command.id,
                    predicateId = Predicates.hasPreviousVersion,
                    objectId = state.paperVersionId!!
                )
            )
        }
    }

    /**
     * The method tests for the following changes in statements:
     *
     * Given: (paper)-&#91;hasPreviousVersion&#93;->(v1)
     *
     * Expected: (paper)-&#91;hasPreviousVersion&#93;->(v2)-&#91;hasPreviousVersion&#93;->(v1)
     */
    @Test
    fun `Given a paper publish command, when paper already has a published version, it inserts the new version between the paper and the already published version`() {
        val paper = createPaper()
        val command = publishPaperCommand().copy(id = paper.id)
        val resource = createResource(
            id = paper.id,
            label = paper.title,
            classes = setOf(Classes.paper)
        )
        val statementId = StatementId("S1")
        val previousVersionId = ThingId("R456")
        val statements = listOf(
            createStatement(
                id = statementId,
                subject = resource,
                predicate = createPredicate(Predicates.hasPreviousVersion),
                `object` = createResource(previousVersionId)
            )
        ).groupBy { it.subject.id }
        val paperVersionId = ThingId("R321")
        val state = PublishPaperState(
            paper = paper,
            statements = statements,
            paperVersionId = paperVersionId
        )

        every { statementService.deleteAllById(setOf(statementId)) } just runs
        every {
            unsafeStatementUseCases.create(
                CreateStatementUseCase.CreateCommand(
                    contributorId = command.contributorId,
                    subjectId = state.paperVersionId!!,
                    predicateId = Predicates.hasPreviousVersion,
                    objectId = previousVersionId
                )
            )
        } returns StatementId("S1")
        every {
            unsafeStatementUseCases.create(
                CreateStatementUseCase.CreateCommand(
                    contributorId = command.contributorId,
                    subjectId = command.id,
                    predicateId = Predicates.hasPreviousVersion,
                    objectId = state.paperVersionId!!
                )
            )
        } returns StatementId("S2")

        paperVersionHistoryUpdater(command, state).asClue {
            it.paper shouldBe paper
            it.statements shouldBe statements
            it.paperVersionId shouldBe paperVersionId
        }

        verify(exactly = 1) { statementService.deleteAllById(setOf(statementId)) }
        verify(exactly = 1) {
            unsafeStatementUseCases.create(
                CreateStatementUseCase.CreateCommand(
                    contributorId = command.contributorId,
                    subjectId = state.paperVersionId!!,
                    predicateId = Predicates.hasPreviousVersion,
                    objectId = previousVersionId
                )
            )
        }
        verify(exactly = 1) {
            unsafeStatementUseCases.create(
                CreateStatementUseCase.CreateCommand(
                    contributorId = command.contributorId,
                    subjectId = command.id,
                    predicateId = Predicates.hasPreviousVersion,
                    objectId = state.paperVersionId!!
                )
            )
        }
    }
}
