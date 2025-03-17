package org.orkg.contenttypes.domain.actions.papers

import io.mockk.every
import io.mockk.just
import io.mockk.mockk
import io.mockk.runs
import io.mockk.verify
import org.junit.jupiter.api.Test
import org.orkg.common.testing.fixtures.MockkBaseTest
import org.orkg.contenttypes.domain.PaperAlreadyExists
import org.orkg.contenttypes.domain.actions.IdentifierValidator
import org.orkg.contenttypes.domain.actions.UpdatePaperState
import org.orkg.contenttypes.domain.testing.fixtures.createPaper
import org.orkg.contenttypes.input.testing.fixtures.updatePaperCommand
import org.orkg.graph.domain.Classes

internal class PaperIdentifierUpdateValidatorUnitTest : MockkBaseTest {
    private val identifierValidator: IdentifierValidator = mockk()

    private val paperIdentifierUpdateValidator = PaperIdentifierUpdateValidator(identifierValidator)

    @Test
    fun `Given a paper update command, when validating its identifiers, it returns success`() {
        val command = updatePaperCommand()
        val state = UpdatePaperState(paper = createPaper())

        every {
            identifierValidator.validate(
                identifiers = command.identifiers!!,
                `class` = Classes.paper,
                subjectId = command.paperId,
                exceptionFactory = PaperAlreadyExists::withIdentifier
            )
        } just runs

        paperIdentifierUpdateValidator(command, state)

        verify(exactly = 1) {
            identifierValidator.validate(
                identifiers = command.identifiers!!,
                `class` = Classes.paper,
                subjectId = command.paperId,
                exceptionFactory = PaperAlreadyExists::withIdentifier
            )
        }
    }

    @Test
    fun `Given a paper update command, when new identifiers are identical to old identifiers, it does nothing`() {
        val command = updatePaperCommand()
        val state = UpdatePaperState(paper = createPaper().copy(identifiers = command.identifiers!!))

        paperIdentifierUpdateValidator(command, state)
    }

    @Test
    fun `Given a paper update command, when no new identifiers are set, it does nothing`() {
        val command = updatePaperCommand().copy(identifiers = null)
        val state = UpdatePaperState(paper = createPaper())

        paperIdentifierUpdateValidator(command, state)
    }
}
