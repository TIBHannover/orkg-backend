package org.orkg.contenttypes.domain.actions.templates

import io.kotest.assertions.asClue
import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.just
import io.mockk.mockk
import io.mockk.runs
import io.mockk.verify
import org.junit.jupiter.api.Test
import org.orkg.common.ObservatoryId
import org.orkg.common.OrganizationId
import org.orkg.common.testing.fixtures.MockkBaseTest
import org.orkg.contenttypes.domain.actions.UpdateTemplateState
import org.orkg.contenttypes.input.testing.fixtures.updateTemplateCommand
import org.orkg.graph.input.UnsafeResourceUseCases
import org.orkg.graph.input.UpdateResourceUseCase

internal class TemplateResourceUpdaterUnitTest : MockkBaseTest {
    private val unsafeResourceUseCases: UnsafeResourceUseCases = mockk()

    private val templateResourceUpdater = TemplateResourceUpdater(unsafeResourceUseCases)

    @Test
    fun `Given a template update command, it updates the template resource`() {
        val command = updateTemplateCommand()
        val state = UpdateTemplateState()

        val resourceUpdateCommand = UpdateResourceUseCase.UpdateCommand(
            id = command.templateId,
            contributorId = command.contributorId,
            label = command.label,
            observatoryId = command.observatories!!.single(),
            organizationId = command.organizations!!.single(),
            extractionMethod = command.extractionMethod,
            visibility = command.visibility
        )

        every { unsafeResourceUseCases.update(resourceUpdateCommand) } just runs

        val result = templateResourceUpdater(command, state)

        result.asClue {
            it.template shouldBe state.template
        }

        verify(exactly = 1) { unsafeResourceUseCases.update(resourceUpdateCommand) }
    }

    @Test
    fun `Given a template update command, when observatories are empty, it removes the associated observatory`() {
        val command = updateTemplateCommand().copy(observatories = emptyList())
        val state = UpdateTemplateState()

        val resourceUpdateCommand = UpdateResourceUseCase.UpdateCommand(
            id = command.templateId,
            contributorId = command.contributorId,
            label = command.label,
            observatoryId = ObservatoryId.UNKNOWN,
            organizationId = command.organizations!!.single(),
            extractionMethod = command.extractionMethod,
            visibility = command.visibility,
        )

        every { unsafeResourceUseCases.update(resourceUpdateCommand) } just runs

        val result = templateResourceUpdater(command, state)

        result.asClue {
            it.template shouldBe state.template
        }

        verify(exactly = 1) { unsafeResourceUseCases.update(resourceUpdateCommand) }
    }

    @Test
    fun `Given a template update command, when observatories are not set, it does not update the associated observatory`() {
        val command = updateTemplateCommand().copy(observatories = null)
        val state = UpdateTemplateState()

        val resourceUpdateCommand = UpdateResourceUseCase.UpdateCommand(
            id = command.templateId,
            contributorId = command.contributorId,
            label = command.label,
            observatoryId = null,
            organizationId = command.organizations!!.single(),
            extractionMethod = command.extractionMethod,
            visibility = command.visibility,
        )

        every { unsafeResourceUseCases.update(resourceUpdateCommand) } just runs

        val result = templateResourceUpdater(command, state)

        result.asClue {
            it.template shouldBe state.template
        }

        verify(exactly = 1) { unsafeResourceUseCases.update(resourceUpdateCommand) }
    }

    @Test
    fun `Given a template update command, when organizations are empty, it removes the associated organizations`() {
        val command = updateTemplateCommand().copy(organizations = emptyList())
        val state = UpdateTemplateState()

        val resourceUpdateCommand = UpdateResourceUseCase.UpdateCommand(
            id = command.templateId,
            contributorId = command.contributorId,
            label = command.label,
            observatoryId = command.observatories!!.single(),
            organizationId = OrganizationId.UNKNOWN,
            extractionMethod = command.extractionMethod,
            visibility = command.visibility,
        )

        every { unsafeResourceUseCases.update(resourceUpdateCommand) } just runs

        val result = templateResourceUpdater(command, state)

        result.asClue {
            it.template shouldBe state.template
        }

        verify(exactly = 1) { unsafeResourceUseCases.update(resourceUpdateCommand) }
    }

    @Test
    fun `Given a template update command, when organizations are not set, it does not update the associated organizations`() {
        val command = updateTemplateCommand().copy(organizations = null)
        val state = UpdateTemplateState()

        val resourceUpdateCommand = UpdateResourceUseCase.UpdateCommand(
            id = command.templateId,
            contributorId = command.contributorId,
            label = command.label,
            observatoryId = command.observatories!!.single(),
            organizationId = null,
            extractionMethod = command.extractionMethod,
            visibility = command.visibility,
        )

        every { unsafeResourceUseCases.update(resourceUpdateCommand) } just runs

        val result = templateResourceUpdater(command, state)

        result.asClue {
            it.template shouldBe state.template
        }

        verify(exactly = 1) { unsafeResourceUseCases.update(resourceUpdateCommand) }
    }
}
