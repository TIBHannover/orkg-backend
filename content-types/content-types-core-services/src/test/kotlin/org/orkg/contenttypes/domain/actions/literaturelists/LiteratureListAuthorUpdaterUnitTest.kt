package org.orkg.contenttypes.domain.actions.literaturelists

import io.kotest.assertions.asClue
import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.just
import io.mockk.mockk
import io.mockk.runs
import io.mockk.verify
import org.junit.jupiter.api.Test
import org.orkg.common.testing.fixtures.MockkBaseTest
import org.orkg.contenttypes.domain.actions.AuthorUpdater
import org.orkg.contenttypes.domain.actions.UpdateLiteratureListState
import org.orkg.contenttypes.domain.testing.fixtures.createLiteratureList
import org.orkg.contenttypes.input.testing.fixtures.dummyUpdateLiteratureListCommand

internal class LiteratureListAuthorUpdaterUnitTest : MockkBaseTest {
    private val authorUpdater: AuthorUpdater = mockk()

    private val literatureListAuthorUpdater = LiteratureListAuthorUpdater(authorUpdater)

    @Test
    fun `Given a literature list update command, it updates the authors`() {
        val command = dummyUpdateLiteratureListCommand()
        val literatureList = createLiteratureList()
        val state = UpdateLiteratureListState(literatureList = literatureList)

        every { authorUpdater.update(command.contributorId, state.authors, command.literatureListId) } just runs

        literatureListAuthorUpdater(command, state).asClue {
            it.literatureList shouldBe literatureList
            it.statements shouldBe state.statements
            it.authors shouldBe state.authors
        }

        verify(exactly = 1) { authorUpdater.update(command.contributorId, state.authors, command.literatureListId) }
    }

    @Test
    fun `Given a literature list update command, when new author list is identical to new author list, it does nothing`() {
        val command = dummyUpdateLiteratureListCommand()
        val literatureList = createLiteratureList().copy(authors = command.authors!!)
        val state = UpdateLiteratureListState(literatureList = literatureList)

        literatureListAuthorUpdater(command, state).asClue {
            it.literatureList shouldBe literatureList
            it.statements shouldBe state.statements
            it.authors shouldBe state.authors
        }
    }

    @Test
    fun `Given a literature list update command, when no author list is set, it does nothing`() {
        val command = dummyUpdateLiteratureListCommand().copy(authors = null)
        val state = UpdateLiteratureListState()

        literatureListAuthorUpdater(command, state).asClue {
            it.literatureList shouldBe state.literatureList
            it.statements shouldBe state.statements
            it.authors shouldBe state.authors
        }
    }
}
