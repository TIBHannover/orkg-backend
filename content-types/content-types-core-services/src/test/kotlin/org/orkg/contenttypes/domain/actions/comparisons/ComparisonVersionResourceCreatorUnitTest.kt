package org.orkg.contenttypes.domain.actions.comparisons

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
import org.orkg.contenttypes.domain.actions.CreateComparisonState
import org.orkg.contenttypes.input.testing.fixtures.dummyCreateComparisonCommand
import org.orkg.graph.domain.Classes
import org.orkg.graph.input.CreateResourceUseCase
import org.orkg.graph.input.ResourceUseCases

internal class ComparisonVersionResourceCreatorUnitTest {
    private val resourceService: ResourceUseCases = mockk()

    private val comparisonVersionResourceCreator = ComparisonVersionResourceCreator(resourceService)

    @BeforeEach
    fun resetState() {
        clearAllMocks()
    }

    @AfterEach
    fun verifyMocks() {
        confirmVerified(resourceService)
    }

    @Test
    fun `Given a literature list create command, it crates a new literature list version resource`() {
        val command = dummyCreateComparisonCommand()
        val state = CreateComparisonState()

        val resourceCreateCommand = CreateResourceUseCase.CreateCommand(
            label = command.title,
            classes = setOf(Classes.comparisonPublished, Classes.latestVersion),
            extractionMethod = command.extractionMethod,
            contributorId = command.contributorId,
            observatoryId = command.observatories.firstOrNull(),
            organizationId = command.organizations.firstOrNull()
        )
        val id = ThingId("R123")

        every { resourceService.createUnsafe(resourceCreateCommand) } returns id

        val result = comparisonVersionResourceCreator(command, state)

        result.asClue {
            it.comparisonId shouldBe id
            it.authors.size shouldBe 0
        }

        verify(exactly = 1) { resourceService.createUnsafe(resourceCreateCommand) }
    }
}
