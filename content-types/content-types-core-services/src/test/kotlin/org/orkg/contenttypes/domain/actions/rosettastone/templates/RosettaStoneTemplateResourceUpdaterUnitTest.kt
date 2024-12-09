package org.orkg.contenttypes.domain.actions.rosettastone.templates

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
import org.orkg.contenttypes.domain.actions.UpdateRosettaStoneTemplateState
import org.orkg.contenttypes.input.testing.fixtures.dummyUpdateRosettaStoneTemplateCommand
import org.orkg.graph.input.ResourceUseCases
import org.orkg.graph.input.UpdateResourceUseCase

internal class RosettaStoneTemplateResourceUpdaterUnitTest {
    private val resourceService: ResourceUseCases = mockk()

    private val rosettaStoneTemplateResourceUpdater = RosettaStoneTemplateResourceUpdater(resourceService)

    @BeforeEach
    fun resetState() {
        clearAllMocks()
    }

    @AfterEach
    fun verifyMocks() {
        confirmVerified(resourceService)
    }

    @Test
    fun `Given a rosetta stone template update command, it updates the rosetta stone template resource`() {
        val command = dummyUpdateRosettaStoneTemplateCommand()
        val state = UpdateRosettaStoneTemplateState()

        val resourceUpdateCommand = UpdateResourceUseCase.UpdateCommand(
            id = command.templateId,
            label = command.label,
            observatoryId = command.observatories!!.single(),
            organizationId = command.organizations!!.single()
        )

        every { resourceService.update(resourceUpdateCommand) } just runs

        val result = rosettaStoneTemplateResourceUpdater(command, state)

        result.asClue {
            it.rosettaStoneTemplate shouldBe state.rosettaStoneTemplate
            it.statements shouldBe state.statements
            it.isUsedInRosettaStoneStatement shouldBe state.isUsedInRosettaStoneStatement
        }

        verify(exactly = 1) { resourceService.update(resourceUpdateCommand) }
    }

    @Test
    fun `Given a rosetta stone template, when observatories are empty, it removes the associated observatory`() {
        val command = dummyUpdateRosettaStoneTemplateCommand().copy(observatories = emptyList())
        val state = UpdateRosettaStoneTemplateState()

        val resourceUpdateCommand = UpdateResourceUseCase.UpdateCommand(
            id = command.templateId,
            label = command.label,
            observatoryId = ObservatoryId.UNKNOWN,
            organizationId = command.organizations!!.single()
        )

        every { resourceService.update(resourceUpdateCommand) } just runs

        val result = rosettaStoneTemplateResourceUpdater(command, state)

        result.asClue {
            it.rosettaStoneTemplate shouldBe state.rosettaStoneTemplate
        }

        verify(exactly = 1) { resourceService.update(resourceUpdateCommand) }
    }

    @Test
    fun `Given a rosetta stone template, when observatories are not set, it does not update the associated observatory`() {
        val command = dummyUpdateRosettaStoneTemplateCommand().copy(observatories = null)
        val state = UpdateRosettaStoneTemplateState()

        val resourceUpdateCommand = UpdateResourceUseCase.UpdateCommand(
            id = command.templateId,
            label = command.label,
            observatoryId = null,
            organizationId = command.organizations!!.single()
        )

        every { resourceService.update(resourceUpdateCommand) } just runs

        val result = rosettaStoneTemplateResourceUpdater(command, state)

        result.asClue {
            it.rosettaStoneTemplate shouldBe state.rosettaStoneTemplate
        }

        verify(exactly = 1) { resourceService.update(resourceUpdateCommand) }
    }

    @Test
    fun `Given a rosetta stone template, when organizations are empty, it removes the associated organizations`() {
        val command = dummyUpdateRosettaStoneTemplateCommand().copy(organizations = emptyList())
        val state = UpdateRosettaStoneTemplateState()

        val resourceUpdateCommand = UpdateResourceUseCase.UpdateCommand(
            id = command.templateId,
            label = command.label,
            observatoryId = command.observatories!!.single(),
            organizationId = OrganizationId.UNKNOWN
        )

        every { resourceService.update(resourceUpdateCommand) } just runs

        val result = rosettaStoneTemplateResourceUpdater(command, state)

        result.asClue {
            it.rosettaStoneTemplate shouldBe state.rosettaStoneTemplate
        }

        verify(exactly = 1) { resourceService.update(resourceUpdateCommand) }
    }

    @Test
    fun `Given a rosetta stone template, when organizations are not set, it does not update the associated organizations`() {
        val command = dummyUpdateRosettaStoneTemplateCommand().copy(organizations = null)
        val state = UpdateRosettaStoneTemplateState()

        val resourceUpdateCommand = UpdateResourceUseCase.UpdateCommand(
            id = command.templateId,
            label = command.label,
            observatoryId = command.observatories!!.single(),
            organizationId = null
        )

        every { resourceService.update(resourceUpdateCommand) } just runs

        val result = rosettaStoneTemplateResourceUpdater(command, state)

        result.asClue {
            it.rosettaStoneTemplate shouldBe state.rosettaStoneTemplate
        }

        verify(exactly = 1) { resourceService.update(resourceUpdateCommand) }
    }
}
