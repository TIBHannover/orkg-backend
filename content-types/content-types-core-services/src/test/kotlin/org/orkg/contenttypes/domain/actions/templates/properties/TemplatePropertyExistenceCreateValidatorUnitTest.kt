package org.orkg.contenttypes.domain.actions.templates.properties

import io.kotest.assertions.asClue
import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkObject
import io.mockk.verify
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments
import org.junit.jupiter.params.provider.MethodSource
import org.orkg.common.testing.fixtures.MockkBaseTest
import org.orkg.contenttypes.domain.ContentTypeSubgraph
import org.orkg.contenttypes.domain.Template
import org.orkg.contenttypes.domain.TemplateNotFound
import org.orkg.contenttypes.domain.TemplateService
import org.orkg.contenttypes.domain.actions.CreateTemplatePropertyCommand
import org.orkg.contenttypes.domain.actions.CreateTemplatePropertyState
import org.orkg.contenttypes.domain.testing.fixtures.createTemplate
import org.orkg.contenttypes.input.testing.fixtures.createNumberLiteralTemplatePropertyCommand
import org.orkg.contenttypes.input.testing.fixtures.createOtherLiteralTemplatePropertyCommand
import org.orkg.contenttypes.input.testing.fixtures.createResourceTemplatePropertyCommand
import org.orkg.contenttypes.input.testing.fixtures.createStringLiteralTemplatePropertyCommand
import org.orkg.contenttypes.input.testing.fixtures.createUntypedTemplatePropertyCommand
import org.orkg.graph.domain.Classes
import org.orkg.graph.output.ResourceRepository
import org.orkg.graph.testing.fixtures.createResource
import org.orkg.graph.testing.fixtures.createStatement
import java.util.Optional
import java.util.stream.Stream

internal class TemplatePropertyExistenceCreateValidatorUnitTest : MockkBaseTest {
    private val templateService: TemplateService = mockk()
    private val resourceRepository: ResourceRepository = mockk()

    private val templatePropertyExistenceCreateValidator = TemplatePropertyExistenceCreateValidator(
        templateService,
        resourceRepository,
    )

    @ParameterizedTest
    @MethodSource("createTemplatePropertyCommands")
    fun `Given a template property create command, when searching for existing templates, it returns success`(command: CreateTemplatePropertyCommand) {
        val template = createTemplate().copy(id = command.templateId)
        val state = CreateTemplatePropertyState()
        val root = createResource(
            id = template.id,
            label = template.label,
            classes = setOf(Classes.nodeShape)
        )
        val statements = listOf(createStatement()).groupBy { it.subject.id }

        mockkObject(Template.Companion) {
            every { resourceRepository.findById(template.id) } returns Optional.of(root)
            every { templateService.findSubgraph(root) } returns ContentTypeSubgraph(root.id, statements)
            every { Template.from(root, statements) } returns template

            templatePropertyExistenceCreateValidator(command, state).asClue {
                it.template shouldBe template
                it.templatePropertyId shouldBe null
            }

            verify(exactly = 1) { resourceRepository.findById(template.id) }
            verify(exactly = 1) { templateService.findSubgraph(root) }
            verify(exactly = 1) { Template.from(root, statements) }
        }
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
            Arguments.of(createUntypedTemplatePropertyCommand()),
            Arguments.of(createStringLiteralTemplatePropertyCommand()),
            Arguments.of(createNumberLiteralTemplatePropertyCommand()),
            Arguments.of(createOtherLiteralTemplatePropertyCommand()),
            Arguments.of(createResourceTemplatePropertyCommand())
        )
    }
}
