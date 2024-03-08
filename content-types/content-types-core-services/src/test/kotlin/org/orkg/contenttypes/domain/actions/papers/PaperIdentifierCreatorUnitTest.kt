package org.orkg.contenttypes.domain.actions.papers

import io.kotest.assertions.asClue
import io.kotest.matchers.shouldBe
import io.mockk.clearAllMocks
import io.mockk.confirmVerified
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.orkg.common.ThingId
import org.orkg.contenttypes.domain.actions.CreatePaperState
import org.orkg.contenttypes.input.testing.fixtures.dummyCreatePaperCommand
import org.orkg.graph.domain.Predicates
import org.orkg.graph.domain.StatementId
import org.orkg.graph.input.CreateLiteralUseCase.CreateCommand
import org.orkg.graph.input.LiteralUseCases
import org.orkg.graph.input.StatementUseCases

class PaperIdentifierCreatorUnitTest {
    private val statementService: StatementUseCases = mockk()
    private val literalService: LiteralUseCases = mockk()

    private val paperIdentifierCreator = PaperIdentifierCreator(statementService, literalService)

    @BeforeEach
    fun resetState() {
        clearAllMocks()
    }

    @AfterEach
    fun verifyMocks() {
        confirmVerified(statementService, literalService)
    }

    @Test
    fun `Given a paper create command, it crates new paper identifiers`() {
        val command = dummyCreatePaperCommand()
        val paperId = ThingId("Paper")
        val state = CreatePaperState(paperId = paperId)

        val doi = command.identifiers["doi"]!!.first()
        val doiLiteralId = ThingId("L1")

        every {
            literalService.create(
                CreateCommand(
                    contributorId = command.contributorId,
                    label = doi
                )
            )
        } returns doiLiteralId
        every { statementService.create(command.contributorId, paperId, Predicates.hasDOI, doiLiteralId) } returns StatementId("S435")

        val result = paperIdentifierCreator(command, state)

        result.asClue {
            it.tempIds.size shouldBe 0
            it.validatedIds.size shouldBe 0
            it.bakedStatements.size shouldBe 0
            it.authors.size shouldBe 0
            it.paperId shouldBe state.paperId
        }

        verify(exactly = 1) {
            literalService.create(
                CreateCommand(
                    contributorId = command.contributorId,
                    label = doi
                )
            )
        }
        verify(exactly = 1) { statementService.create(command.contributorId, paperId, Predicates.hasDOI, doiLiteralId) }
    }

    @Test
    fun `Given a paper create command, when an unknown identifier is specified, it does not create the identifier`() {
        val command = dummyCreatePaperCommand().copy(
            identifiers = mapOf("unknown" to listOf("value"))
        )
        val paperId = ThingId("Paper")
        val state = CreatePaperState(paperId = paperId)

        val result = paperIdentifierCreator(command, state)

        result.asClue {
            it.tempIds.size shouldBe 0
            it.validatedIds.size shouldBe 0
            it.bakedStatements.size shouldBe 0
            it.authors.size shouldBe 0
            it.paperId shouldBe state.paperId
        }

        verify(exactly = 0) { literalService.create(any()) }
        verify(exactly = 0) { statementService.create(any(), paperId, any(), any()) }
    }
}
