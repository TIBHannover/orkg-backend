package org.orkg.contenttypes.domain.actions.visualizations

import io.kotest.assertions.asClue
import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.junit.jupiter.api.Test
import org.orkg.common.ThingId
import org.orkg.common.testing.fixtures.MockkBaseTest
import org.orkg.contenttypes.domain.actions.VisualizationState
import org.orkg.contenttypes.input.testing.fixtures.dummyCreateVisualizationCommand
import org.orkg.graph.domain.Classes
import org.orkg.graph.input.CreateResourceUseCase
import org.orkg.graph.input.ResourceUseCases

internal class VisualizationResourceCreatorUnitTest : MockkBaseTest {
    private val resourceService: ResourceUseCases = mockk()

    private val visualizationResourceCreator = VisualizationResourceCreator(resourceService)

    @Test
    fun `Given a visualization create command, it crates a new visualization resource`() {
        val command = dummyCreateVisualizationCommand()
        val state = VisualizationState()

        val resourceCreateCommand = CreateResourceUseCase.CreateCommand(
            label = command.title,
            classes = setOf(Classes.visualization),
            extractionMethod = command.extractionMethod,
            contributorId = command.contributorId,
            observatoryId = command.observatories.firstOrNull(),
            organizationId = command.organizations.firstOrNull()
        )
        val id = ThingId("R123")

        every { resourceService.createUnsafe(resourceCreateCommand) } returns id

        val result = visualizationResourceCreator(command, state)

        result.asClue {
            it.authors.size shouldBe 0
            it.visualizationId shouldBe id
        }

        verify(exactly = 1) { resourceService.createUnsafe(resourceCreateCommand) }
    }
}
