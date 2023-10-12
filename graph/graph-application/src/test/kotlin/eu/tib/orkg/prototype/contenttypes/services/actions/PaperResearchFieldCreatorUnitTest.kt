package eu.tib.orkg.prototype.contenttypes.services.actions

import eu.tib.orkg.prototype.contenttypes.testing.fixtures.dummyCreatePaperCommand
import eu.tib.orkg.prototype.statements.api.Predicates
import eu.tib.orkg.prototype.statements.api.StatementUseCases
import eu.tib.orkg.prototype.statements.domain.model.ThingId
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

class PaperResearchFieldCreatorUnitTest {
    private val statementService: StatementUseCases = mockk()

    private val paperResearchFieldCreator = PaperResearchFieldCreator(statementService)

    @BeforeEach
    fun resetState() {
        clearAllMocks()
    }

    @AfterEach
    fun verifyMocks() {
        confirmVerified(statementService)
    }

    @Test
    fun `Given a paper create command, when linking research fields, it returns success`() {
        val paperId = ThingId("R123")
        val command = dummyCreatePaperCommand()
        val state = PaperState(
            paperId = paperId
        )

        every {
            statementService.add(
                userId = command.contributorId,
                subject = state.paperId!!,
                predicate = Predicates.hasResearchField,
                `object` = command.researchFields[0]
            )
        } just runs

        val result = paperResearchFieldCreator(command, state)

        result.asClue {
            it.tempIds.size shouldBe 0
            it.validatedIds.size shouldBe 0
            it.bakedStatements.size shouldBe 0
            it.authors.size shouldBe 0
            it.paperId shouldBe state.paperId
        }

        verify(exactly = 1) {
            statementService.add(
                userId = command.contributorId,
                subject = state.paperId!!,
                predicate = Predicates.hasResearchField,
                `object` = command.researchFields[0]
            )
        }
    }
}
