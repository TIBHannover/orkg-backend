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
import org.orkg.community.output.ContributorRepository
import org.orkg.community.testing.fixtures.createContributor
import org.orkg.graph.input.CreatePredicateUseCase
import org.orkg.graph.input.UpdatePredicateUseCase
import org.orkg.graph.output.PredicateRepository
import org.orkg.graph.output.StatementRepository
import org.orkg.graph.testing.fixtures.createPredicate
import org.orkg.testing.MockUserId
import org.orkg.testing.fixedClock

class PredicateServiceUnitTest {

    private val repository: PredicateRepository = mockk()
    private val statementRepository: StatementRepository = mockk()
    private val contributorRepository: ContributorRepository = mockk()
    private val service = PredicateService(repository, statementRepository, contributorRepository, fixedClock)

    @Test
    fun `given a predicate is created, when no id is given, then it gets an id from the repository`() {
        val mockPredicateId = ThingId("P1")
        every { repository.nextIdentity() } returns mockPredicateId
        every { repository.save(any()) } returns Unit

        service.create(CreatePredicateUseCase.CreateCommand(label = "irrelevant", id = null)) shouldBe mockPredicateId

        verify(exactly = 1) { repository.nextIdentity() }
    }

    @Test
    fun `given a predicate is created, when an id is given, then it does not get a new id`() {
        val mockPredicateId = ThingId("P1")
        every { repository.findById(mockPredicateId) } returns Optional.empty()
        every { repository.save(any()) } returns Unit

        service.create(CreatePredicateUseCase.CreateCommand(label = "irrelevant", id = mockPredicateId)) shouldBe mockPredicateId

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

        service.create(CreatePredicateUseCase.CreateCommand(label = "irrelevant")) shouldBe mockPredicateId

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
        service.create(
            CreatePredicateUseCase.CreateCommand(
                label = "irrelevant",
                contributorId = randomContributorId
            )
        ) shouldBe mockPredicateId

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
        val couldBeAnyone = ContributorId(MockUserId.USER)

        every { repository.findById(mockPredicate.id) } returns Optional.of(mockPredicate)
        every { statementRepository.countPredicateUsage(mockPredicate.id) } returns 1

        shouldThrow<PredicateUsedInStatement> {
            service.delete(mockPredicate.id, couldBeAnyone)
        }

        verify(exactly = 0) { repository.deleteById(any()) }
    }

    @Test
    fun `given a predicate is being deleted, when it is not used in a statement, and it is owned by the user, it gets deleted`() {
        val theOwningContributorId = ContributorId(MockUserId.USER)
        val theOwningContributor = createContributor(id = theOwningContributorId)
        val predicate = createPredicate(createdBy = theOwningContributorId)

        every { repository.findById(predicate.id) } returns Optional.of(predicate)
        every { statementRepository.countPredicateUsage(predicate.id) } returns 0
        every { contributorRepository.findById(theOwningContributorId) } returns Optional.of(theOwningContributor)
        every { repository.deleteById(predicate.id) } returns Unit

        service.delete(predicate.id, theOwningContributorId)

        verify(exactly = 1) { repository.findById(predicate.id) }
        verify(exactly = 1) { statementRepository.countPredicateUsage(predicate.id) }
        verify(exactly = 1) { repository.deleteById(predicate.id) }
    }

    @Test
    fun `given a predicate is being deleted, when it is not used in a statement, and it is not owned by the user, but the user is a curator, it gets deleted`() {
        val theOwningContributorId = ContributorId(MockUserId.USER)
        val aCurator = createContributor(id = ContributorId(MockUserId.CURATOR), isCurator = true)
        val predicate = createPredicate(createdBy = theOwningContributorId)

        every { repository.findById(predicate.id) } returns Optional.of(predicate)
        every { statementRepository.countPredicateUsage(predicate.id) } returns 0
        every { contributorRepository.findById(aCurator.id) } returns Optional.of(aCurator)
        every { repository.deleteById(predicate.id) } returns Unit

        service.delete(predicate.id, aCurator.id)

        verify(exactly = 1) { repository.findById(predicate.id) }
        verify(exactly = 1) { statementRepository.countPredicateUsage(predicate.id) }
        verify(exactly = 1) { repository.deleteById(predicate.id) }
        verify(exactly = 1) { contributorRepository.findById(aCurator.id) }
    }

    @Test
    fun `given a predicate is being deleted, when it is not used in a statement, and it is not owned by the user, and the user is not a curator, it throws an exception`() {
        val theOwningContributorId = ContributorId("1255bbe4-1850-4033-ba10-c80d4b370e3e")
        val loggedInUserId = ContributorId(MockUserId.USER)
        val loggedInUser = createContributor(id = loggedInUserId)
        val predicate = createPredicate(createdBy = theOwningContributorId)

        every { repository.findById(predicate.id) } returns Optional.of(predicate)
        every { statementRepository.countPredicateUsage(predicate.id) } returns 0
        every { contributorRepository.findById(loggedInUserId) } returns Optional.of(loggedInUser)
        every { repository.deleteById(predicate.id) } returns Unit

        shouldThrow<NeitherOwnerNorCurator> {
            service.delete(predicate.id, loggedInUserId)
        }

        verify(exactly = 1) { repository.findById(predicate.id) }
        verify(exactly = 1) { statementRepository.countPredicateUsage(predicate.id) }
        verify(exactly = 0) { repository.deleteById(predicate.id) }
        verify(exactly = 1) { contributorRepository.findById(loggedInUserId) }
    }

    @Test
    fun `given a predicate is being deleted, when it is unmodifiable, it throws an exception`() {
        val predicate = createPredicate(modifiable = false)
        val loggedInUser = ContributorId("89b13df4-22ae-4685-bed0-4bb1f1873c78")

        every { repository.findById(predicate.id) } returns Optional.of(predicate)

        shouldThrow<PredicateNotModifiable> { service.delete(predicate.id, loggedInUser) }.asClue {
            it.message shouldBe """Predicate "${predicate.id}" is not modifiable."""
        }

        verify(exactly = 1) { repository.findById(predicate.id) }
        verify(exactly = 0) { repository.deleteById(any()) }
    }
}
