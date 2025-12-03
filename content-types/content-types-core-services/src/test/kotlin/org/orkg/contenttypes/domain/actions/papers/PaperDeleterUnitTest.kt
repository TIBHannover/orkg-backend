package org.orkg.contenttypes.domain.actions.papers

import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.just
import io.mockk.mockk
import io.mockk.runs
import io.mockk.verify
import org.junit.jupiter.api.Test
import org.orkg.common.ThingId
import org.orkg.common.testing.fixtures.MockkBaseTest
import org.orkg.contenttypes.domain.actions.DeletePaperState
import org.orkg.contenttypes.domain.testing.fixtures.createPaper
import org.orkg.contenttypes.input.testing.fixtures.deletePaperCommand
import org.orkg.graph.domain.Classes
import org.orkg.graph.domain.Predicates
import org.orkg.graph.domain.StatementId
import org.orkg.graph.input.UnsafeResourceUseCases
import org.orkg.graph.input.UnsafeStatementUseCases
import org.orkg.graph.output.StatementRepository
import org.orkg.graph.testing.fixtures.createPredicate
import org.orkg.graph.testing.fixtures.createResource
import org.orkg.graph.testing.fixtures.createStatement

internal class PaperDeleterUnitTest : MockkBaseTest {
    private val unsafeResourceUseCases: UnsafeResourceUseCases = mockk()
    private val unsafeStatementUseCases: UnsafeStatementUseCases = mockk()
    private val statementRepository: StatementRepository = mockk()

    private val paperDeleter = PaperDeleter(unsafeResourceUseCases, unsafeStatementUseCases, statementRepository)

    @Test
    fun `Given a paper delete command, when deleting, it deletes the paper`() {
        val paper = createResource(classes = setOf(Classes.paper))
        val command = deletePaperCommand().copy(paperId = paper.id)
        val authorList = createResource(id = ThingId("R546"), classes = setOf(Classes.list))
        val contribution = createResource(id = ThingId("R1563"), classes = setOf(Classes.contribution))
        val state = DeletePaperState().copy(
            paper = paper,
            statements = listOf(
                createStatement(
                    id = StatementId("S1"),
                    subject = paper,
                    predicate = createPredicate(Predicates.hasAuthors),
                    `object` = authorList
                ),
                createStatement(
                    id = StatementId("S2"),
                    subject = paper
                ),
                createStatement(
                    id = StatementId("S3"),
                    subject = authorList,
                    predicate = createPredicate(Predicates.hasListElement),
                ),
                createStatement(
                    id = StatementId("S4"),
                    subject = paper,
                    predicate = createPredicate(Predicates.hasContribution),
                    `object` = contribution,
                ),
                createStatement(
                    id = StatementId("S5"),
                    subject = contribution,
                ),
            ).groupBy { it.subject.id }
        )
        val statementsToDelete = setOf(
            StatementId("S1"),
            StatementId("S2"),
            StatementId("S3"),
            StatementId("S4"),
            StatementId("S5"),
        )

        every { statementRepository.countIncomingStatementsById(authorList.id) } returns 1
        every { unsafeStatementUseCases.deleteAllById(statementsToDelete) } just runs
        every { unsafeResourceUseCases.delete(any(), command.contributorId) } just runs

        paperDeleter(command, state) shouldBe state

        verify(exactly = 1) { statementRepository.countIncomingStatementsById(authorList.id) }
        verify(exactly = 1) { unsafeStatementUseCases.deleteAllById(statementsToDelete) }
        verify(exactly = 1) { unsafeResourceUseCases.delete(command.paperId, command.contributorId) }
        verify(exactly = 1) { unsafeResourceUseCases.delete(authorList.id, command.contributorId) }
        verify(exactly = 1) { unsafeResourceUseCases.delete(contribution.id, command.contributorId) }
    }

    @Test
    fun `Given a paper delete command, when author list does not exist, it deletes the paper`() {
        val paper = createResource(classes = setOf(Classes.paper))
        val command = deletePaperCommand().copy(paperId = paper.id)
        val contribution = createResource(id = ThingId("R1563"), classes = setOf(Classes.contribution))
        val state = DeletePaperState().copy(
            paper = paper,
            statements = listOf(
                createStatement(
                    id = StatementId("S2"),
                    subject = paper
                ),
                createStatement(
                    id = StatementId("S4"),
                    subject = paper,
                    predicate = createPredicate(Predicates.hasContribution),
                    `object` = contribution,
                ),
                createStatement(
                    id = StatementId("S5"),
                    subject = contribution,
                ),
            ).groupBy { it.subject.id }
        )
        val statementsToDelete = setOf(StatementId("S2"), StatementId("S4"), StatementId("S5"))

        every { unsafeStatementUseCases.deleteAllById(statementsToDelete) } just runs
        every { unsafeResourceUseCases.delete(any(), command.contributorId) } just runs

        paperDeleter(command, state) shouldBe state

        verify(exactly = 1) { unsafeStatementUseCases.deleteAllById(statementsToDelete) }
        verify(exactly = 1) { unsafeResourceUseCases.delete(command.paperId, command.contributorId) }
        verify(exactly = 1) { unsafeResourceUseCases.delete(contribution.id, command.contributorId) }
    }

    @Test
    fun `Given a paper delete command, when author list is used by another thing, it does not delete the author list`() {
        val paper = createResource(classes = setOf(Classes.paper))
        val command = deletePaperCommand().copy(paperId = paper.id)
        val authorList = createResource(id = ThingId("R546"), classes = setOf(Classes.list))
        val contribution = createResource(id = ThingId("R1563"), classes = setOf(Classes.contribution))
        val state = DeletePaperState().copy(
            paper = paper,
            statements = listOf(
                createStatement(
                    id = StatementId("S1"),
                    subject = paper,
                    predicate = createPredicate(Predicates.hasAuthors),
                    `object` = authorList
                ),
                createStatement(
                    id = StatementId("S2"),
                    subject = paper
                ),
                createStatement(
                    id = StatementId("S4"),
                    subject = paper,
                    predicate = createPredicate(Predicates.hasContribution),
                    `object` = contribution,
                ),
                createStatement(
                    id = StatementId("S5"),
                    subject = contribution,
                ),
            ).groupBy { it.subject.id }
        )
        val statementsToDelete = setOf(StatementId("S1"), StatementId("S2"), StatementId("S4"), StatementId("S5"))

        every { statementRepository.countIncomingStatementsById(authorList.id) } returns 2
        every { unsafeStatementUseCases.deleteAllById(statementsToDelete) } just runs
        every { unsafeResourceUseCases.delete(any(), command.contributorId) } just runs

        paperDeleter(command, state) shouldBe state

        verify(exactly = 1) { statementRepository.countIncomingStatementsById(authorList.id) }
        verify(exactly = 1) { unsafeStatementUseCases.deleteAllById(statementsToDelete) }
        verify(exactly = 1) { unsafeResourceUseCases.delete(command.paperId, command.contributorId) }
        verify(exactly = 1) { unsafeResourceUseCases.delete(contribution.id, command.contributorId) }
    }

    @Test
    fun `Given a paper delete command, when paper is null, it does nothing`() {
        val paper = createPaper()
        val command = deletePaperCommand().copy(paperId = paper.id)
        val state = DeletePaperState()

        paperDeleter(command, state) shouldBe state
    }
}
