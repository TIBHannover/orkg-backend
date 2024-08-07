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
import org.orkg.contenttypes.domain.actions.UpdateRosettaStoneTemplateState
import org.orkg.contenttypes.input.testing.fixtures.dummyUpdateRosettaStoneTemplateCommand
import org.orkg.graph.input.ResourceUseCases
import org.orkg.graph.input.UpdateResourceUseCase

class RosettaStoneTemplateResourceUpdaterUnitTest {
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
}
