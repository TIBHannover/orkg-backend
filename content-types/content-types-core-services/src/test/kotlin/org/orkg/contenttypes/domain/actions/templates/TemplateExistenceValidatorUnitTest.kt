package org.orkg.contenttypes.domain.actions.templates

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
import org.orkg.contenttypes.domain.actions.UpdateTemplateState
import org.orkg.contenttypes.domain.testing.fixtures.createDummyTemplate
import org.orkg.contenttypes.input.TemplateUseCases
import org.orkg.contenttypes.input.testing.fixtures.dummyUpdateTemplateCommand

class TemplateExistenceValidatorUnitTest {
    private val templateService: TemplateUseCases = mockk()

    private val templateExistenceValidator = TemplateExistenceValidator(templateService)

    @BeforeEach
    fun resetState() {
        clearAllMocks()
    }

    @AfterEach
    fun verifyMocks() {
        confirmVerified(templateService)
    }

    @Test
    fun `Given a template update command, when checking for template existence, it returns success`() {
        val template = createDummyTemplate()
        val command = dummyUpdateTemplateCommand().copy(templateId = template.id)
        val state = UpdateTemplateState()

        every { templateService.findById(template.id) } returns Optional.of(template)

        templateExistenceValidator(command, state).asClue {
            it.template shouldBe template
        }

        verify(exactly = 1) { templateService.findById(template.id) }
    }

    @Test
    fun `Given a template update command, when checking for template existence and template is not found, it throws an exception`() {
        val template = createDummyTemplate()
        val command = dummyUpdateTemplateCommand().copy(templateId = template.id)
        val state = UpdateTemplateState()

        every { templateService.findById(template.id) } returns Optional.empty()

        shouldThrow<TemplateNotFound> { templateExistenceValidator(command, state) }

        verify(exactly = 1) { templateService.findById(template.id) }
    }
}
