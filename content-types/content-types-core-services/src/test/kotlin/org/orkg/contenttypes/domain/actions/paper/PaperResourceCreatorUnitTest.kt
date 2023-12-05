package org.orkg.contenttypes.domain.actions.paper

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
import org.orkg.contenttypes.domain.actions.CreatePaperState
import org.orkg.contenttypes.testing.fixtures.dummyCreatePaperCommand
import org.orkg.graph.domain.Classes
import org.orkg.graph.input.CreateResourceUseCase
import org.orkg.graph.input.ResourceUseCases

class PaperResourceCreatorUnitTest {
    private val resourceService: ResourceUseCases = mockk()

    private val paperResourceCreator = PaperResourceCreator(resourceService)

    @BeforeEach
    fun resetState() {
        clearAllMocks()
    }

    @AfterEach
    fun verifyMocks() {
        confirmVerified(resourceService)
    }

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
        val id = ThingId("Paper")

        every { resourceService.create(resourceCreateCommand) } returns id

        val result = paperResourceCreator(command, state)

        result.asClue {
            it.tempIds.size shouldBe 0
            it.validatedIds.size shouldBe 0
            it.bakedStatements.size shouldBe 0
            it.authors.size shouldBe 0
            it.paperId shouldBe id
        }

        verify(exactly = 1) { resourceService.create(resourceCreateCommand) }
    }
}
