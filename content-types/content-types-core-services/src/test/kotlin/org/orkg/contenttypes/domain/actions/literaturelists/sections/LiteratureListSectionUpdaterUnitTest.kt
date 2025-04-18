package org.orkg.contenttypes.domain.actions.literaturelists.sections

import io.kotest.matchers.types.shouldBeInstanceOf
import io.mockk.every
import io.mockk.just
import io.mockk.mockk
import io.mockk.runs
import io.mockk.verify
import org.junit.jupiter.api.Test
import org.orkg.common.ContributorId
import org.orkg.common.ThingId
import org.orkg.common.testing.fixtures.MockkBaseTest
import org.orkg.contenttypes.domain.LiteratureListListSection
import org.orkg.contenttypes.domain.LiteratureListSection
import org.orkg.contenttypes.domain.LiteratureListTextSection
import org.orkg.contenttypes.domain.actions.UpdateLiteratureListSectionCommand
import org.orkg.contenttypes.domain.actions.UpdateLiteratureListSectionState
import org.orkg.contenttypes.domain.actions.literaturelists.AbstractLiteratureListSectionUpdater
import org.orkg.contenttypes.domain.testing.fixtures.createLiteratureList
import org.orkg.contenttypes.domain.testing.fixtures.toGroupedStatements
import org.orkg.contenttypes.input.AbstractLiteratureListListSectionCommand
import org.orkg.contenttypes.input.AbstractLiteratureListTextSectionCommand
import org.orkg.contenttypes.input.UpdateLiteratureListSectionUseCase.UpdateListSectionCommand
import org.orkg.contenttypes.input.UpdateLiteratureListSectionUseCase.UpdateTextSectionCommand
import org.orkg.contenttypes.input.testing.fixtures.toCommandEntry
import java.util.UUID

internal class LiteratureListSectionUpdaterUnitTest : MockkBaseTest {
    private val abstractLiteratureListSectionUpdater: AbstractLiteratureListSectionUpdater = mockk()

    private val literatureListSectionUpdateValidator = LiteratureListSectionUpdater(abstractLiteratureListSectionUpdater)

    @Test
    fun `Given a text section update command, when contents are equal, it does nothing`() {
        val contributorId = ContributorId(UUID.randomUUID())
        val literatureList = createLiteratureList()
        val state = UpdateLiteratureListSectionState(literatureList = literatureList)
        val command = literatureList.sections.first()
            .toUpdateListSectionCommand(contributorId, literatureList.id)
            .shouldBeInstanceOf<UpdateTextSectionCommand>()

        literatureListSectionUpdateValidator(command, state)
    }

    @Test
    fun `Given a text section update command, when contents have changed, it updates the text section`() {
        val contributorId = ContributorId(UUID.randomUUID())
        val literatureList = createLiteratureList()
        val oldSection = literatureList.sections.first()
        val state = UpdateLiteratureListSectionState(
            literatureList = literatureList,
            statements = oldSection.toGroupedStatements()
        )
        val command = oldSection.toUpdateListSectionCommand(contributorId, literatureList.id)
            .shouldBeInstanceOf<UpdateTextSectionCommand>()
            .copy(text = "updated text")

        every { abstractLiteratureListSectionUpdater.updateTextSection(any(), any(), any(), any()) } just runs

        literatureListSectionUpdateValidator(command, state)

        verify(exactly = 1) {
            abstractLiteratureListSectionUpdater.updateTextSection(
                contributorId = command.contributorId,
                newSection = command as AbstractLiteratureListTextSectionCommand,
                oldSection = oldSection as LiteratureListTextSection,
                statements = state.statements
            )
        }
    }

    @Test
    fun `Given a list section update command, when contents are equal, it does nothing`() {
        val contributorId = ContributorId(UUID.randomUUID())
        val literatureList = createLiteratureList()
        val state = UpdateLiteratureListSectionState(literatureList = literatureList)
        val command = literatureList.sections.last()
            .toUpdateListSectionCommand(contributorId, literatureList.id)
            .shouldBeInstanceOf<UpdateListSectionCommand>()

        literatureListSectionUpdateValidator(command, state)
    }

    @Test
    fun `Given a list section update command, when contents have changed, it updates the list section`() {
        val contributorId = ContributorId(UUID.randomUUID())
        val literatureList = createLiteratureList()
        val oldSection = literatureList.sections.last()
        val state = UpdateLiteratureListSectionState(
            literatureList = literatureList,
            statements = oldSection.toGroupedStatements()
        )
        val command = oldSection.toUpdateListSectionCommand(contributorId, literatureList.id)
            .shouldBeInstanceOf<UpdateListSectionCommand>()
            .copy(entries = listOf(AbstractLiteratureListListSectionCommand.Entry(ThingId("other"))))

        every { abstractLiteratureListSectionUpdater.updateListSection(any(), any(), any(), any()) } just runs

        literatureListSectionUpdateValidator(command, state)

        verify(exactly = 1) {
            abstractLiteratureListSectionUpdater.updateListSection(
                contributorId = command.contributorId,
                newSection = command as AbstractLiteratureListListSectionCommand,
                oldSection = oldSection as LiteratureListListSection,
                statements = state.statements
            )
        }
    }

    private fun LiteratureListSection.toUpdateListSectionCommand(
        contributorId: ContributorId,
        literatureListId: ThingId,
    ): UpdateLiteratureListSectionCommand =
        when (this) {
            is LiteratureListListSection -> toUpdateListSectionCommand(contributorId, literatureListId)
            is LiteratureListTextSection -> toUpdateTextSectionCommand(contributorId, literatureListId)
        }

    private fun LiteratureListListSection.toUpdateListSectionCommand(
        contributorId: ContributorId,
        literatureListId: ThingId,
    ): UpdateListSectionCommand = UpdateListSectionCommand(
        literatureListSectionId = id,
        contributorId = contributorId,
        literatureListId = literatureListId,
        entries = entries.map { it.toCommandEntry() }
    )

    private fun LiteratureListTextSection.toUpdateTextSectionCommand(
        contributorId: ContributorId,
        literatureListId: ThingId,
    ): UpdateTextSectionCommand = UpdateTextSectionCommand(
        literatureListSectionId = id,
        contributorId = contributorId,
        literatureListId = literatureListId,
        heading = heading,
        headingSize = headingSize,
        text = text
    )
}
