package org.orkg.contenttypes.domain.actions.templates

import io.kotest.assertions.throwables.shouldNotThrowAny
import io.kotest.assertions.throwables.shouldThrow
import org.junit.jupiter.api.Test
import org.orkg.contenttypes.domain.TemplateClosed
import org.orkg.contenttypes.domain.actions.UpdateTemplateState
import org.orkg.contenttypes.domain.testing.fixtures.createTemplate
import org.orkg.contenttypes.input.testing.fixtures.updateTemplateCommand
import org.orkg.contenttypes.input.testing.fixtures.updateUntypedTemplatePropertyCommand

internal class TemplateClosedValidatorUnitTest {
    private val templateClosedValidator = TemplateClosedValidator()

    @Test
    fun `Given a template update command, when existing template is not closed, it does nothing`() {
        val command = updateTemplateCommand()
        val template = createTemplate().copy(isClosed = false)
        val state = UpdateTemplateState(template = template)

        shouldNotThrowAny { templateClosedValidator(command, state) }
    }

    @Test
    fun `Given a template update command, when existing template is closed and new properties are unset, it does nothing`() {
        val command = updateTemplateCommand().copy(properties = null)
        val template = createTemplate()
        val state = UpdateTemplateState(template = template)

        shouldNotThrowAny { templateClosedValidator(command, state) }
    }

    @Test
    fun `Given a template update command, when existing template is closed and property count changes, it throws an exception`() {
        val command = updateTemplateCommand().copy(properties = listOf(updateUntypedTemplatePropertyCommand()))
        val template = createTemplate()
        val state = UpdateTemplateState(template = template)

        shouldThrow<TemplateClosed> { templateClosedValidator(command, state) }
    }

    @Test
    fun `Given a template update command, when existing template is closed and properties change, it throws an exception`() {
        val command = updateTemplateCommand()
        val template = createTemplate()
        val state = UpdateTemplateState(template = template)

        shouldThrow<TemplateClosed> { templateClosedValidator(command, state) }
    }
}
