package org.orkg.graph.domain

import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.just
import io.mockk.mockk
import io.mockk.runs
import io.mockk.verify
import org.junit.jupiter.api.Test
import org.orkg.common.ContributorId
import org.orkg.common.ThingId
import org.orkg.common.testing.fixtures.MockkBaseTest
import org.orkg.common.testing.fixtures.fixedClock
import org.orkg.graph.input.CreatePredicateUseCase
import org.orkg.graph.input.UpdatePredicateUseCase
import org.orkg.graph.output.PredicateRepository
import org.orkg.graph.testing.fixtures.createPredicate
import org.orkg.testing.MockUserId
import java.time.OffsetDateTime
import java.util.Optional

internal class UnsafePredicateServiceUnitTest : MockkBaseTest {
    private val repository: PredicateRepository = mockk()

    private val service = UnsafePredicateService(repository, fixedClock)

    @Test
    fun `Given a predicate create command, it creates a new predicate`() {
        val id = ThingId("P123")
        val command = CreatePredicateUseCase.CreateCommand(
            id = id,
            contributorId = ContributorId(MockUserId.USER),
            label = "label",
            modifiable = false
        )

        every { repository.save(any()) } just runs

        service.create(command) shouldBe id

        verify(exactly = 1) {
            repository.save(
                withArg {
                    it.id shouldBe command.id
                    it.label shouldBe command.label
                    it.createdAt shouldBe OffsetDateTime.now(fixedClock)
                    it.createdBy shouldBe command.contributorId
                    it.modifiable shouldBe command.modifiable
                }
            )
        }
    }

    @Test
    fun `Given a predicate create command, when using minimal inputs, it assigns a new id and creates a new predicate`() {
        val id = ThingId("P123")
        val contributorId = ContributorId(MockUserId.USER)
        val command = CreatePredicateUseCase.CreateCommand(
            contributorId = contributorId,
            label = "label"
        )

        every { repository.nextIdentity() } returns id
        every { repository.save(any()) } just runs

        service.create(command) shouldBe id

        verify(exactly = 1) { repository.nextIdentity() }
        verify(exactly = 1) {
            repository.save(
                withArg {
                    it.id shouldBe id
                    it.label shouldBe command.label
                    it.createdAt shouldBe OffsetDateTime.now(fixedClock)
                    it.createdBy shouldBe contributorId
                    it.modifiable shouldBe command.modifiable
                }
            )
        }
    }

    @Test
    fun `Given a predicate update command, when updating all properties, it returns success`() {
        val predicate = createPredicate()
        val contributorId = ContributorId(MockUserId.USER)
        val label = "updated label"
        val modifiable = false

        every { repository.findById(predicate.id) } returns Optional.of(predicate)
        every { repository.save(any()) } just runs

        service.update(
            UpdatePredicateUseCase.UpdateCommand(
                id = predicate.id,
                contributorId = contributorId,
                label = label,
                modifiable = modifiable,
            )
        )

        verify(exactly = 1) { repository.findById(predicate.id) }
        verify(exactly = 1) {
            repository.save(
                withArg {
                    it.label shouldBe label
                    it.modifiable shouldBe modifiable
                }
            )
        }
    }

    @Test
    fun `Given a predicate update command, when updating no properties, it does nothing`() {
        val id = ThingId("P123")
        val contributorId = ContributorId(MockUserId.USER)

        service.update(UpdatePredicateUseCase.UpdateCommand(id, contributorId))
    }

    @Test
    fun `Given a predicate update command, when updating with an invalid label, it updates the predicate`() {
        val predicate = createPredicate()
        val label = "a".repeat(MAX_LABEL_LENGTH + 1)
        val command = UpdatePredicateUseCase.UpdateCommand(
            id = predicate.id,
            contributorId = ContributorId(MockUserId.USER),
            label = label
        )

        every { repository.findById(predicate.id) } returns Optional.of(predicate)
        every { repository.save(any()) } just runs

        service.update(command)

        verify(exactly = 1) { repository.findById(predicate.id) }
        verify(exactly = 1) { repository.save(withArg { it.label shouldBe label }) }
    }

    @Test
    fun `Given a predicate update command, when predicate is unmodifiable predicate, it updates the predicate`() {
        val predicate = createPredicate(modifiable = false)
        val command = UpdatePredicateUseCase.UpdateCommand(
            id = predicate.id,
            contributorId = ContributorId(MockUserId.USER),
            label = "new label"
        )

        every { repository.findById(predicate.id) } returns Optional.of(predicate)
        every { repository.save(any()) } just runs

        service.update(command)

        verify(exactly = 1) { repository.findById(predicate.id) }
        verify(exactly = 1) { repository.save(withArg { it.label shouldBe "new label" }) }
    }

    @Test
    fun `Given a predicate, when deleting, it deletes the predicate from the repository`() {
        val id = ThingId("R2145")
        val couldBeAnyone = ContributorId("1255bbe4-1850-4033-ba10-c80d4b370e3e")

        every { repository.deleteById(id) } just runs

        service.delete(id, couldBeAnyone)

        verify(exactly = 1) { repository.deleteById(id) }
    }
}
