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
import org.orkg.contenttypes.domain.actions.CreateTemplateInstanceState
import org.orkg.contenttypes.domain.testing.fixtures.createTemplate
import org.orkg.contenttypes.input.TemplateUseCases
import org.orkg.contenttypes.input.testing.fixtures.createTemplateInstanceCommand
import java.util.Optional

internal class TemplateInstanceTemplateCreateValidatorUnitTest : MockkBaseTest {
    private val templateService: TemplateUseCases = mockk()

    private val templateInstanceTemplateCreateValidator = TemplateInstanceTemplateCreateValidator(templateService)

    @Test
    fun `Given a template instance create command, when validating its template, it returns success`() {
        val command = createTemplateInstanceCommand()
        val state = CreateTemplateInstanceState()
        val template = createTemplate()

        every { templateService.findById(command.templateId) } returns Optional.of(template)

        val result = templateInstanceTemplateCreateValidator(command, state)

        result.asClue {
            it.template shouldBe template
            it.templateInstanceId shouldBe state.templateInstanceId
            it.validationCache shouldBe state.validationCache
            it.statementsToAdd shouldBe state.statementsToAdd
            it.literals shouldBe state.literals
        }

        verify(exactly = 1) { templateService.findById(command.templateId) }
    }

    @Test
    fun `Given a template instance create command, when template is not found, it throws an exception`() {
        val command = createTemplateInstanceCommand()
        val state = CreateTemplateInstanceState(
            template = createTemplate(),
        )

        every { templateService.findById(command.templateId) } returns Optional.empty()

        shouldThrow<TemplateNotFound> {
            templateInstanceTemplateCreateValidator(command, state)
        }

        verify(exactly = 1) { templateService.findById(command.templateId) }
    }
}
