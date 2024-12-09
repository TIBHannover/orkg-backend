package org.orkg.contenttypes.domain.actions.templates.instances

import io.kotest.assertions.asClue
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.matchers.shouldBe
import io.mockk.clearAllMocks
import io.mockk.confirmVerified
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import java.util.*
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.orkg.contenttypes.domain.TemplateInstanceService
import org.orkg.contenttypes.domain.actions.UpdateTemplateInstanceState
import org.orkg.contenttypes.domain.testing.fixtures.createDummyTemplate
import org.orkg.contenttypes.domain.testing.fixtures.createDummyTemplateInstance
import org.orkg.contenttypes.input.testing.fixtures.dummyUpdateTemplateInstanceCommand
import org.orkg.graph.domain.ResourceNotFound
import org.orkg.graph.output.ResourceRepository
import org.orkg.graph.testing.fixtures.createResource

internal class TemplateInstanceSubjectValidatorUnitTest {
    private val resourceRepository: ResourceRepository = mockk()
    private val templateInstanceService: TemplateInstanceService = mockk()

    private val templateInstanceSubjectValidator = TemplateInstanceSubjectValidator(resourceRepository, templateInstanceService)

    @BeforeEach
    fun resetState() {
        clearAllMocks()
    }

    @AfterEach
    fun verifyMocks() {
        confirmVerified(resourceRepository, templateInstanceService)
    }

    @Test
    fun `Given a template instance update command, when validating its subject, it returns success`() {
        val command = dummyUpdateTemplateInstanceCommand()
        val state = UpdateTemplateInstanceState(
            template = createDummyTemplate()
        )
        val subject = createResource()
        val templateInstance = createDummyTemplateInstance()

        every { resourceRepository.findById(command.subject) } returns Optional.of(subject)
        every { templateInstanceService.run { subject.toTemplateInstance(state.template!!) } } returns templateInstance

        val result = templateInstanceSubjectValidator(command, state)

        result.asClue {
            it.template shouldBe state.template
            it.templateInstance shouldBe templateInstance
            it.tempIds shouldBe state.tempIds
            it.validatedIds shouldBe state.validatedIds
            it.statementsToAdd shouldBe state.statementsToAdd
            it.statementsToRemove shouldBe state.statementsToRemove
            it.literals shouldBe state.literals
        }

        verify(exactly = 1) { resourceRepository.findById(command.subject) }
        verify(exactly = 1) { templateInstanceService.run { subject.toTemplateInstance(state.template!!) } }
    }

    @Test
    fun `Given a template instance update command, when subject resource is not found, it throws an exception`() {
        val command = dummyUpdateTemplateInstanceCommand()
        val state = UpdateTemplateInstanceState(
            template = createDummyTemplate()
        )

        every { resourceRepository.findById(command.subject) } returns Optional.empty()

        shouldThrow<ResourceNotFound> {
            templateInstanceSubjectValidator(command, state)
        }

        verify(exactly = 1) { resourceRepository.findById(command.subject) }
    }
}
