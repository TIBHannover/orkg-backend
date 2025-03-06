package org.orkg.contenttypes.domain.actions.rosettastone.templates

import io.kotest.assertions.asClue
import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.junit.jupiter.api.Test
import org.orkg.common.ThingId
import org.orkg.common.testing.fixtures.MockkBaseTest
import org.orkg.contenttypes.domain.actions.CreateRosettaStoneTemplateState
import org.orkg.contenttypes.input.testing.fixtures.createRosettaStoneTemplateCommand
import org.orkg.graph.domain.Classes
import org.orkg.graph.input.CreateResourceUseCase
import org.orkg.graph.input.UnsafeResourceUseCases

internal class RosettaStoneTemplateResourceCreatorUnitTest : MockkBaseTest {
    private val unsafeResourceUseCases: UnsafeResourceUseCases = mockk()

    private val rosettaStoneTemplateResourceCreator = RosettaStoneTemplateResourceCreator(unsafeResourceUseCases)

    @Test
    fun `Given a rosetta stone template create command, it crates a new rosetta stone template resource`() {
        val command = createRosettaStoneTemplateCommand()
        val state = CreateRosettaStoneTemplateState()

        val resourceCreateCommand = CreateResourceUseCase.CreateCommand(
            contributorId = command.contributorId,
            label = command.label,
            classes = setOf(Classes.rosettaNodeShape),
            observatoryId = command.observatories.firstOrNull(),
            organizationId = command.organizations.firstOrNull()
        )
        val id = ThingId("RosettaStoneTemplate")

        every { unsafeResourceUseCases.create(resourceCreateCommand) } returns id

        val result = rosettaStoneTemplateResourceCreator(command, state)

        result.asClue {
            it.rosettaStoneTemplateId shouldBe id
        }

        verify(exactly = 1) { unsafeResourceUseCases.create(resourceCreateCommand) }
    }
}
