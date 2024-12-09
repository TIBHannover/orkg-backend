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
import org.orkg.contenttypes.domain.actions.CreateTemplatePropertyCommand
import org.orkg.contenttypes.domain.actions.CreateTemplatePropertyState
import org.orkg.contenttypes.input.testing.fixtures.dummyCreateNumberLiteralTemplatePropertyCommand
import org.orkg.contenttypes.input.testing.fixtures.dummyCreateOtherLiteralTemplatePropertyCommand
import org.orkg.contenttypes.input.testing.fixtures.dummyCreateResourceTemplatePropertyCommand
import org.orkg.contenttypes.input.testing.fixtures.dummyCreateStringLiteralTemplatePropertyCommand
import org.orkg.contenttypes.input.testing.fixtures.dummyCreateUntypedTemplatePropertyCommand
import org.orkg.graph.domain.Classes
import org.orkg.graph.output.ResourceRepository
import org.orkg.graph.testing.fixtures.createResource

internal class TemplatePropertyExistenceCreateValidatorUnitTest {
    private val resourceRepository: ResourceRepository = mockk()

    private val templatePropertyExistenceCreateValidator = TemplatePropertyExistenceCreateValidator(
        resourceRepository = resourceRepository
    )

    @BeforeEach
    fun resetState() {
        clearAllMocks()
    }

    @AfterEach
    fun verifyMocks() {
        confirmVerified(resourceRepository)
    }

    @ParameterizedTest
    @MethodSource("createTemplatePropertyCommands")
    fun `Given a template property create command, when searching for existing templates, it returns success`(command: CreateTemplatePropertyCommand) {
        val state = CreateTemplatePropertyState()
        val template = createResource(id = command.templateId, classes = setOf(Classes.nodeShape))

        every { resourceRepository.findById(command.templateId) } returns Optional.of(template)

        val result = templatePropertyExistenceCreateValidator(command, state)

        result.asClue {
            it.templatePropertyId shouldBe null
            it.propertyCount shouldBe null
        }

        verify(exactly = 1) { resourceRepository.findById(command.templateId) }
    }

    @ParameterizedTest
    @MethodSource("createTemplatePropertyCommands")
    fun `Given a template property create command, when template does not exist, it throws an exception`(command: CreateTemplatePropertyCommand) {
        val state = CreateTemplatePropertyState()

        every { resourceRepository.findById(command.templateId) } returns Optional.empty()

        assertThrows<TemplateNotFound> { templatePropertyExistenceCreateValidator(command, state) }

        verify(exactly = 1) { resourceRepository.findById(command.templateId) }
    }

    companion object {
        @JvmStatic
        fun createTemplatePropertyCommands(): Stream<Arguments> = Stream.of(
            Arguments.of(dummyCreateUntypedTemplatePropertyCommand()),
            Arguments.of(dummyCreateStringLiteralTemplatePropertyCommand()),
            Arguments.of(dummyCreateNumberLiteralTemplatePropertyCommand()),
            Arguments.of(dummyCreateOtherLiteralTemplatePropertyCommand()),
            Arguments.of(dummyCreateResourceTemplatePropertyCommand())
        )
    }
}
