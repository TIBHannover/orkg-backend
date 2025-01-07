package org.orkg.contenttypes.domain.actions.comparisons

import io.kotest.assertions.asClue
import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.junit.jupiter.api.Test
import org.orkg.common.ThingId
import org.orkg.common.testing.fixtures.MockkBaseTest
import org.orkg.contenttypes.domain.actions.CreateComparisonState
import org.orkg.contenttypes.input.testing.fixtures.dummyCreateComparisonCommand
import org.orkg.graph.domain.Classes
import org.orkg.graph.input.CreateResourceUseCase
import org.orkg.graph.input.ResourceUseCases

internal class ComparisonResourceCreatorUnitTest : MockkBaseTest {
    private val resourceService: ResourceUseCases = mockk()

    private val comparisonResourceCreator = ComparisonResourceCreator(resourceService)

    @Test
    fun `Given a comparison create command, it crates a new comparison resource`() {
        val command = dummyCreateComparisonCommand()
        val state = CreateComparisonState()

        val resourceCreateCommand = CreateResourceUseCase.CreateCommand(
            label = command.title,
            classes = setOf(Classes.comparison),
            extractionMethod = command.extractionMethod,
            contributorId = command.contributorId,
            observatoryId = command.observatories.firstOrNull(),
            organizationId = command.organizations.firstOrNull()
        )
        val id = ThingId("R123")

        every { resourceService.createUnsafe(resourceCreateCommand) } returns id

        val result = comparisonResourceCreator(command, state)

        result.asClue {
            it.authors.size shouldBe 0
            it.comparisonId shouldBe id
        }

        verify(exactly = 1) { resourceService.createUnsafe(resourceCreateCommand) }
    }
}
