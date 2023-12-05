package org.orkg.contenttypes.domain.actions.template

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
import org.orkg.contenttypes.domain.actions.TemplateState
import org.orkg.contenttypes.testing.fixtures.dummyCreateTemplateCommand
import org.orkg.graph.domain.Classes
import org.orkg.graph.input.CreateResourceUseCase
import org.orkg.graph.input.ResourceUseCases

class TemplateResourceCreatorUnitTest {
    private val resourceService: ResourceUseCases = mockk()

    private val templateResourceCreator = TemplateResourceCreator(resourceService)

    @BeforeEach
    fun resetState() {
        clearAllMocks()
    }

    @AfterEach
    fun verifyMocks() {
        confirmVerified(resourceService)
    }

    @Test
    fun `Given a template create command, it crates a new template resource`() {
        val command = dummyCreateTemplateCommand()
        val state = TemplateState()

        val resourceCreateCommand = CreateResourceUseCase.CreateCommand(
            label = command.label,
            classes = setOf(Classes.nodeShape),
            contributorId = command.contributorId,
            observatoryId = command.observatories.firstOrNull(),
            organizationId = command.organizations.firstOrNull()
        )
        val id = ThingId("Template")

        every { resourceService.create(resourceCreateCommand) } returns id

        val result = templateResourceCreator(command, state)

        result.asClue {
            it.templateId shouldBe id
        }

        verify(exactly = 1) { resourceService.create(resourceCreateCommand) }
    }
}
