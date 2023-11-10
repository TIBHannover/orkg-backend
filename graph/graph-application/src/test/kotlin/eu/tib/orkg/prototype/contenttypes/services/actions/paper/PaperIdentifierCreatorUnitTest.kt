package eu.tib.orkg.prototype.contenttypes.services.actions.paper

import eu.tib.orkg.prototype.contenttypes.services.actions.PaperState
import eu.tib.orkg.prototype.statements.testing.fixtures.createLiteral
import eu.tib.orkg.prototype.contenttypes.testing.fixtures.dummyCreatePaperCommand
import eu.tib.orkg.prototype.statements.api.LiteralUseCases
import eu.tib.orkg.prototype.statements.api.Predicates
import eu.tib.orkg.prototype.statements.api.StatementUseCases
import eu.tib.orkg.prototype.statements.domain.model.StatementId
import eu.tib.orkg.prototype.statements.domain.model.ThingId
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

class PaperIdentifierCreatorUnitTest {
    private val statementService: StatementUseCases = mockk()
    private val literalService: LiteralUseCases = mockk()

    private val paperResourceCreator = PaperIdentifierCreator(statementService, literalService)

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
        val state = PaperState(paperId = paperId)

        val doi = command.identifiers["doi"]!!
        val doiLiteral = createLiteral(label = doi)

        every { literalService.create(doi) } returns doiLiteral
        every { statementService.create(paperId, Predicates.hasDOI, doiLiteral.id) } returns StatementId("S435")

        val result = paperResourceCreator(command, state)

        result.asClue {
            it.tempIds.size shouldBe 0
            it.validatedIds.size shouldBe 0
            it.bakedStatements.size shouldBe 0
            it.authors.size shouldBe 0
            it.paperId shouldBe state.paperId
        }

        verify(exactly = 1) { literalService.create(doi) }
        verify(exactly = 1) { statementService.create(paperId, Predicates.hasDOI, doiLiteral.id) }
    }

    @Test
    fun `Given a paper create command, when an unknown identifier is specified, it does not create the identifier`() {
        val command = dummyCreatePaperCommand().copy(
            identifiers = mapOf("unknown" to "value")
        )
        val paperId = ThingId("Paper")
        val state = PaperState(paperId = paperId)

        val result = paperResourceCreator(command, state)

        result.asClue {
            it.tempIds.size shouldBe 0
            it.validatedIds.size shouldBe 0
            it.bakedStatements.size shouldBe 0
            it.authors.size shouldBe 0
            it.paperId shouldBe state.paperId
        }

        verify(exactly = 0) { literalService.create(any()) }
        verify(exactly = 0) { statementService.create(paperId, any(), any()) }
    }
}
