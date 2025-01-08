package org.orkg.graph.domain

import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.just
import io.mockk.mockk
import io.mockk.runs
import io.mockk.verify
import java.util.*
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.orkg.common.ContributorId
import org.orkg.common.ThingId
import org.orkg.common.testing.fixtures.MockkBaseTest
import org.orkg.common.testing.fixtures.fixedClock
import org.orkg.graph.input.CreateListUseCase
import org.orkg.graph.input.UpdateListUseCase
import org.orkg.graph.output.ListRepository
import org.orkg.graph.output.ThingRepository
import org.orkg.graph.testing.fixtures.createClass
import org.orkg.graph.testing.fixtures.createList
import org.orkg.graph.testing.fixtures.createLiteral
import org.orkg.graph.testing.fixtures.createPredicate
import org.orkg.graph.testing.fixtures.createResource
import org.springframework.data.domain.PageImpl
import org.springframework.data.domain.PageRequest

internal class ListServiceUnitTest : MockkBaseTest {
    private val repository: ListRepository = mockk()
    private val thingRepository: ThingRepository = mockk()
    private val service = ListService(repository, thingRepository, fixedClock)

    @Test
    fun `given a list is created, when valid inputs are provided, it returns success`() {
        val command = CreateListUseCase.CreateCommand(
            id = ThingId("List1"),
            contributorId = ContributorId(UUID.randomUUID()),
            label = "label",
            elements = listOf(ThingId("R1")),
            modifiable = false
        )

        every { thingRepository.findByThingId(command.id!!) } returns Optional.empty()
        every { thingRepository.existsAll(command.elements.toSet()) } returns true
        every { repository.save(any(), any()) } just runs

        val result = service.create(command)
        result shouldBe command.id

        verify(exactly = 1) { thingRepository.findByThingId(command.id!!) }
        verify(exactly = 1) { thingRepository.existsAll(command.elements.toSet()) }
        verify(exactly = 1) { repository.save(any(), any()) }
    }

    @Test
    fun `given a list is created, when label is invalid, then an exception is thrown`() {
        val command = CreateListUseCase.CreateCommand(
            label = "\n",
            elements = listOf(ThingId("R1")),
            id = ThingId("List1"),
            contributorId = ContributorId(UUID.randomUUID())
        )

        assertThrows<InvalidLabel> { service.create(command) }
    }

    @Test
    fun `given a list is created, when not all elements exist, then an exception is thrown`() {
        val command = CreateListUseCase.CreateCommand(
            id = ThingId("List1"),
            contributorId = ContributorId(UUID.randomUUID()),
            label = "label",
            elements = listOf(ThingId("R1"))
        )

        every { thingRepository.findByThingId(command.id!!) } returns Optional.empty()
        every { thingRepository.existsAll(command.elements.toSet()) } returns false

        assertThrows<ListElementNotFound> { service.create(command) }

        verify(exactly = 1) { thingRepository.findByThingId(command.id!!) }
        verify(exactly = 1) { thingRepository.existsAll(command.elements.toSet()) }
    }

    @Test
    fun `given a list is created, when no id is given, then it gets an id from the repository`() {
        val command = CreateListUseCase.CreateCommand(
            label = "label",
            elements = listOf(ThingId("R1")),
            id = null,
            contributorId = ContributorId(UUID.randomUUID())
        )
        val id = ThingId("1")

        every { repository.nextIdentity() } returns id
        every { thingRepository.existsAll(command.elements.toSet()) } returns true
        every { repository.save(any(), any()) } just runs

        val result = service.create(command)
        result shouldBe id

        verify(exactly = 1) { repository.nextIdentity() }
        verify(exactly = 1) { thingRepository.existsAll(command.elements.toSet()) }
        verify(exactly = 1) { repository.save(any(), any()) }
    }

