package org.orkg.contenttypes.domain.actions

import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.orkg.common.ThingId
import org.orkg.common.testing.fixtures.MockkBaseTest
import org.orkg.graph.domain.Classes
import org.orkg.graph.domain.ResourceNotFound
import org.orkg.graph.domain.ThingNotFound
import org.orkg.graph.output.ResourceRepository
import org.orkg.graph.testing.fixtures.createResource
import java.util.Optional

internal class ResourceValidatorUnitTest : MockkBaseTest {
    private val resourceRepository: ResourceRepository = mockk()
    private val includeClasses = mutableSetOf<ThingId>()
    private val exceptionFactory = mockk<(ThingId) -> Throwable>()

    private val resourceValidator = ResourceValidator<Set<ThingId>?, Set<ThingId>>(
        resourceRepository = resourceRepository,
        newValueSelector = { it },
        oldValueSelector = { it },
        includeClasses = includeClasses,
        exceptionFactory = exceptionFactory,
    )

    @BeforeEach
    fun setup() {
        includeClasses.clear()
    }

    @Test
    fun `Given a set of resource ids, when set of included classes is empty, it returns success`() {
        every { resourceRepository.findById(any()) } returns Optional.of(createResource())

        resourceValidator(setOf(ThingId("R123"), ThingId("R456")), emptySet())

        verify(exactly = 2) { resourceRepository.findById(any()) }
    }

    @Test
    fun `Given a set of resource ids, when set of included classes is not empty, it returns success`() {
        includeClasses.add(Classes.paper)
        val paper = createResource(classes = setOf(Classes.paper))

        every { resourceRepository.findById(paper.id) } returns Optional.of(paper)

        resourceValidator(setOf(paper.id), emptySet())

        verify(exactly = 1) { resourceRepository.findById(paper.id) }
    }

    @Test
    fun `Given a set of resource ids, when resource is not found, it throws an exception`() {
        val id = ThingId("R123")

        every { resourceRepository.findById(any()) } returns Optional.empty()
        every { exceptionFactory(any()) } returns ResourceNotFound(id)

        assertThrows<ResourceNotFound> { resourceValidator(setOf(id), emptySet()) }

        verify(exactly = 1) { resourceRepository.findById(id) }
        verify(exactly = 1) { exceptionFactory(id) }
    }

    @Test
    fun `Given a set of resource ids, when resource is not and instance of the included classes, it throws an exception`() {
        val id = ThingId("R123")
        includeClasses.add(Classes.paper)

        every { resourceRepository.findById(any()) } returns Optional.of(createResource())
        every { exceptionFactory(any()) } returns ResourceNotFound(id)

        assertThrows<ResourceNotFound> { resourceValidator(setOf(id), emptySet()) }

        verify(exactly = 1) { resourceRepository.findById(id) }
        verify(exactly = 1) { exceptionFactory(id) }
    }

    @Test
    fun `Given a set of resource ids, when resource is not valid, it throws the exception provided by the factory`() {
        val id = ThingId("R123")

        every { resourceRepository.findById(any()) } returns Optional.empty()
        every { exceptionFactory(any()) } returns ThingNotFound(id)

        assertThrows<ThingNotFound> { resourceValidator(setOf(id), emptySet()) }

        verify(exactly = 1) { resourceRepository.findById(id) }
        verify(exactly = 1) { exceptionFactory(id) }
    }

    @Test
    fun `Given a set of resource ids, when old set of resource ids is identical, it does nothing`() {
        val ids = setOf(ThingId("R123"), ThingId("R456"))
        resourceValidator(ids, ids)
    }

    @Test
    fun `Given a set of resource ids, when no new resource ids are set, it does nothing`() {
        val ids = setOf(ThingId("R123"), ThingId("R456"))
        resourceValidator(null, ids)
    }
}
