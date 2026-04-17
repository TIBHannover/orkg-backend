package org.orkg.contenttypes.domain.actions.templates.instances

import io.kotest.assertions.asClue
import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.junit.jupiter.api.Test
import org.orkg.common.ThingId
import org.orkg.common.testing.fixtures.MockkBaseTest
import org.orkg.contenttypes.domain.actions.CreateTemplateInstanceState
import org.orkg.contenttypes.domain.testing.fixtures.createTemplate
import org.orkg.contenttypes.input.testing.fixtures.createTemplateInstanceCommand
import org.orkg.graph.input.CreateResourceUseCase
import org.orkg.graph.input.UnsafeResourceUseCases

internal class TemplateInstanceSubjectCreatorUnitTest : MockkBaseTest {
    private val unsafeResourceUseCases: UnsafeResourceUseCases = mockk()

    private val templateInstanceSubjectCreator = TemplateInstanceSubjectCreator(unsafeResourceUseCases)

    @Test
    fun `Given a template instance create command, it crates a new template instance subject resource`() {
        val command = createTemplateInstanceCommand()
        val state = CreateTemplateInstanceState(template = createTemplate())

        val resourceCreateCommand = CreateResourceUseCase.CreateCommand(
            contributorId = command.contributorId,
            label = command.label,
            classes = command.additionalClasses + state.template!!.targetClass.id,
            extractionMethod = command.extractionMethod,
        )
        val id = ThingId("R123")

        every { unsafeResourceUseCases.create(resourceCreateCommand) } returns id

        val result = templateInstanceSubjectCreator(command, state)

        result.asClue {
            it.templateInstanceId shouldBe id
            it.template shouldBe state.template
            it.validationCache shouldBe state.validationCache
            it.statementsToAdd shouldBe state.statementsToAdd
            it.literals shouldBe state.literals
        }

        verify(exactly = 1) { unsafeResourceUseCases.create(resourceCreateCommand) }
    }
}
