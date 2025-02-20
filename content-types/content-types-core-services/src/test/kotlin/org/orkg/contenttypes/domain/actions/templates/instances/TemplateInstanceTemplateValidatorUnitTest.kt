package org.orkg.contenttypes.domain.actions.templates.instances

import io.kotest.assertions.asClue
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.junit.jupiter.api.Test
import org.orkg.common.testing.fixtures.MockkBaseTest
import org.orkg.contenttypes.domain.TemplateNotFound
import org.orkg.contenttypes.domain.actions.UpdateTemplateInstanceState
import org.orkg.contenttypes.domain.testing.fixtures.createTemplate
import org.orkg.contenttypes.input.TemplateUseCases
import org.orkg.contenttypes.input.testing.fixtures.updateTemplateInstanceCommand
import java.util.Optional

internal class TemplateInstanceTemplateValidatorUnitTest : MockkBaseTest {
    private val templateService: TemplateUseCases = mockk()

    private val templateInstanceTemplateValidator = TemplateInstanceTemplateValidator(templateService)

    @Test
    fun `Given a template instance update command, when validating its template, it returns success`() {
        val command = updateTemplateInstanceCommand()
        val state = UpdateTemplateInstanceState()
        val template = createTemplate()

        every { templateService.findById(command.templateId) } returns Optional.of(template)

        val result = templateInstanceTemplateValidator(command, state)

        result.asClue {
            it.template shouldBe template
            it.templateInstance shouldBe state.templateInstance
            it.tempIds shouldBe state.tempIds
            it.validatedIds shouldBe state.validatedIds
            it.statementsToAdd shouldBe state.statementsToAdd
            it.statementsToRemove shouldBe state.statementsToRemove
            it.literals shouldBe state.literals
        }

        verify(exactly = 1) { templateService.findById(command.templateId) }
    }

    @Test
    fun `Given a template instance update command, when template is not found, it throws an exception`() {
        val command = updateTemplateInstanceCommand()
        val state = UpdateTemplateInstanceState(
            template = createTemplate()
        )

        every { templateService.findById(command.templateId) } returns Optional.empty()

        shouldThrow<TemplateNotFound> {
            templateInstanceTemplateValidator(command, state)
        }

        verify(exactly = 1) { templateService.findById(command.templateId) }
    }
}
