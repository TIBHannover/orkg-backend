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
import org.orkg.contenttypes.input.testing.fixtures.createPaperCommand
import org.orkg.graph.domain.Classes
import org.orkg.graph.input.CreateResourceUseCase
import org.orkg.graph.input.UnsafeResourceUseCases

internal class PaperResourceCreatorUnitTest : MockkBaseTest {
    private val unsafeResourceUseCases: UnsafeResourceUseCases = mockk()

    private val paperResourceCreator = PaperResourceCreator(unsafeResourceUseCases)

    @Test
    fun `Given a paper create command, it crates a new paper resource`() {
        val command = createPaperCommand()
        val state = CreatePaperState()

        val resourceCreateCommand = CreateResourceUseCase.CreateCommand(
            contributorId = command.contributorId,
            label = command.title,
            classes = setOf(Classes.paper),
            extractionMethod = command.extractionMethod,
            observatoryId = command.observatories.firstOrNull(),
            organizationId = command.organizations.firstOrNull()
        )
        val id = ThingId("R123")

        every { unsafeResourceUseCases.create(resourceCreateCommand) } returns id

        val result = paperResourceCreator(command, state)

        result.asClue {
            it.tempIds.size shouldBe 0
            it.validatedIds.size shouldBe 0
            it.bakedStatements.size shouldBe 0
            it.authors.size shouldBe 0
            it.paperId shouldBe id
        }

        verify(exactly = 1) { unsafeResourceUseCases.create(resourceCreateCommand) }
    }
}
