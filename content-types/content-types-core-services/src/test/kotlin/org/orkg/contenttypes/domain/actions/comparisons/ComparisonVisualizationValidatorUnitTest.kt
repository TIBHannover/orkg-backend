package org.orkg.contenttypes.domain.actions.comparisons

import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertDoesNotThrow
import org.junit.jupiter.api.assertThrows
import org.orkg.common.ThingId
import org.orkg.common.testing.fixtures.MockkBaseTest
import org.orkg.contenttypes.domain.VisualizationNotFound
import org.orkg.graph.domain.Classes
import org.orkg.graph.output.ResourceRepository
import org.orkg.graph.testing.fixtures.createResource
import java.util.Optional

internal class ComparisonVisualizationValidatorUnitTest : MockkBaseTest {
    private val resourceRepository: ResourceRepository = mockk()

    private val visualizationValidator = ComparisonVisualizationValidator<List<ThingId>?, Unit>(resourceRepository) { it }

    @Test
    fun `Given a list of visualizations, when validating its visualizations, it returns success`() {
        val command = listOf(ThingId("R6541"), ThingId("R5364"), ThingId("R9786"), ThingId("R3120"))
        val visualization = createResource(classes = setOf(Classes.visualization))

        every { resourceRepository.findById(any()) } returns Optional.of(visualization)

        visualizationValidator(command, Unit)

        command.forEach {
            verify(exactly = 1) { resourceRepository.findById(it) }
        }
    }

    @Test
    fun `Given a list of visualizations, when visualization is missing, it throws an exception`() {
        val command = listOf(ThingId("R6541"), ThingId("R5364"), ThingId("R9786"), ThingId("R3120"))

        every { resourceRepository.findById(command.first()) } returns Optional.empty()

        assertThrows<VisualizationNotFound> { visualizationValidator(command, Unit) }

        verify(exactly = 1) { resourceRepository.findById(command.first()) }
    }

    @Test
    fun `Given a list of visualizations, when resource its not a visualization, it throws an exception`() {
        val command = listOf(ThingId("R6541"), ThingId("R5364"), ThingId("R9786"), ThingId("R3120"))
        val visualization = createResource(id = command.first())

        every { resourceRepository.findById(visualization.id) } returns Optional.of(visualization)

        assertThrows<VisualizationNotFound> { visualizationValidator(command, Unit) }

        verify(exactly = 1) { resourceRepository.findById(visualization.id) }
    }

    @Test
    fun `Given a list of visualizations, when empty, it does not throw an exception`() {
        assertDoesNotThrow { visualizationValidator(emptyList(), Unit) }
    }

    @Test
    fun `Given a list of visualizations, when null, it returns success`() {
        assertDoesNotThrow { visualizationValidator(null, Unit) }
    }
}
