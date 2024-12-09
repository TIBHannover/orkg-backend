package org.orkg.contenttypes.domain.actions.papers

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
import org.orkg.contenttypes.domain.actions.PublishPaperState
import org.orkg.contenttypes.domain.testing.fixtures.createDummyPaper
import org.orkg.contenttypes.input.testing.fixtures.createPaperPublishCommand
import org.orkg.graph.domain.Classes
import org.orkg.graph.domain.Predicates
import org.orkg.graph.domain.StatementId
import org.orkg.graph.input.StatementUseCases
import org.orkg.graph.testing.fixtures.createPredicate
import org.orkg.graph.testing.fixtures.createResource
import org.orkg.graph.testing.fixtures.createStatement

internal class PaperVersionHistoryUpdaterUnitTest {
    private val statementService: StatementUseCases = mockk()

    private val paperVersionHistoryUpdater = PaperVersionHistoryUpdater(statementService)

    @BeforeEach
    fun resetState() {
        clearAllMocks()
    }

    @AfterEach
    fun verifyMocks() {
        confirmVerified(statementService)
    }

    @Test
    fun `Given a paper publish command, when paper does not yet have a published version, it creates a new hasPreviousVersion statement`() {
        val paper = createDummyPaper()
        val command = createPaperPublishCommand().copy(id = paper.id)
        val statements = listOf(createStatement()).groupBy { it.subject.id }
        val paperVersionId = ThingId("R321")
        val state = PublishPaperState(
            paper = paper,
            statements = statements,
            paperVersionId = paperVersionId
        )

        every {
            statementService.add(
                userId = command.contributorId,
                subject = command.id,
                predicate = Predicates.hasPreviousVersion,
                `object` = state.paperVersionId!!
            )
        } just runs

        paperVersionHistoryUpdater(command, state).asClue {
            it.paper shouldBe paper
            it.statements shouldBe statements
            it.paperVersionId shouldBe paperVersionId
        }

        verify(exactly = 1) {
            statementService.add(
                userId = command.contributorId,
                subject = command.id,
                predicate = Predicates.hasPreviousVersion,
                `object` = state.paperVersionId!!
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
        val paper = createDummyPaper()
        val command = createPaperPublishCommand().copy(id = paper.id)
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

        every { statementService.delete(setOf(statementId)) } just runs
        every {
            statementService.add(
                userId = command.contributorId,
                subject = state.paperVersionId!!,
                predicate = Predicates.hasPreviousVersion,
                `object` = previousVersionId
            )
        } just runs
        every {
            statementService.add(
                userId = command.contributorId,
                subject = command.id,
                predicate = Predicates.hasPreviousVersion,
                `object` = state.paperVersionId!!
            )
        } just runs

        paperVersionHistoryUpdater(command, state).asClue {
            it.paper shouldBe paper
            it.statements shouldBe statements
            it.paperVersionId shouldBe paperVersionId
        }

        verify(exactly = 1) { statementService.delete(setOf(statementId)) }
        verify(exactly = 1) {
            statementService.add(
                userId = command.contributorId,
                subject = state.paperVersionId!!,
                predicate = Predicates.hasPreviousVersion,
                `object` = previousVersionId
            )
        }
        verify(exactly = 1) {
            statementService.add(
                userId = command.contributorId,
                subject = command.id,
                predicate = Predicates.hasPreviousVersion,
                `object` = state.paperVersionId!!
            )
        }
    }
}
