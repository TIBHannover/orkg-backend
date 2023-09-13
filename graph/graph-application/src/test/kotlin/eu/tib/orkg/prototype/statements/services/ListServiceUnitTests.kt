package eu.tib.orkg.prototype.statements.services

import eu.tib.orkg.prototype.community.domain.model.ContributorId
import eu.tib.orkg.prototype.createClass
import eu.tib.orkg.prototype.createLiteral
import eu.tib.orkg.prototype.createPredicate
import eu.tib.orkg.prototype.createResource
import eu.tib.orkg.prototype.statements.api.CreateListUseCase
import eu.tib.orkg.prototype.statements.api.UpdateListUseCase
import eu.tib.orkg.prototype.statements.application.InvalidLabel
import eu.tib.orkg.prototype.statements.application.ListElementNotFound
import eu.tib.orkg.prototype.statements.application.ListNotFound
import eu.tib.orkg.prototype.statements.domain.model.List
import eu.tib.orkg.prototype.statements.domain.model.ThingId
import eu.tib.orkg.prototype.statements.spi.ListRepository
import eu.tib.orkg.prototype.statements.spi.ThingRepository
import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.just
import io.mockk.mockk
import io.mockk.runs
import io.mockk.verify
import java.time.Instant
import java.time.OffsetDateTime
import java.time.ZoneId
import java.time.ZoneOffset
import java.util.*
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.orkg.statements.testing.createList
import org.springframework.data.domain.PageImpl
import org.springframework.data.domain.PageRequest

class ListServiceUnitTests {

    private val repository: ListRepository = mockk()
    private val thingRepository: ThingRepository = mockk()
    private val fixedTime = OffsetDateTime.of(2023, 5, 9, 14, 51, 25, 12345, ZoneOffset.ofHours(1))
    private val staticClock = java.time.Clock.fixed(Instant.from(fixedTime), ZoneId.systemDefault())
    private val service = ListService(repository, thingRepository, staticClock)

    @Test
    fun `given a list is created, when valid inputs are provided, it returns success`() {
        val command = CreateListUseCase.CreateCommand(
            label = "label",
            elements = listOf(ThingId("R1")),
            id = ThingId("List1"),
            contributorId = ContributorId(UUID.randomUUID())
        )

        every { thingRepository.existsAll(command.elements.toSet()) } returns true
        every { repository.save(any(), any()) } just runs

        val result = service.create(command)
        result shouldBe command.id

        verify(exactly = 0) { repository.nextIdentity() }
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

        assertThrows<InvalidLabel> {
            service.create(command)
        }

        verify(exactly = 0) { repository.nextIdentity() }
        verify(exactly = 0) { repository.save(any(), any()) }
    }

    @Test
    fun `given a list is created, when not all elements exist, then an exception is thrown`() {
        val command = CreateListUseCase.CreateCommand(
            label = "label",
            elements = listOf(ThingId("R1")),
            id = ThingId("List1"),
            contributorId = ContributorId(UUID.randomUUID())
        )

        every { thingRepository.existsAll(command.elements.toSet()) } returns false

        assertThrows<ListElementNotFound> {
            service.create(command)
        }

        verify(exactly = 0) { repository.nextIdentity() }
        verify(exactly = 0) { repository.save(any(), any()) }
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
        verify(exactly = 1) { repository.save(any(), any()) }
    }

    @Test
    fun `given a list is created, when no contributor is given, then unknown contributor id gets used`() {
        val command = CreateListUseCase.CreateCommand(
            label = "label",
            elements = listOf(ThingId("R1")),
            id = ThingId("List1"),
            contributorId = null
        )

        every { thingRepository.existsAll(command.elements.toSet()) } returns true
        every { repository.save(any(), any()) } just runs

        service.create(command)

        verify(exactly = 0) { repository.nextIdentity() }
        verify(exactly = 1) {
            repository.save(
                List(
                    id = command.id!!,
                    label = command.label,
                    elements = command.elements,
                    createdAt = OffsetDateTime.now(staticClock),
                    createdBy = ContributorId.createUnknownContributor()
                ),
                ContributorId.createUnknownContributor()
            )
        }
    }

