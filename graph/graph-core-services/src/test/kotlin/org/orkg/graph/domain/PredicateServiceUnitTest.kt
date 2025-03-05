package org.orkg.graph.domain

import io.kotest.assertions.asClue
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.just
import io.mockk.mockk
import io.mockk.runs
import io.mockk.verify
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.orkg.common.ContributorId
import org.orkg.common.ThingId
import org.orkg.common.testing.fixtures.MockkBaseTest
import org.orkg.community.output.ContributorRepository
import org.orkg.community.testing.fixtures.createContributor
import org.orkg.graph.input.CreatePredicateUseCase
import org.orkg.graph.input.UnsafePredicateUseCases
import org.orkg.graph.input.UpdatePredicateUseCase
import org.orkg.graph.output.PredicateRepository
import org.orkg.graph.testing.fixtures.createPredicate
import org.orkg.testing.MockUserId
import java.util.Optional

internal class PredicateServiceUnitTest : MockkBaseTest {
    private val repository: PredicateRepository = mockk()
    private val contributorRepository: ContributorRepository = mockk()
    private val unsafePredicateUseCases: UnsafePredicateUseCases = mockk()

    private val service = PredicateService(repository, contributorRepository, unsafePredicateUseCases)

    @Test
    fun `Given a predicate create command, when inputs are valid, it creates a new predicate`() {
        val id = ThingId("R123")
        val command = CreatePredicateUseCase.CreateCommand(
            id = id,
            contributorId = ContributorId(MockUserId.USER),
            label = "label",
            modifiable = false
        )

        every { repository.findById(id) } returns Optional.empty()
        every { unsafePredicateUseCases.create(command) } returns id

        service.create(command) shouldBe id

        verify(exactly = 1) { repository.findById(id) }
        verify(exactly = 1) { unsafePredicateUseCases.create(command) }
    }

    @Test
    fun `Given a predicate create command, when inputs are minimal, it creates a new predicate`() {
        val id = ThingId("R123")
        val command = CreatePredicateUseCase.CreateCommand(
            contributorId = ContributorId(MockUserId.USER),
            label = "label"
        )

        every { unsafePredicateUseCases.create(command) } returns id

        service.create(command) shouldBe id

        verify(exactly = 1) { unsafePredicateUseCases.create(command) }
    }

    @Test
    fun `Given a predicate create command, when id already exists, it throws an exception`() {
        val id = ThingId("R123")
        val command = CreatePredicateUseCase.CreateCommand(
            id = id,
            contributorId = ContributorId(MockUserId.USER),
            label = "some label"
        )

        every { repository.findById(id) } returns Optional.of(createPredicate(id))

        assertThrows<PredicateAlreadyExists> { service.create(command) }

        verify(exactly = 1) { repository.findById(id) }
    }

    @Test
    fun `Given a predicate create command, when label is invalid, it throws an exception`() {
        val id = ThingId("R123")
        val command = CreatePredicateUseCase.CreateCommand(
            id = id,
            contributorId = ContributorId(MockUserId.USER),
            label = "\n"
        )

        assertThrows<InvalidLabel> { service.create(command) }
    }

    @Test
    fun `Given a predicate update command, when updating no properties, it does nothing`() {
        val id = ThingId("P123")
        val contributorId = ContributorId(MockUserId.USER)

        service.update(UpdatePredicateUseCase.UpdateCommand(id, contributorId))
    }

    @Test
    fun `Given a predicate update command, when it contains an invalid label, it throws an exception`() {
        val predicate = createPredicate()
        val command = UpdatePredicateUseCase.UpdateCommand(
            id = predicate.id,
            contributorId = ContributorId(MockUserId.USER),
            label = "\n"
        )

        every { repository.findById(predicate.id) } returns Optional.of(predicate)

        shouldThrow<InvalidLabel> { service.update(command) }

        verify(exactly = 1) { repository.findById(predicate.id) }
    }

    @Test
    fun `Given a predicate update command, when predicate does not exist, it throws an exception`() {
        val command = UpdatePredicateUseCase.UpdateCommand(
            id = ThingId("P123"),
            contributorId = ContributorId(MockUserId.USER),
            label = "new label"
        )

        every { repository.findById(any()) } returns Optional.empty()

        shouldThrow<PredicateNotFound> { service.update(command) }

        verify(exactly = 1) { repository.findById(any()) }
    }

    @Test
    fun `Given a predicate update command, when updating an unmodifiable predicate, it throws an exception`() {
        val predicate = createPredicate(modifiable = false)
        val contributorId = ContributorId(MockUserId.USER)
        val command = UpdatePredicateUseCase.UpdateCommand(predicate.id, contributorId, "updated label")

        every { repository.findById(predicate.id) } returns Optional.of(predicate)

        shouldThrow<PredicateNotModifiable> { service.update(command) }.asClue {
            it.message shouldBe """Predicate "${predicate.id}" is not modifiable."""
        }

        verify(exactly = 1) { repository.findById(predicate.id) }
    }

    @Test
    fun `Given a predicate update command, when updating with the same values, it does nothing`() {
        val predicate = createPredicate()
        val contributorId = ContributorId(MockUserId.USER)

        every { repository.findById(predicate.id) } returns Optional.of(predicate)

        service.update(
            UpdatePredicateUseCase.UpdateCommand(
                id = predicate.id,
                contributorId = contributorId,
                label = predicate.label,
                modifiable = predicate.modifiable
            )
        )

        verify(exactly = 1) { repository.findById(predicate.id) }
    }

