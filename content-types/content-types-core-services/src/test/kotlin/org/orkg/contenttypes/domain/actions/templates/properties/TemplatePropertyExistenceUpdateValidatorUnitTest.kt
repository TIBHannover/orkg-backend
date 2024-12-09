package org.orkg.contenttypes.domain.actions.templates.properties

import io.kotest.assertions.asClue
import io.kotest.matchers.shouldBe
import io.mockk.clearAllMocks
import io.mockk.confirmVerified
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkObject
import io.mockk.verify
import java.util.*
import java.util.stream.Stream
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments
import org.junit.jupiter.params.provider.MethodSource
import org.orkg.contenttypes.domain.ContentTypeSubgraph
import org.orkg.contenttypes.domain.Template
import org.orkg.contenttypes.domain.TemplateNotFound
import org.orkg.contenttypes.domain.TemplateService
import org.orkg.contenttypes.domain.actions.UpdateTemplatePropertyCommand
import org.orkg.contenttypes.domain.actions.UpdateTemplatePropertyState
import org.orkg.contenttypes.domain.testing.fixtures.createDummyTemplate
import org.orkg.contenttypes.input.testing.fixtures.dummyUpdateNumberLiteralTemplatePropertyCommand
import org.orkg.contenttypes.input.testing.fixtures.dummyUpdateOtherLiteralTemplatePropertyCommand
import org.orkg.contenttypes.input.testing.fixtures.dummyUpdateResourceTemplatePropertyCommand
import org.orkg.contenttypes.input.testing.fixtures.dummyUpdateStringLiteralTemplatePropertyCommand
import org.orkg.contenttypes.input.testing.fixtures.dummyUpdateUntypedTemplatePropertyCommand
import org.orkg.graph.domain.Classes
import org.orkg.graph.output.ResourceRepository
import org.orkg.graph.testing.fixtures.createResource
import org.orkg.graph.testing.fixtures.createStatement

internal class TemplatePropertyExistenceUpdateValidatorUnitTest {
    private val templateService: TemplateService = mockk()
    private val resourceRepository: ResourceRepository = mockk()

    private val templatePropertyExistenceUpdateValidator = TemplatePropertyExistenceUpdateValidator(templateService, resourceRepository)

    @BeforeEach
    fun resetState() {
        clearAllMocks()
    }

    @AfterEach
    fun verifyMocks() {
        confirmVerified(templateService, resourceRepository)
    }

    @ParameterizedTest
    @MethodSource("updateTemplatePropertyCommands")
    fun `Given a template property update command, when searching for existing templates, it returns success`(command: UpdateTemplatePropertyCommand) {
        val state = UpdateTemplatePropertyState()
        val template = createDummyTemplate().copy(id = command.templateId)
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

            templatePropertyExistenceUpdateValidator(command, state).asClue {
                it.template shouldBe template
                it.statements shouldBe statements
            }

            verify(exactly = 1) { resourceRepository.findById(template.id) }
            verify(exactly = 1) { templateService.findSubgraph(root) }
            verify(exactly = 1) { Template.from(root, statements) }
        }
    }

    @ParameterizedTest
    @MethodSource("updateTemplatePropertyCommands")
    fun `Given a template property update command, when template does not exist, it throws an exception`(command: UpdateTemplatePropertyCommand) {
        val state = UpdateTemplatePropertyState()

        every { resourceRepository.findById(command.templateId) } returns Optional.empty()

        assertThrows<TemplateNotFound> { templatePropertyExistenceUpdateValidator(command, state) }

        verify(exactly = 1) { resourceRepository.findById(command.templateId) }
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
