package org.orkg.contenttypes.domain.actions.papers

import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.orkg.contenttypes.domain.PaperNotModifiable
import org.orkg.contenttypes.domain.actions.UpdatePaperState
import org.orkg.contenttypes.domain.testing.fixtures.createDummyPaper
import org.orkg.contenttypes.input.testing.fixtures.dummyUpdatePaperCommand

internal class PaperModifiableValidatorUnitTest {
    private val paperModifiableValidator = PaperModifiableValidator()

    @Test
    fun `Given a paper update command, when paper is modifiable, it returns success`() {
        val command = dummyUpdatePaperCommand()
        val state = UpdatePaperState(paper = createDummyPaper())

        paperModifiableValidator(command, state)
    }

    @Test
    fun `Given a paper update command, when paper is not modifiable, it throws an exception`() {
        val command = dummyUpdatePaperCommand()
        val state = UpdatePaperState(paper = createDummyPaper().copy(modifiable = false))

        assertThrows<PaperNotModifiable> { paperModifiableValidator(command, state) }
    }
}