    @Test
    fun `Given a predicate update command, when all properties, it returns success`() {
        val predicate = createPredicate()
        val contributorId = ContributorId(MockUserId.USER)
        val label = "updated label"
        val modifiable = true
        val command = UpdatePredicateUseCase.UpdateCommand(
            id = predicate.id,
            contributorId = contributorId,
            label = label,
            modifiable = modifiable
        )

        every { repository.findById(predicate.id) } returns Optional.of(predicate)
        every { repository.save(any()) } just runs

        service.update(command)

        verify(exactly = 1) { repository.findById(predicate.id) }
        verify(exactly = 1) {
            repository.save(
                withArg {
                    it.id shouldBe predicate.id
                    it.label shouldBe label
                    it.createdAt shouldBe predicate.createdAt
                    it.createdBy shouldBe predicate.createdBy
                    it.modifiable shouldBe modifiable
                }
            )
        }
    }

    @Test
    fun `Given a predicate is being deleted, when it is still used in a statement, an appropriate error is thrown`() {
        val mockPredicate = createPredicate()
        val couldBeAnyone = ContributorId(MockUserId.USER)

        every { repository.findById(mockPredicate.id) } returns Optional.of(mockPredicate)
        every { repository.isInUse(mockPredicate.id) } returns true

        shouldThrow<PredicateInUse> {
            service.delete(mockPredicate.id, couldBeAnyone)
        }

        verify(exactly = 1) { repository.findById(mockPredicate.id) }
        verify(exactly = 1) { repository.isInUse(mockPredicate.id) }
    }

    @Test
    fun `Given a predicate is being deleted, when it is not used in a statement, and it is owned by the user, it gets deleted`() {
        val theOwningContributorId = ContributorId(MockUserId.USER)
        val predicate = createPredicate(createdBy = theOwningContributorId)

        every { repository.findById(predicate.id) } returns Optional.of(predicate)
        every { repository.isInUse(predicate.id) } returns false
        every { unsafePredicateUseCases.delete(predicate.id, theOwningContributorId) } returns Unit

        service.delete(predicate.id, theOwningContributorId)

        verify(exactly = 1) { repository.findById(predicate.id) }
        verify(exactly = 1) { repository.isInUse(predicate.id) }
        verify(exactly = 1) { unsafePredicateUseCases.delete(predicate.id, theOwningContributorId) }
    }

    @Test
    fun `Given a predicate is being deleted, when it is not used in a statement, and it is not owned by the user, but the user is a curator, it gets deleted`() {
        val theOwningContributorId = ContributorId(MockUserId.USER)
        val aCurator = createContributor(id = ContributorId(MockUserId.CURATOR), isCurator = true)
        val predicate = createPredicate(createdBy = theOwningContributorId)

        every { repository.findById(predicate.id) } returns Optional.of(predicate)
        every { repository.isInUse(predicate.id) } returns false
        every { contributorRepository.findById(aCurator.id) } returns Optional.of(aCurator)
        every { unsafePredicateUseCases.delete(predicate.id, aCurator.id) } returns Unit

        service.delete(predicate.id, aCurator.id)

        verify(exactly = 1) { repository.findById(predicate.id) }
        verify(exactly = 1) { repository.isInUse(predicate.id) }
        verify(exactly = 1) { unsafePredicateUseCases.delete(predicate.id, aCurator.id) }
        verify(exactly = 1) { contributorRepository.findById(aCurator.id) }
    }

    @Test
    fun `Given a predicate is being deleted, when it is not used in a statement, and it is not owned by the user, and the user is not a curator, it throws an exception`() {
        val theOwningContributorId = ContributorId("1255bbe4-1850-4033-ba10-c80d4b370e3e")
        val loggedInUserId = ContributorId(MockUserId.USER)
        val loggedInUser = createContributor(id = loggedInUserId)
        val predicate = createPredicate(createdBy = theOwningContributorId)

        every { repository.findById(predicate.id) } returns Optional.of(predicate)
        every { repository.isInUse(predicate.id) } returns false
        every { contributorRepository.findById(loggedInUserId) } returns Optional.of(loggedInUser)

        shouldThrow<NeitherOwnerNorCurator> {
            service.delete(predicate.id, loggedInUserId)
        }

        verify(exactly = 1) { repository.findById(predicate.id) }
        verify(exactly = 1) { repository.isInUse(predicate.id) }
        verify(exactly = 1) { contributorRepository.findById(loggedInUserId) }
    }

    @Test
    fun `Given a predicate is being deleted, when it is unmodifiable, it throws an exception`() {
        val predicate = createPredicate(modifiable = false)
        val loggedInUser = ContributorId("89b13df4-22ae-4685-bed0-4bb1f1873c78")

        every { repository.findById(predicate.id) } returns Optional.of(predicate)

        shouldThrow<PredicateNotModifiable> { service.delete(predicate.id, loggedInUser) }.asClue {
            it.message shouldBe """Predicate "${predicate.id}" is not modifiable."""
        }

        verify(exactly = 1) { repository.findById(predicate.id) }
    }
}
