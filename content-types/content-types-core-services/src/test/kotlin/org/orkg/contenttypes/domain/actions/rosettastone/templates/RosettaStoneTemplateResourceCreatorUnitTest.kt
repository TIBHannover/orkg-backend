package org.orkg.contenttypes.domain.actions.rosettastone.templates

import io.kotest.assertions.asClue
import io.kotest.matchers.shouldBe
import io.mockk.clearAllMocks
import io.mockk.confirmVerified
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.orkg.common.ThingId
import org.orkg.contenttypes.domain.actions.CreateRosettaStoneTemplateState
import org.orkg.contenttypes.input.testing.fixtures.dummyCreateRosettaStoneTemplateCommand
import org.orkg.graph.domain.Classes
import org.orkg.graph.input.CreateResourceUseCase
import org.orkg.graph.input.ResourceUseCases

internal class RosettaStoneTemplateResourceCreatorUnitTest {
    private val resourceService: ResourceUseCases = mockk()

    private val rosettaStoneTemplateResourceCreator = RosettaStoneTemplateResourceCreator(resourceService)

    @BeforeEach
    fun resetState() {
        clearAllMocks()
    }

    @AfterEach
    fun verifyMocks() {
        confirmVerified(resourceService)
    }

    @Test
    fun `Given a rosetta stone template create command, it crates a new rosetta stone template resource`() {
        val command = dummyCreateRosettaStoneTemplateCommand()
        val state = CreateRosettaStoneTemplateState()

        val resourceCreateCommand = CreateResourceUseCase.CreateCommand(
            label = command.label,
            classes = setOf(Classes.rosettaNodeShape),
            contributorId = command.contributorId,
            observatoryId = command.observatories.firstOrNull(),
            organizationId = command.organizations.firstOrNull()
        )
        val id = ThingId("RosettaStoneTemplate")

        every { resourceService.createUnsafe(resourceCreateCommand) } returns id

        val result = rosettaStoneTemplateResourceCreator(command, state)

        result.asClue {
            it.rosettaStoneTemplateId shouldBe id
        }

        verify(exactly = 1) { resourceService.createUnsafe(resourceCreateCommand) }
    }
}
