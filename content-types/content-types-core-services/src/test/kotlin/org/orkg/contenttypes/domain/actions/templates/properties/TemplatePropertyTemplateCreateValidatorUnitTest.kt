package org.orkg.contenttypes.domain.actions.templates.properties

import org.junit.jupiter.api.assertDoesNotThrow
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments
import org.junit.jupiter.params.provider.MethodSource
import org.orkg.common.testing.fixtures.MockkBaseTest
import org.orkg.contenttypes.domain.TemplateClosed
import org.orkg.contenttypes.domain.actions.CreateTemplatePropertyCommand
import org.orkg.contenttypes.domain.actions.CreateTemplatePropertyState
import org.orkg.contenttypes.domain.testing.fixtures.createTemplate
import org.orkg.contenttypes.input.testing.fixtures.createNumberLiteralTemplatePropertyCommand
import org.orkg.contenttypes.input.testing.fixtures.createOtherLiteralTemplatePropertyCommand
import org.orkg.contenttypes.input.testing.fixtures.createResourceTemplatePropertyCommand
import org.orkg.contenttypes.input.testing.fixtures.createStringLiteralTemplatePropertyCommand
import org.orkg.contenttypes.input.testing.fixtures.createUntypedTemplatePropertyCommand
import java.util.stream.Stream

internal class TemplatePropertyTemplateCreateValidatorUnitTest : MockkBaseTest {
    private val templatePropertyTemplateCreateValidator = TemplatePropertyTemplateCreateValidator()

    @ParameterizedTest
    @MethodSource("createTemplatePropertyCommands")
    fun `Given a template property create command, when validating template metadata, it returns success`(command: CreateTemplatePropertyCommand) {
        val state = CreateTemplatePropertyState(template = createTemplate().copy(isClosed = false))

        assertDoesNotThrow { templatePropertyTemplateCreateValidator(command, state) }
    }

    @ParameterizedTest
    @MethodSource("createTemplatePropertyCommands")
    fun `Given a template property create command, when template is closed, it throws an exception`(command: CreateTemplatePropertyCommand) {
        val state = CreateTemplatePropertyState(template = createTemplate().copy(isClosed = true))

        assertThrows<TemplateClosed> { templatePropertyTemplateCreateValidator(command, state) }
    }

    companion object {
        @JvmStatic
        fun createTemplatePropertyCommands(): Stream<Arguments> = Stream.of(
            Arguments.of(createUntypedTemplatePropertyCommand()),
            Arguments.of(createStringLiteralTemplatePropertyCommand()),
            Arguments.of(createNumberLiteralTemplatePropertyCommand()),
            Arguments.of(createOtherLiteralTemplatePropertyCommand()),
            Arguments.of(createResourceTemplatePropertyCommand())
        )
    }
}
