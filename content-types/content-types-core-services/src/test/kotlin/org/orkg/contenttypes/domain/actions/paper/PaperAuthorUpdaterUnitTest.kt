package org.orkg.contenttypes.domain.actions.paper

import io.mockk.clearAllMocks
import io.mockk.confirmVerified
import io.mockk.every
import io.mockk.just
import io.mockk.mockk
import io.mockk.runs
import io.mockk.verify
import java.util.*
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.orkg.contenttypes.domain.actions.AuthorUpdater
import org.orkg.contenttypes.domain.actions.UpdatePaperState
import org.orkg.contenttypes.input.testing.fixtures.dummyUpdatePaperCommand

class PaperAuthorUpdaterUnitTest {
    private val authorUpdater: AuthorUpdater = mockk()

    private val paperAuthorUpdater = PaperAuthorUpdater(authorUpdater)

    @BeforeEach
    fun resetState() {
        clearAllMocks()
    }

    @AfterEach
    fun verifyMocks() {
        confirmVerified(authorUpdater)
    }

    @Test
    fun `Given a paper update command, it updates the authors`() {
        val command = dummyUpdatePaperCommand()
        val state = UpdatePaperState()

        every { authorUpdater.update(command.contributorId, state.authors, command.paperId) } just runs

        paperAuthorUpdater(command, state)

        verify(exactly = 1) { authorUpdater.update(command.contributorId, state.authors, command.paperId) }
    }

    @Test
    fun `Given a paper update command, when author list is null, it does nothing`() {
        val command = dummyUpdatePaperCommand().copy(
            authors = null
        )
        val state = UpdatePaperState()

        paperAuthorUpdater(command, state)
    }
}
