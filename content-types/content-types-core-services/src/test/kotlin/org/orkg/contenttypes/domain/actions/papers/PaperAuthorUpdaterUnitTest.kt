package org.orkg.contenttypes.domain.actions.papers

import io.mockk.every
import io.mockk.just
import io.mockk.mockk
import io.mockk.runs
import io.mockk.verify
import org.junit.jupiter.api.Test
import org.orkg.common.testing.fixtures.MockkBaseTest
import org.orkg.contenttypes.domain.actions.AuthorUpdater
import org.orkg.contenttypes.domain.actions.UpdatePaperState
import org.orkg.contenttypes.domain.testing.fixtures.createPaper
import org.orkg.contenttypes.input.testing.fixtures.updatePaperCommand

internal class PaperAuthorUpdaterUnitTest : MockkBaseTest {
    private val authorUpdater: AuthorUpdater = mockk()

    private val paperAuthorUpdater = PaperAuthorUpdater(authorUpdater)

    @Test
    fun `Given a paper update command, it updates the authors`() {
        val command = updatePaperCommand()
        val state = UpdatePaperState(paper = createPaper())

        every { authorUpdater.update(command.contributorId, state.authors, command.paperId) } just runs

        paperAuthorUpdater(command, state)

        verify(exactly = 1) { authorUpdater.update(command.contributorId, state.authors, command.paperId) }
    }

    @Test
    fun `Given a paper update command, when new author list is identical to new author list, it does nothing`() {
        val command = updatePaperCommand()
        val state = UpdatePaperState(paper = createPaper().copy(authors = command.authors!!))

        paperAuthorUpdater(command, state)
    }

    @Test
    fun `Given a paper update command, when no author list is set, it does nothing`() {
        val command = updatePaperCommand().copy(authors = null)
        val state = UpdatePaperState()

        paperAuthorUpdater(command, state)
    }
}
