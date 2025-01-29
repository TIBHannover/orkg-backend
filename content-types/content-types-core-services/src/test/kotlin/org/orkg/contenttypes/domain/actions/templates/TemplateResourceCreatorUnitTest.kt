package org.orkg.contenttypes.domain.actions.templates

import io.kotest.assertions.asClue
import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.junit.jupiter.api.Test
import org.orkg.common.ThingId
import org.orkg.common.testing.fixtures.MockkBaseTest
import org.orkg.contenttypes.domain.actions.CreateTemplateState
import org.orkg.contenttypes.input.testing.fixtures.createTemplateCommand
import org.orkg.graph.domain.Classes
import org.orkg.graph.input.CreateResourceUseCase
import org.orkg.graph.input.UnsafeResourceUseCases

internal class TemplateResourceCreatorUnitTest : MockkBaseTest {
    private val unsafeResourceUseCases: UnsafeResourceUseCases = mockk()

    private val templateResourceCreator = TemplateResourceCreator(unsafeResourceUseCases)

    @Test
    fun `Given a template create command, it crates a new template resource`() {
        val command = createTemplateCommand()
        val state = CreateTemplateState()

        val resourceCreateCommand = CreateResourceUseCase.CreateCommand(
            label = command.label,
            classes = setOf(Classes.nodeShape),
            contributorId = command.contributorId,
            observatoryId = command.observatories.firstOrNull(),
            organizationId = command.organizations.firstOrNull(),
            extractionMethod = command.extractionMethod
        )
        val id = ThingId("Template")

        every { unsafeResourceUseCases.create(resourceCreateCommand) } returns id

        val result = templateResourceCreator(command, state)

        result.asClue {
            it.templateId shouldBe id
        }

        verify(exactly = 1) { unsafeResourceUseCases.create(resourceCreateCommand) }
    }
}
