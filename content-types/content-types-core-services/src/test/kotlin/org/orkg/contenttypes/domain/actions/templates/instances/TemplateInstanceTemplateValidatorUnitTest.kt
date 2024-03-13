package org.orkg.contenttypes.domain.actions.templates.instances

import io.kotest.assertions.asClue
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.matchers.shouldBe
import io.mockk.clearAllMocks
import io.mockk.confirmVerified
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import java.util.*
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.orkg.contenttypes.domain.TemplateNotFound
import org.orkg.contenttypes.domain.actions.UpdateTemplateInstanceState
import org.orkg.contenttypes.domain.testing.fixtures.createDummyTemplate
import org.orkg.contenttypes.input.TemplateUseCases
import org.orkg.contenttypes.input.testing.fixtures.dummyUpdateTemplateInstanceCommand

class TemplateInstanceTemplateValidatorUnitTest {
    private val templateService: TemplateUseCases = mockk()

private val templateInstanceTemplateValidator = TemplateInstanceTemplateValidator(templateService)

    @BeforeEach
    fun resetState() {
        clearAllMocks()
    }

    @AfterEach
    fun verifyMocks() {
        confirmVerified(templateService)
    }

    @Test
    fun `Given a template instance update command, when validating its template, it returns success`() {
        val command = dummyUpdateTemplateInstanceCommand()
        val state = UpdateTemplateInstanceState()
        val template = createDummyTemplate()

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
        val command = dummyUpdateTemplateInstanceCommand()
        val state = UpdateTemplateInstanceState(
            template = createDummyTemplate()
        )

        every { templateService.findById(command.templateId) } returns Optional.empty()

        shouldThrow<TemplateNotFound> {
            templateInstanceTemplateValidator(command, state)
        }

        verify(exactly = 1) { templateService.findById(command.templateId) }
    }
}
