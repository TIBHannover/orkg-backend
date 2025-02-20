package org.orkg.contenttypes.domain.actions.templates

import io.kotest.assertions.asClue
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkObject
import io.mockk.verify
import org.junit.jupiter.api.Test
import org.orkg.common.testing.fixtures.MockkBaseTest
import org.orkg.contenttypes.domain.ContentTypeSubgraph
import org.orkg.contenttypes.domain.Template
import org.orkg.contenttypes.domain.TemplateNotFound
import org.orkg.contenttypes.domain.TemplateService
import org.orkg.contenttypes.domain.actions.UpdateTemplateState
import org.orkg.contenttypes.domain.testing.fixtures.createTemplate
import org.orkg.contenttypes.input.testing.fixtures.updateTemplateCommand
import org.orkg.graph.domain.Classes
import org.orkg.graph.output.ResourceRepository
import org.orkg.graph.testing.fixtures.createResource
import org.orkg.graph.testing.fixtures.createStatement
import java.util.Optional

internal class TemplateExistenceValidatorUnitTest : MockkBaseTest {
    private val templateService: TemplateService = mockk()
    private val resourceRepository: ResourceRepository = mockk()

    private val templateExistenceValidator = TemplateExistenceValidator(templateService, resourceRepository)

    @Test
    fun `Given a template update command, when checking for template existence, it returns success`() {
        val template = createTemplate()
        val command = updateTemplateCommand().copy(templateId = template.id)
        val state = UpdateTemplateState()
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

            templateExistenceValidator(command, state).asClue {
                it.template shouldBe template
                it.statements shouldBe statements
            }

            verify(exactly = 1) { resourceRepository.findById(template.id) }
            verify(exactly = 1) { templateService.findSubgraph(root) }
            verify(exactly = 1) { Template.from(root, statements) }
        }
    }

    @Test
    fun `Given a template update command, when checking for template existence and template is not found, it throws an exception`() {
        val template = createTemplate()
        val command = updateTemplateCommand().copy(templateId = template.id)
        val state = UpdateTemplateState()

        every { resourceRepository.findById(template.id) } returns Optional.empty()

        shouldThrow<TemplateNotFound> { templateExistenceValidator(command, state) }

        verify(exactly = 1) { resourceRepository.findById(template.id) }
    }
}