    @Test
    fun `given a list is updated, when valid inputs are provided, it returns success`() {
        val id = ThingId("List1")
        val command = UpdateListUseCase.UpdateCommand(
            id = id,
            contributorId = ContributorId.UNKNOWN,
            label = "label",
            elements = listOf(ThingId("R1"))
        )
        val list = createList(id)
        val expected = list.copy(
            label = command.label!!,
            elements = command.elements!!
        )

        every { repository.findById(id) } returns Optional.of(list)
        every { thingRepository.existsAll(command.elements!!.toSet()) } returns true
        every { repository.save(any(), any()) } just runs

        service.update(command)

        verify(exactly = 1) { repository.findById(id) }
        verify(exactly = 1) { thingRepository.existsAll(command.elements!!.toSet()) }
        verify(exactly = 1) { repository.save(expected, ContributorId.UNKNOWN) }
    }

    @Test
    fun `given a list is updated, when list does not exist, then an exception is thrown`() {
        val id = ThingId("List1")
        val command = UpdateListUseCase.UpdateCommand(
            id = id,
            contributorId = ContributorId.UNKNOWN,
            label = "label",
            elements = listOf(ThingId("R1"))
        )

        every { repository.findById(id) } returns Optional.empty()

        assertThrows<ListNotFound> { service.update(command) }

        verify(exactly = 1) { repository.findById(id) }
    }

    @Test
    fun `given a list is updated, when label is invalid, then an exception is thrown`() {
        val id = ThingId("List1")
        val command = UpdateListUseCase.UpdateCommand(
            id = id,
            contributorId = ContributorId.UNKNOWN,
            label = "\n",
            elements = listOf(ThingId("R1"))
        )
        val list = createList(id = id)

        every { repository.findById(id) } returns Optional.of(list)

        assertThrows<InvalidLabel> { service.update(command) }

        verify(exactly = 1) { repository.findById(id) }
    }

    @Test
    fun `given a list is updated, when not all elements exist, then an exception is thrown`() {
        val id = ThingId("List1")
        val command = UpdateListUseCase.UpdateCommand(
            id = id,
            contributorId = ContributorId.UNKNOWN,
            label = "label",
            elements = listOf(ThingId("R1"))
        )
        val list = createList(id = id)

        every { repository.findById(id) } returns Optional.of(list)
        every { thingRepository.existsAll(command.elements!!.toSet()) } returns false

        assertThrows<ListElementNotFound> { service.update(command) }

        verify(exactly = 1) { repository.findById(id) }
        verify(exactly = 1) { thingRepository.existsAll(command.elements!!.toSet()) }
    }

    @Test
    fun `given a list is updated, when only the label is updated, it returns success`() {
        val id = ThingId("List1")
        val command = UpdateListUseCase.UpdateCommand(
            id = id,
            contributorId = ContributorId.UNKNOWN,
            label = "label",
            elements = null
        )
        val list = createList(id = id)
        val expected = list.copy(
            label = command.label!!
        )

        every { repository.findById(id) } returns Optional.of(list)
        every { repository.save(any(), any()) } just runs

        service.update(command)

        verify(exactly = 1) { repository.findById(id) }
        verify(exactly = 1) { repository.save(expected, ContributorId.UNKNOWN) }
    }

    @Test
    fun `given a list is updated, when only the elements are updated, it returns success`() {
        val id = ThingId("List1")
        val command = UpdateListUseCase.UpdateCommand(
            id = id,
            contributorId = ContributorId.UNKNOWN,
            label = null,
            elements = listOf(ThingId("R1"))
        )
        val list = createList(id = id)
        val expected = list.copy(
            elements = command.elements!!
        )

        every { repository.findById(id) } returns Optional.of(list)
        every { thingRepository.existsAll(command.elements!!.toSet()) } returns true
        every { repository.save(any(), any()) } just runs

        service.update(command)

        verify(exactly = 1) { repository.findById(id) }
        verify(exactly = 1) { thingRepository.existsAll(command.elements!!.toSet()) }
        verify(exactly = 1) { repository.save(expected, ContributorId.UNKNOWN) }
    }

