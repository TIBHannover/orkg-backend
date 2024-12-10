package org.orkg.contenttypes.domain.actions.literaturelists

import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.orkg.contenttypes.domain.LiteratureListNotModifiable
import org.orkg.contenttypes.domain.actions.UpdateLiteratureListState
import org.orkg.contenttypes.domain.testing.fixtures.createLiteratureList
import org.orkg.contenttypes.input.testing.fixtures.dummyUpdateLiteratureListCommand

internal class LiteratureListModifiableValidatorUnitTest {
    private val literatureListModifiableValidator = LiteratureListModifiableValidator()

    @Test
    fun `Given a literature list update command, when literature list is unpublished, it returns success`() {
        val command = dummyUpdateLiteratureListCommand()
        val state = UpdateLiteratureListState(literatureList = createLiteratureList())

        literatureListModifiableValidator(command, state)
    }

    @Test
    fun `Given a literature list update command, when literature list is published, it throws an exception`() {
        val command = dummyUpdateLiteratureListCommand()
        val state = UpdateLiteratureListState(literatureList = createLiteratureList().copy(published = true))

        assertThrows<LiteratureListNotModifiable> { literatureListModifiableValidator(command, state) }
    }
}
