package org.orkg.contenttypes.domain.actions.templates.properties

import io.kotest.assertions.asClue
import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.orkg.common.ThingId
import org.orkg.contenttypes.domain.TemplateClosed
import org.orkg.contenttypes.domain.UnrelatedTemplateProperty
import org.orkg.contenttypes.domain.actions.UpdateTemplatePropertyState
import org.orkg.contenttypes.domain.testing.fixtures.createDummyTemplate
import org.orkg.contenttypes.input.testing.fixtures.dummyUpdateUntypedTemplatePropertyCommand

internal class TemplatePropertyTemplateUpdateValidatorUnitTest {
    private val templatePropertyTemplateUpdateValidator = TemplatePropertyTemplateUpdateValidator()

    @Test
    fun `Given a template property update command, when validating template metadata, it returns success`() {
        val template = createDummyTemplate().copy(isClosed = false)
        val state = UpdateTemplatePropertyState().copy(template = template)
        val command = dummyUpdateUntypedTemplatePropertyCommand()

        val result = templatePropertyTemplateUpdateValidator(command, state)

        result.asClue {
            it.template shouldBe template
            it.templateProperty shouldBe template.properties.first()
        }
    }

    @Test
    fun `Given a template property update command, when template is closed, it throws an exception`() {
        val template = createDummyTemplate().copy(isClosed = true)
        val state = UpdateTemplatePropertyState().copy(template = template)
        val command = dummyUpdateUntypedTemplatePropertyCommand()

        assertThrows<TemplateClosed> { templatePropertyTemplateUpdateValidator(command, state) }
    }

    @Test
    fun `Given a template property update command, when template property does not belong to template, it throws an exception`() {
        val template = createDummyTemplate().copy(isClosed = false)
        val state = UpdateTemplatePropertyState().copy(template = template)
        val command = dummyUpdateUntypedTemplatePropertyCommand().copy(templatePropertyId = ThingId("missing"))

        assertThrows<UnrelatedTemplateProperty> { templatePropertyTemplateUpdateValidator(command, state) }
    }
}
