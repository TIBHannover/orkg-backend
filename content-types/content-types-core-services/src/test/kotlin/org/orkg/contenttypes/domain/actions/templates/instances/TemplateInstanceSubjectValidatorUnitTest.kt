package org.orkg.contenttypes.domain.actions.templates.instances

import io.kotest.assertions.asClue
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.junit.jupiter.api.Test
import org.orkg.common.testing.fixtures.MockkBaseTest
import org.orkg.contenttypes.domain.TemplateInstanceService
import org.orkg.contenttypes.domain.actions.UpdateTemplateInstanceState
import org.orkg.contenttypes.domain.testing.fixtures.createTemplate
import org.orkg.contenttypes.domain.testing.fixtures.createTemplateInstance
import org.orkg.contenttypes.input.testing.fixtures.updateTemplateInstanceCommand
import org.orkg.graph.domain.ResourceNotFound
import org.orkg.graph.output.ResourceRepository
import org.orkg.graph.testing.fixtures.createResource
import java.util.Optional

internal class TemplateInstanceSubjectValidatorUnitTest : MockkBaseTest {
    private val resourceRepository: ResourceRepository = mockk()
    private val templateInstanceService: TemplateInstanceService = mockk()

    private val templateInstanceSubjectValidator = TemplateInstanceSubjectValidator(resourceRepository, templateInstanceService)

    @Test
    fun `Given a template instance update command, when validating its subject, it returns success`() {
        val command = updateTemplateInstanceCommand()
        val state = UpdateTemplateInstanceState(
            template = createTemplate()
        )
        val subject = createResource()
        val templateInstance = createTemplateInstance()

        every { resourceRepository.findById(command.subject) } returns Optional.of(subject)
        every { templateInstanceService.run { subject.toTemplateInstance(state.template!!) } } returns templateInstance

        val result = templateInstanceSubjectValidator(command, state)

        result.asClue {
            it.template shouldBe state.template
            it.templateInstance shouldBe templateInstance
            it.validationCache shouldBe state.validationCache
            it.statementsToAdd shouldBe state.statementsToAdd
            it.statementsToRemove shouldBe state.statementsToRemove
            it.literals shouldBe state.literals
        }

        verify(exactly = 1) { resourceRepository.findById(command.subject) }
        verify(exactly = 1) { templateInstanceService.run { subject.toTemplateInstance(state.template!!) } }
    }

    @Test
    fun `Given a template instance update command, when subject resource is not found, it throws an exception`() {
        val command = updateTemplateInstanceCommand()
        val state = UpdateTemplateInstanceState(
            template = createTemplate()
        )

        every { resourceRepository.findById(command.subject) } returns Optional.empty()

        shouldThrow<ResourceNotFound> {
            templateInstanceSubjectValidator(command, state)
        }

        verify(exactly = 1) { resourceRepository.findById(command.subject) }
    }
}
