package eu.tib.orkg.prototype.statements.services

import eu.tib.orkg.prototype.contributions.domain.model.ContributorId
import eu.tib.orkg.prototype.createPredicate
import eu.tib.orkg.prototype.statements.api.CreatePredicateUseCase
import eu.tib.orkg.prototype.statements.application.PredicateCantBeDeleted
import eu.tib.orkg.prototype.statements.domain.model.Clock
import eu.tib.orkg.prototype.statements.domain.model.Predicate
import eu.tib.orkg.prototype.statements.domain.model.ThingId
import eu.tib.orkg.prototype.statements.spi.PredicateRepository
import eu.tib.orkg.prototype.statements.spi.StatementRepository
import io.kotest.assertions.throwables.shouldThrow
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import java.time.OffsetDateTime
import java.time.ZoneOffset
import java.util.*
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows

class PredicateServiceUnitTest {

    private val repository: PredicateRepository = mockk()
    private val statementRepository: StatementRepository = mockk()
    private val staticClock: Clock = object : Clock {
        override fun now(): OffsetDateTime = OffsetDateTime.of(2022, 11, 29, 11, 20, 33, 12345, ZoneOffset.ofHours(1))
    }

    private val service = PredicateService(repository, statementRepository, staticClock)

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
        every { repository.save(any()) } returns Unit

        service.create(CreatePredicateUseCase.CreateCommand(label = "irrelevant", id = mockPredicateId.value))

        verify(exactly = 0) { repository.nextIdentity() }
    }

    @Test
    fun `given a predicate is created, when the id is invalid, then an exception is thrown`() {
        val exception = assertThrows<IllegalArgumentException> {
            service.create(CreatePredicateUseCase.CreateCommand(label = "irrelevant", id = "!invalid"))
        }
        assertThat(exception.message).isEqualTo("Must only contain alphanumeric characters, dashes and underscores")
    }

    @Test
    fun `given a predicate is created, when the label is invalid, then an exception is thrown`() {
        val mockPredicateId = ThingId("P1")
        every { repository.nextIdentity() } returns mockPredicateId

        val exception = assertThrows<IllegalArgumentException> {
            service.create(CreatePredicateUseCase.CreateCommand(label = " \t "))
        }
        assertThat(exception.message).isEqualTo("Invalid label:  \t ")
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
                    createdAt = staticClock.now(),
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
                    createdAt = staticClock.now(),
                    createdBy = randomContributorId,
                )
            )
        }
    }

    @Test
    fun `given a predicate is being deleted, when it is still used in a statement, an appropriate error is thrown`() {
        val mockPredicate = createPredicate()

        every { repository.findById(mockPredicate.id) } returns Optional.of(mockPredicate)
        every { statementRepository.countPredicateUsage(mockPredicate.id) } returns 1

        shouldThrow<PredicateCantBeDeleted> {
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
}
