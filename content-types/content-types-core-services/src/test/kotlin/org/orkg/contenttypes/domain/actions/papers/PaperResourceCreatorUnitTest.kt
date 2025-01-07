package org.orkg.contenttypes.domain.actions.papers

import io.kotest.assertions.asClue
import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.junit.jupiter.api.Test
import org.orkg.common.ThingId
import org.orkg.common.testing.fixtures.MockkBaseTest
import org.orkg.contenttypes.domain.actions.CreatePaperState
import org.orkg.contenttypes.input.testing.fixtures.dummyCreatePaperCommand
import org.orkg.graph.domain.Classes
import org.orkg.graph.input.CreateResourceUseCase
import org.orkg.graph.input.ResourceUseCases

internal class PaperResourceCreatorUnitTest : MockkBaseTest {
    private val resourceService: ResourceUseCases = mockk()

    private val paperResourceCreator = PaperResourceCreator(resourceService)

    @Test
    fun `Given a paper create command, it crates a new paper resource`() {
        val command = dummyCreatePaperCommand()
        val state = CreatePaperState()

        val resourceCreateCommand = CreateResourceUseCase.CreateCommand(
            label = command.title,
            classes = setOf(Classes.paper),
            extractionMethod = command.extractionMethod,
            contributorId = command.contributorId,
            observatoryId = command.observatories.firstOrNull(),
            organizationId = command.organizations.firstOrNull()
        )
        val id = ThingId("R123")

        every { resourceService.createUnsafe(resourceCreateCommand) } returns id

        val result = paperResourceCreator(command, state)

        result.asClue {
            it.tempIds.size shouldBe 0
            it.validatedIds.size shouldBe 0
            it.bakedStatements.size shouldBe 0
            it.authors.size shouldBe 0
            it.paperId shouldBe id
        }

        verify(exactly = 1) { resourceService.createUnsafe(resourceCreateCommand) }
    }
}
