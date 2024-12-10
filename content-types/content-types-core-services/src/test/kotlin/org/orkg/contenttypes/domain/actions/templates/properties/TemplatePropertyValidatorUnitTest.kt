package org.orkg.contenttypes.domain.actions.templates.properties

import io.kotest.matchers.shouldBe
import io.mockk.clearAllMocks
import io.mockk.confirmVerified
import io.mockk.every
import io.mockk.just
import io.mockk.mockk
import io.mockk.runs
import io.mockk.verify
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.orkg.contenttypes.domain.TemplateProperty
import org.orkg.contenttypes.domain.actions.AbstractTemplatePropertyValidator
import org.orkg.contenttypes.domain.testing.fixtures.createStringLiteralTemplateProperty
import org.orkg.contenttypes.domain.testing.fixtures.createUntypedTemplateProperty
import org.orkg.contenttypes.input.TemplatePropertyDefinition
import org.orkg.contenttypes.input.testing.fixtures.dummyUpdateUntypedTemplatePropertyCommand
import org.orkg.contenttypes.input.testing.fixtures.toTemplatePropertyDefinition

internal class TemplatePropertyValidatorUnitTest {
    private val abstractTemplatePropertyValidator: AbstractTemplatePropertyValidator = mockk()

    private val templatePropertyValidator =
        TemplatePropertyValidator<TemplatePropertyDefinition, TemplateProperty?>(
            abstractTemplatePropertyValidator = abstractTemplatePropertyValidator,
            newValueSelector = { it },
            oldValueSelector = { it }
        )

    @BeforeEach
    fun resetState() {
        clearAllMocks()
    }

    @AfterEach
    fun verifyMocks() {
        confirmVerified(abstractTemplatePropertyValidator)
    }

    @Test
    fun `Given a template property update command, when previous template property is not defined, it validates the new template property`() {
        val command = dummyUpdateUntypedTemplatePropertyCommand()
        val state = null

        every { abstractTemplatePropertyValidator.validate(command) } just runs

        templatePropertyValidator(command, state) shouldBe state

        verify(exactly = 1) { abstractTemplatePropertyValidator.validate(command) }
    }

    @Test
    fun `Given a template property update command, when new template property is different to existing template property, it is validated`() {
        val command = dummyUpdateUntypedTemplatePropertyCommand()
        val state = createStringLiteralTemplateProperty()

        every { abstractTemplatePropertyValidator.validate(command) } just runs

        templatePropertyValidator(command, state) shouldBe state

        verify(exactly = 1) { abstractTemplatePropertyValidator.validate(command) }
    }

    @Test
    fun `Given a template property update command, when new template property is identical to existing template property, it is not validated again`() {
        val state = createUntypedTemplateProperty()
        val command = state.toTemplatePropertyDefinition()

        templatePropertyValidator(command, state) shouldBe state
    }
}
