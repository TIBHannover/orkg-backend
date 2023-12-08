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
import org.orkg.contenttypes.domain.actions.UpdatePaperState
import org.orkg.contenttypes.domain.testing.fixtures.createDummyPaper
import org.orkg.contenttypes.input.testing.fixtures.dummyUpdatePaperCommand
import org.orkg.graph.domain.Classes
import org.orkg.graph.domain.Predicates
import org.orkg.graph.domain.StatementId
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

        val doi = command.identifiers!!["doi"]!!
        val doiLiteral = createLiteral(label = doi)

        every { literalService.create(doi) } returns doiLiteral
        every { statementService.create(command.contributorId, paperId, Predicates.hasDOI, doiLiteral.id) } returns StatementId("S435")

        val result = paperIdentifierUpdater(command, state)

        result.asClue {
            it.paper shouldBe paper
            it.authors.size shouldBe 0
        }

        verify(exactly = 1) { literalService.create(doi) }
        verify(exactly = 1) { statementService.create(command.contributorId, paperId, Predicates.hasDOI, doiLiteral.id) }
    }

    @Test
    fun `Given a paper update command, it updates an already existing paper identifier`() {
        val oldDoi = "10.1234/56790"
        val newDoi = "10.1234/56789"
        val command = dummyUpdatePaperCommand().copy(
            identifiers = mapOf("doi" to newDoi)
        )
        val paper = createDummyPaper().copy(
            identifiers = mapOf("doi" to oldDoi)
        )
        val paperId = paper.id
        val state = UpdatePaperState(paper = paper)

        val oldDoiLiteral = createLiteral(label = oldDoi)
        val newDoiLiteral = createLiteral(label = newDoi)
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
        every { literalService.create(newDoi) } returns newDoiLiteral
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
        verify(exactly = 1) { literalService.create(newDoi) }
        verify(exactly = 1) { statementService.create(command.contributorId, paperId, Predicates.hasDOI, newDoiLiteral.id) }
    }

    @Test
    fun `Given a paper update command, when an unknown identifier is specified, it does not create the identifier`() {
        val command = dummyUpdatePaperCommand().copy(
            identifiers = mapOf("unknown" to "value")
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
}