package org.orkg.contenttypes.domain.actions.rosettastone.templates

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
import org.orkg.contenttypes.domain.actions.UpdateRosettaStoneTemplateState
import org.orkg.contenttypes.input.testing.fixtures.updateRosettaStoneTemplateCommand
import org.orkg.graph.input.UnsafeResourceUseCases
import org.orkg.graph.input.UpdateResourceUseCase

internal class RosettaStoneTemplateResourceUpdaterUnitTest : MockkBaseTest {
    private val unsafeResourceUseCases: UnsafeResourceUseCases = mockk()

    private val rosettaStoneTemplateResourceUpdater = RosettaStoneTemplateResourceUpdater(unsafeResourceUseCases)

    @Test
    fun `Given a rosetta stone template update command, it updates the rosetta stone template resource`() {
        val command = updateRosettaStoneTemplateCommand()
        val state = UpdateRosettaStoneTemplateState()

        val resourceUpdateCommand = UpdateResourceUseCase.UpdateCommand(
            id = command.templateId,
            contributorId = command.contributorId,
            label = command.label,
            observatoryId = command.observatories!!.single(),
            organizationId = command.organizations!!.single()
        )

        every { unsafeResourceUseCases.update(resourceUpdateCommand) } just runs

        val result = rosettaStoneTemplateResourceUpdater(command, state)

        result.asClue {
            it.rosettaStoneTemplate shouldBe state.rosettaStoneTemplate
            it.statements shouldBe state.statements
            it.isUsedInRosettaStoneStatement shouldBe state.isUsedInRosettaStoneStatement
        }

        verify(exactly = 1) { unsafeResourceUseCases.update(resourceUpdateCommand) }
    }

    @Test
    fun `Given a rosetta stone template, when observatories are empty, it removes the associated observatory`() {
        val command = updateRosettaStoneTemplateCommand().copy(observatories = emptyList())
        val state = UpdateRosettaStoneTemplateState()

        val resourceUpdateCommand = UpdateResourceUseCase.UpdateCommand(
            id = command.templateId,
            contributorId = command.contributorId,
            label = command.label,
            observatoryId = ObservatoryId.UNKNOWN,
            organizationId = command.organizations!!.single()
        )

        every { unsafeResourceUseCases.update(resourceUpdateCommand) } just runs

        val result = rosettaStoneTemplateResourceUpdater(command, state)

        result.asClue {
            it.rosettaStoneTemplate shouldBe state.rosettaStoneTemplate
        }

        verify(exactly = 1) { unsafeResourceUseCases.update(resourceUpdateCommand) }
    }

    @Test
    fun `Given a rosetta stone template, when observatories are not set, it does not update the associated observatory`() {
        val command = updateRosettaStoneTemplateCommand().copy(observatories = null)
        val state = UpdateRosettaStoneTemplateState()

        val resourceUpdateCommand = UpdateResourceUseCase.UpdateCommand(
            id = command.templateId,
            contributorId = command.contributorId,
            label = command.label,
            observatoryId = null,
            organizationId = command.organizations!!.single()
        )

        every { unsafeResourceUseCases.update(resourceUpdateCommand) } just runs

        val result = rosettaStoneTemplateResourceUpdater(command, state)

        result.asClue {
            it.rosettaStoneTemplate shouldBe state.rosettaStoneTemplate
        }

        verify(exactly = 1) { unsafeResourceUseCases.update(resourceUpdateCommand) }
    }

    @Test
    fun `Given a rosetta stone template, when organizations are empty, it removes the associated organizations`() {
        val command = updateRosettaStoneTemplateCommand().copy(organizations = emptyList())
        val state = UpdateRosettaStoneTemplateState()

        val resourceUpdateCommand = UpdateResourceUseCase.UpdateCommand(
            id = command.templateId,
            contributorId = command.contributorId,
            label = command.label,
            observatoryId = command.observatories!!.single(),
            organizationId = OrganizationId.UNKNOWN
        )

        every { unsafeResourceUseCases.update(resourceUpdateCommand) } just runs

        val result = rosettaStoneTemplateResourceUpdater(command, state)

        result.asClue {
            it.rosettaStoneTemplate shouldBe state.rosettaStoneTemplate
        }

        verify(exactly = 1) { unsafeResourceUseCases.update(resourceUpdateCommand) }
    }

    @Test
    fun `Given a rosetta stone template, when organizations are not set, it does not update the associated organizations`() {
        val command = updateRosettaStoneTemplateCommand().copy(organizations = null)
        val state = UpdateRosettaStoneTemplateState()

        val resourceUpdateCommand = UpdateResourceUseCase.UpdateCommand(
            id = command.templateId,
            contributorId = command.contributorId,
            label = command.label,
            observatoryId = command.observatories!!.single(),
            organizationId = null
        )

        every { unsafeResourceUseCases.update(resourceUpdateCommand) } just runs

        val result = rosettaStoneTemplateResourceUpdater(command, state)

        result.asClue {
            it.rosettaStoneTemplate shouldBe state.rosettaStoneTemplate
        }

        verify(exactly = 1) { unsafeResourceUseCases.update(resourceUpdateCommand) }
    }
}
