package org.orkg.contenttypes.domain.actions.literaturelists.sections

import io.kotest.matchers.types.shouldBeInstanceOf
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
import org.orkg.common.ContributorId
import org.orkg.common.ThingId
import org.orkg.contenttypes.domain.ListSection
import org.orkg.contenttypes.domain.LiteratureListSection
import org.orkg.contenttypes.domain.TextSection
import org.orkg.contenttypes.domain.actions.UpdateLiteratureListSectionCommand
import org.orkg.contenttypes.domain.actions.UpdateLiteratureListSectionState
import org.orkg.contenttypes.domain.actions.literaturelists.AbstractLiteratureListSectionUpdater
import org.orkg.contenttypes.domain.testing.fixtures.createDummyLiteratureList
import org.orkg.contenttypes.domain.testing.fixtures.toGroupedStatements
import org.orkg.contenttypes.input.ListSectionDefinition
import org.orkg.contenttypes.input.TextSectionDefinition
import org.orkg.contenttypes.input.UpdateLiteratureListSectionUseCase.UpdateListSectionCommand
import org.orkg.contenttypes.input.UpdateLiteratureListSectionUseCase.UpdateTextSectionCommand

class LiteratureListSectionUpdaterUnitTest {
    private val abstractLiteratureListSectionUpdater: AbstractLiteratureListSectionUpdater = mockk()

    private val literatureListSectionUpdateValidator = LiteratureListSectionUpdater(abstractLiteratureListSectionUpdater)

    @BeforeEach
    fun resetState() {
        clearAllMocks()
    }

    @AfterEach
    fun verifyMocks() {
        confirmVerified(abstractLiteratureListSectionUpdater)
    }

    @Test
    fun `Given a text section update command, when contents are equal, it does nothing`() {
        val contributorId = ContributorId(UUID.randomUUID())
        val literatureList = createDummyLiteratureList()
        val state = UpdateLiteratureListSectionState(literatureList = literatureList)
        val command = literatureList.sections.first()
            .toUpdateListSectionCommand(contributorId, literatureList.id)
            .shouldBeInstanceOf<UpdateTextSectionCommand>()

        literatureListSectionUpdateValidator(command, state)
    }

    @Test
    fun `Given a text section update command, when contents have changed, it updates the text section`() {
        val contributorId = ContributorId(UUID.randomUUID())
        val literatureList = createDummyLiteratureList()
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
                newSection = command as TextSectionDefinition,
                oldSection = oldSection as TextSection,
                statements = state.statements
            )
        }
    }

    @Test
    fun `Given a list section update command, when contents are equal, it does nothing`() {
        val contributorId = ContributorId(UUID.randomUUID())
        val literatureList = createDummyLiteratureList()
        val state = UpdateLiteratureListSectionState(literatureList = literatureList)
        val command = literatureList.sections.last()
            .toUpdateListSectionCommand(contributorId, literatureList.id)
            .shouldBeInstanceOf<UpdateListSectionCommand>()

        literatureListSectionUpdateValidator(command, state)
    }

    @Test
    fun `Given a list section update command, when contents have changed, it updates the list section`() {
        val contributorId = ContributorId(UUID.randomUUID())
        val literatureList = createDummyLiteratureList()
        val oldSection = literatureList.sections.last()
        val state = UpdateLiteratureListSectionState(
            literatureList = literatureList,
            statements = oldSection.toGroupedStatements()
        )
        val command = oldSection.toUpdateListSectionCommand(contributorId, literatureList.id)
            .shouldBeInstanceOf<UpdateListSectionCommand>()
            .copy(entries = listOf(ThingId("other")))

        every { abstractLiteratureListSectionUpdater.updateListSection(any(), any(), any(), any()) } just runs

        literatureListSectionUpdateValidator(command, state)

        verify(exactly = 1) {
            abstractLiteratureListSectionUpdater.updateListSection(
                contributorId = command.contributorId,
                newSection = command as ListSectionDefinition,
                oldSection = oldSection as ListSection,
                statements = state.statements
            )
        }
    }

    private fun LiteratureListSection.toUpdateListSectionCommand(
        contributorId: ContributorId,
        literatureListId: ThingId
    ): UpdateLiteratureListSectionCommand =
        when (this) {
            is ListSection -> toUpdateListSectionCommand(contributorId, literatureListId)
            is TextSection -> toUpdateTextSectionCommand(contributorId, literatureListId)
        }

    private fun ListSection.toUpdateListSectionCommand(
        contributorId: ContributorId,
        literatureListId: ThingId
    ): UpdateListSectionCommand = UpdateListSectionCommand(
        literatureListSectionId = id,
        contributorId = contributorId,
        literatureListId = literatureListId,
        entries = entries.map { it.id }
    )

    private fun TextSection.toUpdateTextSectionCommand(
        contributorId: ContributorId,
        literatureListId: ThingId
    ): UpdateTextSectionCommand = UpdateTextSectionCommand(
        literatureListSectionId = id,
        contributorId = contributorId,
        literatureListId = literatureListId,
        heading = heading,
        headingSize = headingSize,
        text = text
    )
}
