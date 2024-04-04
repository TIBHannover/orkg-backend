package org.orkg.contenttypes.domain.actions.templates.properties

import io.kotest.assertions.asClue
import io.kotest.matchers.shouldBe
import io.mockk.clearAllMocks
import io.mockk.confirmVerified
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import java.util.*
import java.util.stream.Stream
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments
import org.junit.jupiter.params.provider.MethodSource
import org.orkg.contenttypes.domain.TemplateNotFound
import org.orkg.contenttypes.domain.actions.UpdateTemplatePropertyCommand
import org.orkg.contenttypes.domain.actions.UpdateTemplatePropertyState
import org.orkg.contenttypes.domain.testing.fixtures.createDummyTemplate
import org.orkg.contenttypes.input.TemplateUseCases
import org.orkg.contenttypes.input.testing.fixtures.dummyUpdateNumberLiteralTemplatePropertyCommand
import org.orkg.contenttypes.input.testing.fixtures.dummyUpdateOtherLiteralTemplatePropertyCommand
import org.orkg.contenttypes.input.testing.fixtures.dummyUpdateResourceTemplatePropertyCommand
import org.orkg.contenttypes.input.testing.fixtures.dummyUpdateStringLiteralTemplatePropertyCommand
import org.orkg.contenttypes.input.testing.fixtures.dummyUpdateUntypedTemplatePropertyCommand

class TemplatePropertyExistenceUpdateValidatorUnitTest {
    private val templateUseCases: TemplateUseCases = mockk()

    private val templatePropertyExistenceUpdateValidator = TemplatePropertyExistenceUpdateValidator(templateUseCases)

    @BeforeEach
    fun resetState() {
        clearAllMocks()
    }

    @AfterEach
    fun verifyMocks() {
        confirmVerified(templateUseCases)
    }

    @ParameterizedTest
    @MethodSource("updateTemplatePropertyCommands")
    fun `Given a template property update command, when searching for existing templates, it returns success`(command: UpdateTemplatePropertyCommand) {
        val state = UpdateTemplatePropertyState()
        val template = createDummyTemplate()

        every { templateUseCases.findById(command.templateId) } returns Optional.of(template)

        val result = templatePropertyExistenceUpdateValidator(command, state)

        result.asClue {
            it.template shouldBe template
        }

        verify(exactly = 1) { templateUseCases.findById(command.templateId) }
    }

    @ParameterizedTest
    @MethodSource("updateTemplatePropertyCommands")
    fun `Given a template property update command, when template does not exist, it throws an exception`(command: UpdateTemplatePropertyCommand) {
        val state = UpdateTemplatePropertyState()

        every { templateUseCases.findById(command.templateId) } returns Optional.empty()

        assertThrows<TemplateNotFound> { templatePropertyExistenceUpdateValidator(command, state) }

        verify(exactly = 1) { templateUseCases.findById(command.templateId) }
    }

    companion object {
        @JvmStatic
        fun updateTemplatePropertyCommands(): Stream<Arguments> = Stream.of(
            Arguments.of(dummyUpdateUntypedTemplatePropertyCommand()),
            Arguments.of(dummyUpdateStringLiteralTemplatePropertyCommand()),
            Arguments.of(dummyUpdateNumberLiteralTemplatePropertyCommand()),
            Arguments.of(dummyUpdateOtherLiteralTemplatePropertyCommand()),
            Arguments.of(dummyUpdateResourceTemplatePropertyCommand())
        )
    }
}
