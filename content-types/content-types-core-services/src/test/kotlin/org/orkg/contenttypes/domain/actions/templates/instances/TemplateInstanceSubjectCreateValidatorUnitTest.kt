package org.orkg.contenttypes.domain.actions.templates.instances

import io.kotest.assertions.throwables.shouldThrow
import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.junit.jupiter.api.Test
import org.orkg.common.testing.fixtures.MockkBaseTest
import org.orkg.contenttypes.domain.actions.CreateTemplateInstanceState
import org.orkg.contenttypes.input.testing.fixtures.createTemplateInstanceCommand
import org.orkg.graph.domain.Classes
import org.orkg.graph.domain.InvalidClassCollection
import org.orkg.graph.domain.ReservedClassId
import org.orkg.graph.domain.ThingAlreadyExists
import org.orkg.graph.output.ClassRepository
import org.orkg.graph.output.ThingRepository
import org.orkg.graph.testing.fixtures.createResource
import java.util.Optional

internal class TemplateInstanceSubjectCreateValidatorUnitTest : MockkBaseTest {
    private val thingRepository: ThingRepository = mockk()
    private val classRepository: ClassRepository = mockk()

    private val templateInstanceSubjectCreateValidator = TemplateInstanceSubjectCreateValidator(thingRepository, classRepository)

    @Test
    fun `Given a template instance create command, when validating its subject, it returns success`() {
        val command = createTemplateInstanceCommand()
        val state = CreateTemplateInstanceState()

        every { thingRepository.findById(command.id!!) } returns Optional.empty()
        every { classRepository.existsAllById(any()) } returns true

        val result = templateInstanceSubjectCreateValidator(command, state)
        result shouldBe state

        verify(exactly = 1) { thingRepository.findById(command.id!!) }
        verify(exactly = 1) { classRepository.existsAllById(any()) }
    }

    @Test
    fun `Given a template instance create command, when validating its additional classes but class is reserved, it throws an exception`() {
        val command = createTemplateInstanceCommand().copy(
            id = null,
            additionalClasses = setOf(Classes.thing),
        )
        val state = CreateTemplateInstanceState()

        shouldThrow<ReservedClassId> {
            templateInstanceSubjectCreateValidator(command, state)
        }
    }

    @Test
    fun `Given a template instance create command, when validating its additional classes but a class does not exist, it throws an exception`() {
        val command = createTemplateInstanceCommand().copy(id = null)
        val state = CreateTemplateInstanceState()

        every { classRepository.existsAllById(any()) } returns false

        shouldThrow<InvalidClassCollection> {
            templateInstanceSubjectCreateValidator(command, state)
        }

        verify(exactly = 1) { classRepository.existsAllById(any()) }
    }

    @Test
    fun `Given a template instance create command, when validating its subject id and subject id is null, it does nothing`() {
        val command = createTemplateInstanceCommand().copy(
            id = null,
            additionalClasses = emptySet(),
        )
        val state = CreateTemplateInstanceState()

        val result = templateInstanceSubjectCreateValidator(command, state)
        result shouldBe state
    }

    @Test
    fun `Given a template instance create command, when validating its subject id and subject id is already taken, it throws an exception`() {
        val command = createTemplateInstanceCommand().copy(additionalClasses = emptySet())
        val state = CreateTemplateInstanceState()

        every { thingRepository.findById(command.id!!) } returns Optional.of(createResource(id = command.id!!))

        shouldThrow<ThingAlreadyExists> {
            templateInstanceSubjectCreateValidator(command, state)
        }

        verify(exactly = 1) { thingRepository.findById(command.id!!) }
    }
}
