package org.orkg.contenttypes.domain.actions.literaturelists

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
import org.orkg.common.ObservatoryId
import org.orkg.common.OrganizationId
import org.orkg.contenttypes.domain.actions.UpdateLiteratureListState
import org.orkg.contenttypes.input.testing.fixtures.dummyUpdateLiteratureListCommand
import org.orkg.graph.input.ResourceUseCases
import org.orkg.graph.input.UpdateResourceUseCase

class LiteratureListResourceUpdaterUnitTest {
    private val resourceService: ResourceUseCases = mockk()

    private val literatureListResourceUpdater = LiteratureListResourceUpdater(resourceService)

    @BeforeEach
    fun resetState() {
        clearAllMocks()
    }

    @AfterEach
    fun verifyMocks() {
        confirmVerified(resourceService)
    }

    @Test
    fun `Given a literature list update command, it updates the literature list resource`() {
        val command = dummyUpdateLiteratureListCommand()
        val state = UpdateLiteratureListState()

        val resourceUpdateCommand = UpdateResourceUseCase.UpdateCommand(
            id = command.literatureListId,
            label = command.title,
            observatoryId = command.observatories!!.single(),
            organizationId = command.organizations!!.single(),
            extractionMethod = command.extractionMethod!!
        )

        every { resourceService.update(resourceUpdateCommand) } just runs

        val result = literatureListResourceUpdater(command, state)

        result.asClue {
            it.literatureList shouldBe state.literatureList
            it.statements shouldBe state.statements
            it.authors.size shouldBe 0
        }

        verify(exactly = 1) { resourceService.update(resourceUpdateCommand) }
    }

    @Test
    fun `Given a literature list update command, when observatories are empty, it removes the associated observatory`() {
        val command = dummyUpdateLiteratureListCommand().copy(observatories = emptyList())
        val state = UpdateLiteratureListState()

        val resourceUpdateCommand = UpdateResourceUseCase.UpdateCommand(
            id = command.literatureListId,
            label = command.title,
            observatoryId = ObservatoryId.UNKNOWN,
            organizationId = command.organizations!!.single(),
            extractionMethod = command.extractionMethod
        )

        every { resourceService.update(resourceUpdateCommand) } just runs

        val result = literatureListResourceUpdater(command, state)

        result.asClue {
            it.literatureList shouldBe state.literatureList
        }

        verify(exactly = 1) { resourceService.update(resourceUpdateCommand) }
    }

    @Test
    fun `Given a literature list update command, when observatories are not set, it does not update the associated observatory`() {
        val command = dummyUpdateLiteratureListCommand().copy(observatories = null)
        val state = UpdateLiteratureListState()

        val resourceUpdateCommand = UpdateResourceUseCase.UpdateCommand(
            id = command.literatureListId,
            label = command.title,
            observatoryId = null,
            organizationId = command.organizations!!.single(),
            extractionMethod = command.extractionMethod
        )

        every { resourceService.update(resourceUpdateCommand) } just runs

        val result = literatureListResourceUpdater(command, state)

        result.asClue {
            it.literatureList shouldBe state.literatureList
        }

        verify(exactly = 1) { resourceService.update(resourceUpdateCommand) }
    }

    @Test
    fun `Given a literature list update command, when organizations are empty, it removes the associated organizations`() {
        val command = dummyUpdateLiteratureListCommand().copy(organizations = emptyList())
        val state = UpdateLiteratureListState()

        val resourceUpdateCommand = UpdateResourceUseCase.UpdateCommand(
            id = command.literatureListId,
            label = command.title,
            observatoryId = command.observatories!!.single(),
            organizationId = OrganizationId.UNKNOWN,
            extractionMethod = command.extractionMethod
        )

        every { resourceService.update(resourceUpdateCommand) } just runs

        val result = literatureListResourceUpdater(command, state)

        result.asClue {
            it.literatureList shouldBe state.literatureList
        }

        verify(exactly = 1) { resourceService.update(resourceUpdateCommand) }
    }

    @Test
    fun `Given a literature list update command, when organizations are not set, it does not update the associated organizations`() {
        val command = dummyUpdateLiteratureListCommand().copy(organizations = null)
        val state = UpdateLiteratureListState()

        val resourceUpdateCommand = UpdateResourceUseCase.UpdateCommand(
            id = command.literatureListId,
            label = command.title,
            observatoryId = command.observatories!!.single(),
            organizationId = null,
            extractionMethod = command.extractionMethod
        )

        every { resourceService.update(resourceUpdateCommand) } just runs

        val result = literatureListResourceUpdater(command, state)

        result.asClue {
            it.literatureList shouldBe state.literatureList
        }

        verify(exactly = 1) { resourceService.update(resourceUpdateCommand) }
    }
}
