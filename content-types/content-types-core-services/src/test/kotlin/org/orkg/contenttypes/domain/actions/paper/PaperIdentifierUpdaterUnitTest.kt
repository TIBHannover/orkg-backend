package org.orkg.contenttypes.domain.actions.paper

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
import org.orkg.common.PageRequests
import org.orkg.common.ThingId
import org.orkg.contenttypes.domain.actions.UpdatePaperState
import org.orkg.contenttypes.domain.testing.fixtures.createDummyPaper
import org.orkg.contenttypes.input.testing.fixtures.dummyUpdatePaperCommand
import org.orkg.graph.domain.Classes
import org.orkg.graph.domain.Predicates
import org.orkg.graph.domain.StatementId
import org.orkg.graph.input.CreateLiteralUseCase
import org.orkg.graph.input.LiteralUseCases
import org.orkg.graph.input.StatementUseCases
import org.orkg.graph.testing.fixtures.createLiteral
import org.orkg.graph.testing.fixtures.createPredicate
import org.orkg.graph.testing.fixtures.createResource
import org.orkg.graph.testing.fixtures.createStatement
import org.orkg.testing.pageOf

class PaperIdentifierUpdaterUnitTest {
    private val statementService: StatementUseCases = mockk()
    private val literalService: LiteralUseCases = mockk()

    private val paperIdentifierUpdater = PaperIdentifierUpdater(statementService, literalService)

    @BeforeEach
    fun resetState() {
        clearAllMocks()
    }

    @AfterEach
    fun verifyMocks() {
        confirmVerified(statementService, literalService)
    }

    @Test
    fun `Given a paper update command, it crates new paper identifiers`() {
        val command = dummyUpdatePaperCommand()
        val paper = createDummyPaper().copy(
            identifiers = emptyMap()
        )
        val paperId = paper.id
        val state = UpdatePaperState(paper = paper)

        val doi = command.identifiers!!["doi"]!!.first()
        val doiLiteralId = ThingId("L1")

        every {
            literalService.create(
                CreateLiteralUseCase.CreateCommand(
                    contributorId = command.contributorId,
                    label = doi
                )
            )
        } returns doiLiteralId
        every { statementService.create(command.contributorId, paperId, Predicates.hasDOI, doiLiteralId) } returns StatementId("S435")

        val result = paperIdentifierUpdater(command, state)

        result.asClue {
            it.paper shouldBe paper
            it.authors.size shouldBe 0
        }

        verify(exactly = 1) {
            literalService.create(
                CreateLiteralUseCase.CreateCommand(
                    contributorId = command.contributorId,
                    label = doi
                )
            )
        }
        verify(exactly = 1) { statementService.create(command.contributorId, paperId, Predicates.hasDOI, doiLiteralId) }
    }

    @Test
    fun `Given a paper update command, it updates an already existing paper identifier`() {
        val oldDoi = "10.1234/56790"
        val newDoi = "10.1234/56789"
        val command = dummyUpdatePaperCommand().copy(
            identifiers = mapOf("doi" to listOf(newDoi))
        )
        val paper = createDummyPaper().copy(
            identifiers = mapOf("doi" to listOf(oldDoi))
        )
        val paperId = paper.id
        val state = UpdatePaperState(paper = paper)

        val oldDoiLiteral = createLiteral(ThingId("L1"), label = oldDoi)
        val newDoiLiteral = createLiteral(ThingId("L2"), label = newDoi)
        val statementId = StatementId("S1")

        every {
            statementService.findAllBySubjectAndPredicate(
                subjectId = paperId,
                predicateId = Predicates.hasDOI,
                pagination = PageRequests.ALL
            )
        } returns pageOf(
            createStatement(
                id = statementId,
                subject = createResource(paperId, classes = setOf(Classes.paper)),
                predicate = createPredicate(Predicates.hasDOI),
                `object` = oldDoiLiteral
            )
        )
        every { statementService.delete(statementId) } just runs
        every {
            literalService.create(
                CreateLiteralUseCase.CreateCommand(
                    contributorId = command.contributorId,
                    label = newDoi
                )
            )
        } returns newDoiLiteral.id
        every { statementService.create(command.contributorId, paperId, Predicates.hasDOI, newDoiLiteral.id) } returns StatementId("S435")

        val result = paperIdentifierUpdater(command, state)

        result.asClue {
            it.paper shouldBe paper
            it.authors.size shouldBe 0
        }

        verify(exactly = 1) {
            statementService.findAllBySubjectAndPredicate(
                subjectId = paperId,
                predicateId = Predicates.hasDOI,
                pagination = PageRequests.ALL
            )
        }
        verify(exactly = 1) { statementService.delete(statementId) }
        verify(exactly = 1) {
            literalService.create(
                CreateLiteralUseCase.CreateCommand(
                    contributorId = command.contributorId,
                    label = newDoi
                )
            )
        }
        verify(exactly = 1) { statementService.create(command.contributorId, paperId, Predicates.hasDOI, newDoiLiteral.id) }
    }

    @Test
    fun `Given a paper update command, when an unknown identifier is specified, it does not create the identifier`() {
        val command = dummyUpdatePaperCommand().copy(
            identifiers = mapOf("unknown" to listOf("value"))
        )
        val paper = createDummyPaper().copy(
            identifiers = emptyMap()
        )
        val paperId = paper.id
        val state = UpdatePaperState(paper = paper)

        val result = paperIdentifierUpdater(command, state)

        result.asClue {
            it.paper shouldBe paper
            it.authors.size shouldBe 0
        }

        verify(exactly = 0) { literalService.create(any()) }
        verify(exactly = 0) { statementService.create(any(), paperId, any(), any()) }
    }

    @Test
    fun `Given a paper update command, when new identifiers are identical to old identifiers, it does nothing`() {
        val command = dummyUpdatePaperCommand()
        val state = UpdatePaperState(paper = createDummyPaper().copy(identifiers = command.identifiers!!))

        paperIdentifierUpdater(command, state)
    }

    @Test
    fun `Given a paper update command, when no new identifiers are set, it does nothing`() {
        val command = dummyUpdatePaperCommand().copy(identifiers = null)
        val state = UpdatePaperState(paper = createDummyPaper())

        paperIdentifierUpdater(command, state)
    }
}
