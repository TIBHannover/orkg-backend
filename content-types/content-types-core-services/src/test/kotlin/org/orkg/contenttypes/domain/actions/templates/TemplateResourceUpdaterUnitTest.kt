package org.orkg.contenttypes.domain.actions.templates

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
import org.orkg.contenttypes.domain.actions.UpdateTemplateState
import org.orkg.contenttypes.input.testing.fixtures.dummyUpdateTemplateCommand
import org.orkg.graph.input.ResourceUseCases
import org.orkg.graph.input.UpdateResourceUseCase

class TemplateResourceUpdaterUnitTest {
    private val resourceService: ResourceUseCases = mockk()

    private val templateResourceUpdater = TemplateResourceUpdater(resourceService)

    @BeforeEach
    fun resetState() {
        clearAllMocks()
    }

    @AfterEach
    fun verifyMocks() {
        confirmVerified(resourceService)
    }

    @Test
    fun `Given a template update command, it updates the template resource`() {
        val command = dummyUpdateTemplateCommand()
        val state = UpdateTemplateState()

        val resourceUpdateCommand = UpdateResourceUseCase.UpdateCommand(
            id = command.templateId,
            label = command.label,
            observatoryId = command.observatories!!.single(),
            organizationId = command.organizations!!.single(),
            extractionMethod = command.extractionMethod
        )

        every { resourceService.update(resourceUpdateCommand) } just runs

        val result = templateResourceUpdater(command, state)

        result.asClue {
            it.template shouldBe state.template
        }

        verify(exactly = 1) { resourceService.update(resourceUpdateCommand) }
    }

    @Test
    fun `Given a template update command, when observatories are empty, it removes the associated observatory`() {
        val command = dummyUpdateTemplateCommand().copy(observatories = emptyList())
        val state = UpdateTemplateState()

        val resourceUpdateCommand = UpdateResourceUseCase.UpdateCommand(
            id = command.templateId,
            label = command.label,
            observatoryId = ObservatoryId.UNKNOWN,
            organizationId = command.organizations!!.single(),
            extractionMethod = command.extractionMethod
        )

        every { resourceService.update(resourceUpdateCommand) } just runs

        val result = templateResourceUpdater(command, state)

        result.asClue {
            it.template shouldBe state.template
        }

        verify(exactly = 1) { resourceService.update(resourceUpdateCommand) }
    }

    @Test
    fun `Given a template update command, when observatories are not set, it does not update the associated observatory`() {
        val command = dummyUpdateTemplateCommand().copy(observatories = null)
        val state = UpdateTemplateState()

        val resourceUpdateCommand = UpdateResourceUseCase.UpdateCommand(
            id = command.templateId,
            label = command.label,
            observatoryId = null,
            organizationId = command.organizations!!.single(),
            extractionMethod = command.extractionMethod
        )

        every { resourceService.update(resourceUpdateCommand) } just runs

        val result = templateResourceUpdater(command, state)

        result.asClue {
            it.template shouldBe state.template
        }

        verify(exactly = 1) { resourceService.update(resourceUpdateCommand) }
    }

    @Test
    fun `Given a template update command, when organizations are empty, it removes the associated organizations`() {
        val command = dummyUpdateTemplateCommand().copy(organizations = emptyList())
        val state = UpdateTemplateState()

        val resourceUpdateCommand = UpdateResourceUseCase.UpdateCommand(
            id = command.templateId,
            label = command.label,
            observatoryId = command.observatories!!.single(),
            organizationId = OrganizationId.UNKNOWN,
            extractionMethod = command.extractionMethod
        )

        every { resourceService.update(resourceUpdateCommand) } just runs

        val result = templateResourceUpdater(command, state)

        result.asClue {
            it.template shouldBe state.template
        }

        verify(exactly = 1) { resourceService.update(resourceUpdateCommand) }
    }

    @Test
    fun `Given a template update command, when organizations are not set, it does not update the associated organizations`() {
        val command = dummyUpdateTemplateCommand().copy(organizations = null)
        val state = UpdateTemplateState()

        val resourceUpdateCommand = UpdateResourceUseCase.UpdateCommand(
            id = command.templateId,
            label = command.label,
            observatoryId = command.observatories!!.single(),
            organizationId = null,
            extractionMethod = command.extractionMethod
        )

        every { resourceService.update(resourceUpdateCommand) } just runs

        val result = templateResourceUpdater(command, state)

        result.asClue {
            it.template shouldBe state.template
        }

        verify(exactly = 1) { resourceService.update(resourceUpdateCommand) }
    }
}
