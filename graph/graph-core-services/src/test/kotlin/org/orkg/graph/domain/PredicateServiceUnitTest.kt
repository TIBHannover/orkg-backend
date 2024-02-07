package org.orkg.graph.domain

import io.kotest.assertions.asClue
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import java.time.OffsetDateTime
import java.util.*
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.orkg.common.ContributorId
import org.orkg.common.ThingId
import org.orkg.graph.input.CreatePredicateUseCase
import org.orkg.graph.input.UpdatePredicateUseCase
import org.orkg.graph.output.PredicateRepository
import org.orkg.graph.output.StatementRepository
import org.orkg.graph.testing.fixtures.createPredicate
import org.orkg.testing.fixedClock

class PredicateServiceUnitTest {

    private val repository: PredicateRepository = mockk()
    private val statementRepository: StatementRepository = mockk()
    private val service = PredicateService(repository, statementRepository, fixedClock)

    @Test
    fun `given a predicate is created, when no id is given, then it gets an id from the repository`() {
        val mockPredicateId = ThingId("P1")
        every { repository.nextIdentity() } returns mockPredicateId
        every { repository.save(any()) } returns Unit

        service.create(CreatePredicateUseCase.CreateCommand(label = "irrelevant", id = null))

        verify(exactly = 1) { repository.nextIdentity() }
    }

    @Test
    fun `given a predicate is created, when an id is given, then it does not get a new id`() {
        val mockPredicateId = ThingId("P1")
        every { repository.findById(mockPredicateId) } returns Optional.empty()
        every { repository.save(any()) } returns Unit

        service.create(CreatePredicateUseCase.CreateCommand(label = "irrelevant", id = mockPredicateId))

        verify(exactly = 1) { repository.findById(mockPredicateId) }
        verify(exactly = 1) {
            repository.save(
                withArg {
                    it.id shouldBe mockPredicateId
                }
            )
        }
        verify(exactly = 0) { repository.nextIdentity() }
    }

    @Test
    fun `given a predicate is created, when an id is taken, then it throws an exception`() {
        val id = ThingId("P1")
        every { repository.findById(id) } returns Optional.of(createPredicate(id))

        assertThrows<PredicateAlreadyExists> {
            service.create(CreatePredicateUseCase.CreateCommand(id = id, label = "irrelevant"))
        }

        verify(exactly = 1) { repository.findById(id) }
    }

    @Test
    fun `given a predicate is created, when the label is invalid, then an exception is thrown`() {
        val mockPredicateId = ThingId("P1")
        every { repository.nextIdentity() } returns mockPredicateId

        val exception = assertThrows<InvalidLabel> {
            service.create(CreatePredicateUseCase.CreateCommand(label = " \t "))
        }
        assertThat(exception.message).isEqualTo("A label must not be blank or contain newlines and must be at most 8164 characters long.")
    }

    @Test
    fun `given a predicate is created, when no contributor is given, the anonymous user id is used`() {
        val mockPredicateId = ThingId("P1")
        every { repository.nextIdentity() } returns mockPredicateId
        every { repository.save(any()) } returns Unit

        service.create(CreatePredicateUseCase.CreateCommand(label = "irrelevant"))

        verify(exactly = 1) {
            repository.save(
                Predicate(
                    id = mockPredicateId,
                    label = "irrelevant",
                    createdAt = OffsetDateTime.now(fixedClock),
                    createdBy = ContributorId(UUID(0, 0)),
                )
            )
        }
    }

    @Test
    fun `given a predicate is created, when a contributor is given, the contributor id is used`() {
        val mockPredicateId = ThingId("P1")
        every { repository.nextIdentity() } returns mockPredicateId
        every { repository.save(any()) } returns Unit

        val randomContributorId = ContributorId(UUID.randomUUID())
        service.create(CreatePredicateUseCase.CreateCommand(label = "irrelevant", contributorId = randomContributorId))

        verify(exactly = 1) {
            repository.save(
                Predicate(
                    id = mockPredicateId,
                    label = "irrelevant",
                    createdAt = OffsetDateTime.now(fixedClock),
                    createdBy = randomContributorId,
                )
            )
        }
    }

    @Test
    fun `Given a predicate update command, when updating an unmodifiable predicate, it throws an exception`() {
        val predicate = createPredicate(modifiable = false)
        val label = "updated label"

        every { repository.findById(predicate.id) } returns Optional.of(predicate)

        shouldThrow<PredicateNotModifiable> {
            service.update(predicate.id, UpdatePredicateUseCase.ReplaceCommand(label))
        }.asClue {
            it.message shouldBe """Predicate "${predicate.id}" is not modifiable."""
        }

        verify(exactly = 1) { repository.findById(predicate.id) }
        verify(exactly = 0) { repository.save(any()) }
    }

    @Test
    fun `given a predicate is being deleted, when it is still used in a statement, an appropriate error is thrown`() {
        val mockPredicate = createPredicate()

        every { repository.findById(mockPredicate.id) } returns Optional.of(mockPredicate)
        every { statementRepository.countPredicateUsage(mockPredicate.id) } returns 1

        shouldThrow<PredicateUsedInStatement> {
            service.delete(mockPredicate.id)
        }

        verify(exactly = 0) { repository.deleteById(any()) }
    }

    @Test
    fun `given a predicate is being deleted, when it is not used in a statement, it gets deleted`() {
        val mockPredicate = createPredicate()

        every { repository.findById(mockPredicate.id) } returns Optional.of(mockPredicate)
        every { statementRepository.countPredicateUsage(mockPredicate.id) } returns 0
        every { repository.deleteById(mockPredicate.id) } returns Unit

        service.delete(mockPredicate.id)

        verify(exactly = 1) { repository.deleteById(mockPredicate.id) }
    }

    @Test
    fun `given a predicate is being deleted, when it is unmodifiable, it throws an exception`() {
        val predicate = createPredicate(modifiable = false)

        every { repository.findById(predicate.id) } returns Optional.of(predicate)

        shouldThrow<PredicateNotModifiable> { service.delete(predicate.id) }.asClue {
            it.message shouldBe """Predicate "${predicate.id}" is not modifiable."""
        }

        verify(exactly = 1) { repository.findById(predicate.id) }
        verify(exactly = 0) { repository.deleteById(any()) }
    }
}
