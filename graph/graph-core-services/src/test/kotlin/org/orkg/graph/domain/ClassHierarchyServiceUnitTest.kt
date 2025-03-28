package org.orkg.graph.domain

import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.orkg.common.ContributorId
import org.orkg.common.ThingId
import org.orkg.common.testing.fixtures.MockkBaseTest
import org.orkg.common.testing.fixtures.fixedClock
import org.orkg.graph.output.ClassHierarchyRepository
import org.orkg.graph.output.ClassRelationRepository
import org.orkg.graph.output.ClassRepository
import org.orkg.graph.testing.fixtures.createClass
import org.springframework.data.domain.PageImpl
import org.springframework.data.domain.PageRequest
import java.util.Optional

internal class ClassHierarchyServiceUnitTest : MockkBaseTest {
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

        verify(exactly = 1) { classRepository.findById(parentId) }
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

        verify(exactly = 1) { classRepository.findById(parentId) }
        verify(exactly = 1) { repository.existsChildren(parentId) }
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

        verify(exactly = 1) { classRepository.findById(parentId) }
        verify(exactly = 1) { classRepository.findById(childId) }
    }

    @Test
    fun `given a class relation is created, when the parent class cannot be found, then an exception is thrown`() {
        val parentId = ThingId("parent")
        val childId = ThingId("child")

        every { classRepository.findById(parentId) } returns Optional.empty()

        val exception = assertThrows<ClassNotFound> {
            service.create(ContributorId.UNKNOWN, parentId, setOf(childId), false)
        }
        assertThat(exception.message).isEqualTo(ClassNotFound.withThingId(parentId).message)

        verify(exactly = 1) { classRepository.findById(parentId) }
    }

    @Test
    fun `given a class relation is created, when the child already has a parent class, then an exception is thrown`() {
        val parentId = ThingId("parent")
        val childId = ThingId("child")

        every { classRepository.findById(childId) } returns Optional.of(createClass(id = childId))
        every { classRepository.findById(parentId) } returns Optional.of(createClass(id = parentId))
        every { repository.findParentByChildId(childId) } returns Optional.of(createClass(id = parentId))

        val exception = assertThrows<ParentClassAlreadyExists> {
            service.create(ContributorId.UNKNOWN, parentId, setOf(childId), false)
        }
        assertThat(exception.message).isEqualTo(ParentClassAlreadyExists(childId, parentId).message)

        verify(exactly = 1) { classRepository.findById(childId) }
        verify(exactly = 1) { classRepository.findById(parentId) }
        verify(exactly = 1) { repository.findParentByChildId(childId) }
    }

    @Test
    fun `given a class relation is created, when the child already has the parent class as a child, then an exception is thrown`() {
        val parentId = ThingId("parent")
        val childId = ThingId("child")

        every { classRepository.findById(childId) } returns Optional.of(createClass(id = childId))
        every { classRepository.findById(parentId) } returns Optional.of(createClass(id = parentId))
        every { repository.findParentByChildId(childId) } returns Optional.empty()
        every { repository.existsChild(childId, parentId) } returns true

        val exception = assertThrows<InvalidSubclassRelation> {
            service.create(ContributorId.UNKNOWN, parentId, setOf(childId), false)
        }
        assertThat(exception.message).isEqualTo(InvalidSubclassRelation(childId, parentId).message)

        verify(exactly = 1) { classRepository.findById(childId) }
        verify(exactly = 1) { classRepository.findById(parentId) }
        verify(exactly = 1) { repository.findParentByChildId(childId) }
        verify(exactly = 1) { repository.existsChild(childId, parentId) }
    }

    @Test
    fun `given a class relation is created, it returns success`() {
        val parentId = ThingId("parent")
        val childId = ThingId("child")

        every { classRepository.findById(childId) } returns Optional.of(createClass(id = childId))
        every { classRepository.findById(parentId) } returns Optional.of(createClass(id = parentId))
        every { repository.findParentByChildId(childId) } returns Optional.empty()
        every { repository.existsChild(childId, parentId) } returns false
        every { relationRepository.saveAll(any()) } returns Unit

        service.create(ContributorId.UNKNOWN, parentId, setOf(childId), false)

        verify(exactly = 1) { classRepository.findById(childId) }
        verify(exactly = 1) { classRepository.findById(parentId) }
        verify(exactly = 1) { repository.findParentByChildId(childId) }
        verify(exactly = 1) { repository.existsChild(childId, parentId) }
        verify(exactly = 1) { relationRepository.saveAll(any()) }
    }

    @Test
    fun `given a class id, when searching for its children, when the class is not found, then an exception is thrown`() {
        val childId = ThingId("child")

        every { classRepository.findById(childId) } returns Optional.empty()

        val exception = assertThrows<ClassNotFound> {
            service.findAllChildrenByAncestorId(childId, PageRequest.of(0, 5))
        }
        assertThat(exception.message).isEqualTo(ClassNotFound.withThingId(childId).message)

        verify(exactly = 1) { classRepository.findById(childId) }
    }

    @Test
    fun `given a class id, when the children are fetched, it returns success`() {
        val childId = ThingId("child")
        val pageable = PageRequest.of(0, 5)

        every { classRepository.findById(childId) } returns Optional.of(createClass(id = childId))
        every { repository.findAllChildrenByAncestorId(childId, pageable) } returns PageImpl(listOf())

        service.findAllChildrenByAncestorId(childId, pageable)

        verify(exactly = 1) { classRepository.findById(childId) }
        verify(exactly = 1) { repository.findAllChildrenByAncestorId(childId, pageable) }
    }

    @Test
    fun `given a class id, when searching for its parent class, when the class is not found, then an exception is thrown`() {
        val childId = ThingId("child")

        every { classRepository.findById(childId) } returns Optional.empty()

        val exception = assertThrows<ClassNotFound> {
            service.findParentByChildId(childId)
        }
        assertThat(exception.message).isEqualTo(ClassNotFound.withThingId(childId).message)

        verify(exactly = 1) { classRepository.findById(childId) }
    }

    @Test
    fun `given a class id, when the parent class is fetched, it returns success`() {
        val childId = ThingId("child")

        every { classRepository.findById(childId) } returns Optional.of(createClass(id = childId))
        every { repository.findParentByChildId(childId) } returns Optional.of(createClass(id = ThingId("parent")))

        service.findParentByChildId(childId)

        verify(exactly = 1) { classRepository.findById(childId) }
        verify(exactly = 1) { repository.findParentByChildId(childId) }
    }

    @Test
    fun `given a class id, when the non-existing parent class is fetched, it returns success`() {
        val childId = ThingId("child")

        every { classRepository.findById(childId) } returns Optional.of(createClass(id = childId))
        every { repository.findParentByChildId(childId) } returns Optional.empty()

        service.findParentByChildId(childId)

        verify(exactly = 1) { classRepository.findById(childId) }
        verify(exactly = 1) { repository.findParentByChildId(childId) }
    }

    @Test
    fun `given a class id, when searching for its root class, when the class is not found, then an exception is thrown`() {
        val childId = ThingId("child")

        every { classRepository.findById(childId) } returns Optional.empty()

        val exception = assertThrows<ClassNotFound> {
            service.findRootByDescendantId(childId)
        }
        assertThat(exception.message).isEqualTo(ClassNotFound.withThingId(childId).message)

        verify(exactly = 1) { classRepository.findById(childId) }
    }

    @Test
    fun `given a class id, when the root class is fetched, it returns success`() {
        val childId = ThingId("child")

        every { classRepository.findById(childId) } returns Optional.of(createClass(id = childId))
        every { repository.findRootByDescendantId(childId) } returns Optional.of(createClass(id = ThingId("root")))

        service.findRootByDescendantId(childId)

        verify(exactly = 1) { classRepository.findById(childId) }
        verify(exactly = 1) { repository.findRootByDescendantId(childId) }
    }

    @Test
    fun `given a class id, when the non-existing root class is fetched, it returns success`() {
        val childId = ThingId("child")

        every { classRepository.findById(childId) } returns Optional.of(createClass(id = childId))
        every { repository.findRootByDescendantId(childId) } returns Optional.empty()

        service.findRootByDescendantId(childId)

        verify(exactly = 1) { classRepository.findById(childId) }
        verify(exactly = 1) { repository.findRootByDescendantId(childId) }
    }

    @Test
    fun `given a class id, when searching for its class hierarchy, when the class is not found, then an exception is thrown`() {
        val childId = ThingId("child")

        every { classRepository.findById(childId) } returns Optional.empty()

        val exception = assertThrows<ClassNotFound> {
            service.findClassHierarchy(childId, PageRequest.of(0, 5))
        }
        assertThat(exception.message).isEqualTo(ClassNotFound.withThingId(childId).message)

        verify(exactly = 1) { classRepository.findById(childId) }
    }

    @Test
    fun `given a class id, when searching for its class hierarchy, it returns success`() {
        val childId = ThingId("child")
        val pageable = PageRequest.of(0, 5)

        every { classRepository.findById(childId) } returns Optional.of(createClass(id = childId))
        every { repository.findClassHierarchy(childId, pageable) } returns PageImpl(listOf())

        service.findClassHierarchy(childId, pageable)

        verify(exactly = 1) { classRepository.findById(childId) }
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

        verify(exactly = 1) { classRepository.findById(childId) }
    }

    @Test
    fun `given a class id, when counting its instances, it returns success`() {
        val childId = ThingId("child")

        every { classRepository.findById(childId) } returns Optional.of(createClass(id = childId))
        every { repository.countClassInstances(childId) } returns 5

        service.countClassInstances(childId)

        verify(exactly = 1) { classRepository.findById(childId) }
        verify(exactly = 1) { repository.countClassInstances(childId) }
    }

    @Test
    fun `given a class relation is deleted, when the child class is not found, then an exception is thrown`() {
        val childId = ThingId("child")

        every { classRepository.findById(childId) } returns Optional.empty()

        val exception = assertThrows<ClassNotFound> {
            service.deleteByChildId(childId)
        }
        assertThat(exception.message).isEqualTo(ClassNotFound.withThingId(childId).message)

        verify(exactly = 1) { classRepository.findById(childId) }
    }

    @Test
    fun `given a class relation is deleted, it returns success`() {
        val childId = ThingId("child")

        every { classRepository.findById(childId) } returns Optional.of(createClass(id = childId))
        every { relationRepository.deleteByChildId(childId) } returns Unit

        service.deleteByChildId(childId)

        verify(exactly = 1) { classRepository.findById(childId) }
        verify(exactly = 1) { relationRepository.deleteByChildId(childId) }
    }
}