    @Test
    fun `given a list is updated, when elements are empty, it returns success`() {
        val id = ThingId("List1")
        val command = UpdateListUseCase.UpdateCommand(
            id = id,
            contributorId = ContributorId.UNKNOWN,
            label = "label",
            elements = listOf()
        )
        val list = createList(id = id, elements = listOf(ThingId("R25")))
        val expected = list.copy(
            label = command.label!!,
            elements = command.elements!!
        )

        every { repository.findById(id) } returns Optional.of(list)
        every { repository.save(any(), any()) } just runs

        service.update(command)

        verify(exactly = 1) { repository.findById(id) }
        verify(exactly = 1) { repository.save(expected, ContributorId.UNKNOWN) }
    }

    @Test
    fun `given a list is updated, when list is unmodifiable, it throws an exception`() {
        val id = ThingId("List1")
        val command = UpdateListUseCase.UpdateCommand(
            id = id,
            contributorId = ContributorId.UNKNOWN,
            label = "label",
            elements = listOf()
        )
        val list = createList(id = id, elements = listOf(ThingId("R25")), modifiable = false)

        every { repository.findById(id) } returns Optional.of(list)

        assertThrows<ListNotModifiable> { service.update(command) }

        verify(exactly = 1) { repository.findById(id) }
    }

    @Test
    fun `given a list, when its elements are fetched, it returns success`() {
        val id = ThingId("List1")
        val elements = listOf(
            createResource(),
            createLiteral(),
            createClass(),
            createPredicate()
        )
        val pageable = PageRequest.of(0, 5)

        every { repository.exists(id) } returns true
        every { repository.findAllElementsById(id, any()) } returns PageImpl(elements)

        service.findAllElementsById(id, pageable)

        verify(exactly = 1) { repository.exists(id) }
        verify(exactly = 1) { repository.findAllElementsById(id, any()) }
    }

    @Test
    fun `given a list, when its elements are fetched but the list does not exist, then an exception is thrown`() {
        val id = ThingId("List1")
        val pageable = PageRequest.of(0, 5)

        every { repository.exists(id) } returns false

        assertThrows<ListNotFound> { service.findAllElementsById(id, pageable) }

        verify(exactly = 1) { repository.exists(id) }
    }

    @Test
    fun `given a list is being deleted, when it still exists, it deletes the list`() {
        val id = ThingId("List1")
        val list = createList(id)

        every { repository.findById(id) } returns Optional.of(list)
        every { thingRepository.isUsedAsObject(id) } returns false
        every { repository.delete(id) } just runs

        service.delete(id)

        verify(exactly = 1) { repository.findById(id) }
        verify(exactly = 1) { thingRepository.isUsedAsObject(id) }
        verify(exactly = 1) { repository.delete(id) }
    }

    @Test
    fun `given a list is being deleted, when it is already deleted, it returns success`() {
        val id = ThingId("List1")

        every { repository.findById(id) } returns Optional.empty()

        service.delete(id)

        verify(exactly = 1) { repository.findById(id) }
    }

    @Test
    fun `given a list is being deleted, when list is unmodifiable, then an exception is thrown`() {
        val id = ThingId("List1")
        val list = createList(id, modifiable = false)

        every { repository.findById(id) } returns Optional.of(list)

        assertThrows<ListNotModifiable> { service.delete(id) }

        verify(exactly = 1) { repository.findById(id) }
    }

    @Test
    fun `given a list is being deleted, when list is still used, then it throws an exception`() {
        val id = ThingId("List1")
        val list = createList(id)

        every { repository.findById(id) } returns Optional.of(list)
        every { thingRepository.isUsedAsObject(id) } returns true

        assertThrows<ListInUse> { service.delete(id) }

        verify(exactly = 1) { repository.findById(id) }
        verify(exactly = 1) { thingRepository.isUsedAsObject(id) }
    }
}
