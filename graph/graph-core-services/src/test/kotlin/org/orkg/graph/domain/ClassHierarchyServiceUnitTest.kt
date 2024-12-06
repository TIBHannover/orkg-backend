package org.orkg.graph.domain

import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import java.util.*
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.orkg.common.ContributorId
import org.orkg.common.ThingId
import org.orkg.graph.output.ClassHierarchyRepository
import org.orkg.graph.output.ClassRelationRepository
import org.orkg.graph.output.ClassRepository
import org.orkg.graph.testing.fixtures.createClass
import org.orkg.testing.fixedClock
import org.springframework.data.domain.PageImpl
import org.springframework.data.domain.PageRequest

class ClassHierarchyServiceUnitTest {
    private val repository: ClassHierarchyRepository = mockk()
    private val relationRepository: ClassRelationRepository = mockk()
    private val classRepository: ClassRepository = mockk()
    private val service = ClassHierarchyService(repository, relationRepository, classRepository, fixedClock)

    @Test
    fun `given a class relation is created, when the parent id and child id are identical, then an exception is thrown`() {
        val parentId = ThingId("identical")
        val childId = ThingId("identical")

        every { classRepository.findById(parentId) } returns Optional.of(createClass(id = parentId))

        val exception = assertThrows<InvalidSubclassRelation> {
            service.create(ContributorId.UNKNOWN, parentId, setOf(childId), false)
        }
        assertThat(exception.message).isEqualTo(InvalidSubclassRelation(childId, parentId).message)
    }

    @Test
    fun `given a class relation is created, when the parent id is not a leaf, then an exception is thrown`() {
        val parentId = ThingId("parent")
        val childId = ThingId("child")

        every { classRepository.findById(parentId) } returns Optional.of(createClass(id = parentId))
        every { repository.existsChildren(parentId) } returns true

        val exception = assertThrows<ParentClassAlreadyHasChildren> {
            service.create(ContributorId.UNKNOWN, parentId, setOf(childId), true)
        }
        assertThat(exception.message).isEqualTo(ParentClassAlreadyHasChildren(parentId).message)
    }

    @Test
    fun `given a class relation is created, when the child class cannot be found, then an exception is thrown`() {
        val parentId = ThingId("parent")
        val childId = ThingId("child")

        every { classRepository.findById(parentId) } returns Optional.of(createClass(id = parentId))
        every { classRepository.findById(childId) } returns Optional.empty()

        val exception = assertThrows<ClassNotFound> {
            service.create(ContributorId.UNKNOWN, parentId, setOf(childId), false)
        }
        assertThat(exception.message).isEqualTo(ClassNotFound.withThingId(childId).message)
    }

    @Test
    fun `given a class relation is created, when the parent class cannot be found, then an exception is thrown`() {
        val parentId = ThingId("parent")
        val childId = ThingId("child")

        every { classRepository.findById(childId) } returns Optional.of(createClass(id = childId))
        every { classRepository.findById(parentId) } returns Optional.empty()

        val exception = assertThrows<ClassNotFound> {
            service.create(ContributorId.UNKNOWN, parentId, setOf(childId), false)
        }
        assertThat(exception.message).isEqualTo(ClassNotFound.withThingId(parentId).message)
    }

    @Test
    fun `given a class relation is created, when the child already has a parent class, then an exception is thrown`() {
        val parentId = ThingId("parent")
        val childId = ThingId("child")

        every { classRepository.findById(childId) } returns Optional.of(createClass(id = childId))
        every { classRepository.findById(parentId) } returns Optional.of(createClass(id = parentId))
        every { repository.findParent(childId) } returns Optional.of(createClass(id = parentId))

        val exception = assertThrows<ParentClassAlreadyExists> {
            service.create(ContributorId.UNKNOWN, parentId, setOf(childId), false)
        }
        assertThat(exception.message).isEqualTo(ParentClassAlreadyExists(childId, parentId).message)
    }

    @Test
    fun `given a class relation is created, when the child already has the parent class as a child, then an exception is thrown`() {
        val parentId = ThingId("parent")
        val childId = ThingId("child")

        every { classRepository.findById(childId) } returns Optional.of(createClass(id = childId))
        every { classRepository.findById(parentId) } returns Optional.of(createClass(id = parentId))
        every { repository.findParent(childId) } returns Optional.empty()
        every { repository.existsChild(childId, parentId) } returns true

        val exception = assertThrows<InvalidSubclassRelation> {
            service.create(ContributorId.UNKNOWN, parentId, setOf(childId), false)
        }
        assertThat(exception.message).isEqualTo(InvalidSubclassRelation(childId, parentId).message)
    }

    @Test
    fun `given a class relation is created, it returns success`() {
        val parentId = ThingId("parent")
        val childId = ThingId("child")

        every { classRepository.findById(childId) } returns Optional.of(createClass(id = childId))
        every { classRepository.findById(parentId) } returns Optional.of(createClass(id = parentId))
        every { repository.findParent(childId) } returns Optional.empty()
        every { repository.existsChild(childId, parentId) } returns false
        every { relationRepository.saveAll(any()) } returns Unit

        service.create(ContributorId.UNKNOWN, parentId, setOf(childId), false)

        verify(exactly = 1) { relationRepository.saveAll(any()) }
    }