    @Test
    fun `given a list is updated, when valid inputs are provided, it returns success`() {
        val id = ThingId("List1")
        val command = UpdateListUseCase.UpdateCommand(
            label = "label",
            elements = listOf(ThingId("R1"))
        )
        val list = createList(id = id)
        val expected = list.copy(
            label = command.label!!,
            elements = command.elements!!
        )

        every { repository.findById(id) } returns Optional.of(list)
        every { thingRepository.existsAll(command.elements!!.toSet()) } returns true
        every { repository.save(any(), any()) } just runs

        service.update(id, command)

        verify(exactly = 1) { repository.save(expected, ContributorId.createUnknownContributor()) }
    }

    @Test
    fun `given a list is updated, when list does not exist, then an exception is thrown`() {
        val id = ThingId("List1")
        val command = UpdateListUseCase.UpdateCommand(
            label = "label",
            elements = listOf(ThingId("R1"))
        )

        every { repository.findById(id) } returns Optional.empty()

        assertThrows<ListNotFound> {
            service.update(id, command)
        }

        verify(exactly = 0) { repository.save(any(), any()) }
    }

    @Test
    fun `given a list is updated, when label is invalid, then an exception is thrown`() {
        val id = ThingId("List1")
        val command = UpdateListUseCase.UpdateCommand(
            label = "\n",
            elements = listOf(ThingId("R1"))
        )
        val list = createList(id = id)

        every { repository.findById(id) } returns Optional.of(list)

        assertThrows<InvalidLabel> {
            service.update(id, command)
        }

        verify(exactly = 0) { repository.save(any(), any()) }
    }

    @Test
    fun `given a list is updated, when not all elements exist, then an exception is thrown`() {
        val id = ThingId("List1")
        val command = UpdateListUseCase.UpdateCommand(
            label = "label",
            elements = listOf(ThingId("R1"))
        )
        val list = createList(id = id)

        every { repository.findById(id) } returns Optional.of(list)
        every { thingRepository.existsAll(command.elements!!.toSet()) } returns false

        assertThrows<ListElementNotFound> {
            service.update(id, command)
        }

        verify(exactly = 0) { repository.save(any(), any()) }
    }

    @Test
    fun `given a list is updated, when only the label is updated, it returns success`() {
        val id = ThingId("List1")
        val command = UpdateListUseCase.UpdateCommand(
            label = "label",
            elements = null
        )
        val list = createList(id = id)
        val expected = list.copy(
            label = command.label!!
        )

        every { repository.findById(id) } returns Optional.of(list)
        every { repository.save(any(), any()) } just runs

        service.update(id, command)

        verify(exactly = 0) { thingRepository.existsAll(any()) }
        verify(exactly = 1) { repository.save(expected, ContributorId.createUnknownContributor()) }
    }

    @Test
    fun `given a list is updated, when only the elements are updated, it returns success`() {
        val id = ThingId("List1")
        val command = UpdateListUseCase.UpdateCommand(
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

        service.update(id, command)

        verify(exactly = 1) { repository.save(expected, ContributorId.createUnknownContributor()) }
    }

    @Test
    fun `given a list is updated, when elements are empty, it returns success`() {
        val id = ThingId("List1")
        val command = UpdateListUseCase.UpdateCommand(
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

        service.update(id, command)

        verify(exactly = 1) { repository.findById(id) }
        verify(exactly = 1) { repository.save(expected, ContributorId.createUnknownContributor()) }
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

        assertThrows<ListNotFound> {
            service.findAllElementsById(id, pageable)
        }

        verify(exactly = 1) { repository.exists(id) }
        verify(exactly = 0) { repository.findAllElementsById(id, any()) }
    }
}
