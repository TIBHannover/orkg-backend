package org.orkg.contenttypes.domain.actions.literaturelists.sections

import io.mockk.every
import io.mockk.just
import io.mockk.mockk
import io.mockk.runs
import io.mockk.verify
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.orkg.common.ThingId
import org.orkg.common.testing.fixtures.MockkBaseTest
import org.orkg.contenttypes.domain.LiteratureListSectionTypeMismatch
import org.orkg.contenttypes.domain.UnrelatedLiteratureListSection
import org.orkg.contenttypes.domain.actions.UpdateLiteratureListSectionState
import org.orkg.contenttypes.domain.actions.literaturelists.AbstractLiteratureListSectionValidator
import org.orkg.contenttypes.domain.testing.fixtures.createLiteratureList
import org.orkg.contenttypes.input.AbstractLiteratureListSectionCommand
import org.orkg.contenttypes.input.testing.fixtures.updateLiteratureListListSectionCommand
import org.orkg.contenttypes.input.testing.fixtures.updateLiteratureListTextSectionCommand

internal class LiteratureListSectionUpdateValidatorUnitTest : MockkBaseTest {
    private val abstractLiteratureListSectionValidator: AbstractLiteratureListSectionValidator = mockk()

    private val literatureListSectionUpdateValidator = LiteratureListSectionUpdateValidator(abstractLiteratureListSectionValidator)

    @Test
    fun `Given a literature list section update command, when section is not related to the literature list, it throws an exception`() {
        val command = updateLiteratureListTextSectionCommand()
        val state = UpdateLiteratureListSectionState(literatureList = createLiteratureList())

        assertThrows<UnrelatedLiteratureListSection> { literatureListSectionUpdateValidator(command, state) }
    }

    @Test
    fun `Given a text section update command, when validation succeeds, it returns success`() {
        val literatureList = createLiteratureList()
        val state = UpdateLiteratureListSectionState(literatureList = literatureList)
        val command = updateLiteratureListTextSectionCommand().copy(literatureListSectionId = literatureList.sections.first().id)

        every { abstractLiteratureListSectionValidator.validate(any(), any()) } just runs

        literatureListSectionUpdateValidator(command, state)

        verify(exactly = 1) {
            abstractLiteratureListSectionValidator.validate(
                section = command as AbstractLiteratureListSectionCommand,
                validIds = mutableSetOf()
            )
        }
    }

    @Test
    fun `Given a text section update command, when types mismatch, it throws an exception`() {
        val literatureList = createLiteratureList()
        val state = UpdateLiteratureListSectionState(literatureList = literatureList)
        val command = updateLiteratureListTextSectionCommand().copy(literatureListSectionId = literatureList.sections.last().id)

        assertThrows<LiteratureListSectionTypeMismatch> { literatureListSectionUpdateValidator(command, state) }
    }

    @Test
    fun `Given a list section update command, when validation succeeds, it returns success`() {
        val literatureList = createLiteratureList()
        val state = UpdateLiteratureListSectionState(literatureList = literatureList)
        val command = updateLiteratureListListSectionCommand().copy(literatureListSectionId = literatureList.sections.last().id)

        every { abstractLiteratureListSectionValidator.validate(any(), any()) } just runs

        literatureListSectionUpdateValidator(command, state)

        verify(exactly = 1) {
            abstractLiteratureListSectionValidator.validate(
                section = command as AbstractLiteratureListSectionCommand,
                validIds = mutableSetOf(ThingId("R154686"), ThingId("R6416"))
            )
        }
    }

    @Test
    fun `Given a list section update command, when types mismatch, it throws an exception`() {
        val literatureList = createLiteratureList()
        val state = UpdateLiteratureListSectionState(literatureList = literatureList)
        val command = updateLiteratureListListSectionCommand().copy(literatureListSectionId = literatureList.sections.first().id)

        assertThrows<LiteratureListSectionTypeMismatch> { literatureListSectionUpdateValidator(command, state) }
    }
}