    @Test
    fun `given a class id, when searching for its children, when the class is not found, then an exception is thrown`() {
        val childId = ThingId("child")

        every { classRepository.findById(childId) } returns Optional.empty()

        val exception = assertThrows<ClassNotFound> {
            service.findChildren(childId, PageRequest.of(0, 5))
        }
        assertThat(exception.message).isEqualTo(ClassNotFound.withThingId(childId).message)
    }

    @Test
    fun `given a class id, when the children are fetched, it returns success`() {
        val childId = ThingId("child")
        val pageable = PageRequest.of(0, 5)

        every { classRepository.findById(childId) } returns Optional.of(createClass(id = childId))
        every { repository.findChildren(childId, pageable) } returns PageImpl(listOf())

        service.findChildren(childId, pageable)

        verify(exactly = 1) { repository.findChildren(childId, pageable) }
    }

    @Test
    fun `given a class id, when searching for its parent class, when the class is not found, then an exception is thrown`() {
        val childId = ThingId("child")

        every { classRepository.findById(childId) } returns Optional.empty()

        val exception = assertThrows<ClassNotFound> {
            service.findParent(childId)
        }
        assertThat(exception.message).isEqualTo(ClassNotFound.withThingId(childId).message)
    }

    @Test
    fun `given a class id, when the parent class is fetched, it returns success`() {
        val childId = ThingId("child")

        every { classRepository.findById(childId) } returns Optional.of(createClass(id = childId))
        every { repository.findParent(childId) } returns Optional.of(createClass(id = ThingId("parent")))

        service.findParent(childId)

        verify(exactly = 1) { repository.findParent(childId) }
    }

    @Test
    fun `given a class id, when the non-existing parent class is fetched, it returns success`() {
        val childId = ThingId("child")

        every { classRepository.findById(childId) } returns Optional.of(createClass(id = childId))
        every { repository.findParent(childId) } returns Optional.empty()

        service.findParent(childId)

        verify(exactly = 1) { repository.findParent(childId) }
    }

    @Test
    fun `given a class id, when searching for its root class, when the class is not found, then an exception is thrown`() {
        val childId = ThingId("child")

        every { classRepository.findById(childId) } returns Optional.empty()

        val exception = assertThrows<ClassNotFound> {
            service.findRoot(childId)
        }
        assertThat(exception.message).isEqualTo(ClassNotFound.withThingId(childId).message)
    }

    @Test
    fun `given a class id, when the root class is fetched, it returns success`() {
        val childId = ThingId("child")

        every { classRepository.findById(childId) } returns Optional.of(createClass(id = childId))
        every { repository.findRoot(childId) } returns Optional.of(createClass(id = ThingId("root")))

        service.findRoot(childId)

        verify(exactly = 1) { repository.findRoot(childId) }
    }

    @Test
    fun `given a class id, when the non-existing root class is fetched, it returns success`() {
        val childId = ThingId("child")

        every { classRepository.findById(childId) } returns Optional.of(createClass(id = childId))
        every { repository.findRoot(childId) } returns Optional.empty()

        service.findRoot(childId)

        verify(exactly = 1) { repository.findRoot(childId) }
    }

    @Test
    fun `given a class id, when searching for its class hierarchy, when the class is not found, then an exception is thrown`() {
        val childId = ThingId("child")

        every { classRepository.findById(childId) } returns Optional.empty()

        val exception = assertThrows<ClassNotFound> {
            service.findClassHierarchy(childId, PageRequest.of(0, 5))
        }
        assertThat(exception.message).isEqualTo(ClassNotFound.withThingId(childId).message)
    }

    @Test
    fun `given a class id, when searching for its class hierarchy, it returns success`() {
        val childId = ThingId("child")
        val pageable = PageRequest.of(0, 5)

        every { classRepository.findById(childId) } returns Optional.of(createClass(id = childId))
        every { repository.findClassHierarchy(childId, pageable) } returns PageImpl(listOf())

        service.findClassHierarchy(childId, pageable)

        verify(exactly = 1) { repository.findClassHierarchy(childId, pageable) }
    }

    @Test
    fun `given a class id, when counting its instances, when the class is not found, then an exception is thrown`() {
        val childId = ThingId("child")

        every { classRepository.findById(childId) } returns Optional.empty()

        val exception = assertThrows<ClassNotFound> {
            service.countClassInstances(childId)
        }
        assertThat(exception.message).isEqualTo(ClassNotFound.withThingId(childId).message)
    }

    @Test
    fun `given a class id, when counting its instances, it returns success`() {
        val childId = ThingId("child")

        every { classRepository.findById(childId) } returns Optional.of(createClass(id = childId))
        every { repository.countClassInstances(childId) } returns 5

        service.countClassInstances(childId)

        verify(exactly = 1) { repository.countClassInstances(childId) }
    }

    @Test
    fun `given a class relation is deleted, when the child class is not found, then an exception is thrown`() {
        val childId = ThingId("child")

        every { classRepository.findById(childId) } returns Optional.empty()

        val exception = assertThrows<ClassNotFound> {
            service.delete(childId)
        }
        assertThat(exception.message).isEqualTo(ClassNotFound.withThingId(childId).message)
    }

    @Test
    fun `given a class relation is deleted, it returns success`() {
        val childId = ThingId("child")

        every { classRepository.findById(childId) } returns Optional.of(createClass(id = childId))
        every { relationRepository.deleteByChildId(childId) } returns Unit

        service.delete(childId)

        verify(exactly = 1) { relationRepository.deleteByChildId(childId) }
    }
}
