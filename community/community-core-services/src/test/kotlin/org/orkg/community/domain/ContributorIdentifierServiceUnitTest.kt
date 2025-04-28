package org.orkg.community.domain

import io.kotest.assertions.throwables.shouldThrow
import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.just
import io.mockk.mockk
import io.mockk.runs
import io.mockk.verify
import org.junit.jupiter.api.Test
import org.orkg.common.ORCID
import org.orkg.common.testing.fixtures.MockkBaseTest
import org.orkg.common.testing.fixtures.fixedClock
import org.orkg.community.input.testing.fixtures.createContributorIdentifierCommand
import org.orkg.community.output.ContributorIdentifierRepository
import org.orkg.community.output.ContributorRepository
import org.orkg.community.testing.fixtures.createContributor
import org.orkg.community.testing.fixtures.createContributorIdentifier
import org.orkg.contenttypes.domain.InvalidIdentifier
import java.time.OffsetDateTime
import java.util.Optional

internal class ContributorIdentifierServiceUnitTest : MockkBaseTest {
    private val contribtorRepository: ContributorRepository = mockk()
    private val contributorIdentifierRepository: ContributorIdentifierRepository = mockk()

    private val service = ContributorIdentifierService(
        contribtorRepository,
        contributorIdentifierRepository,
        fixedClock
    )

    @Test
    fun `Given a contributor identifier is created, when inputs are valid, it creates a new contributor identifier`() {
        val command = createContributorIdentifierCommand()
        val contributor = createContributor(command.contributorId)
        val expected = ContributorIdentifier(
            contributorId = command.contributorId,
            type = command.type,
            value = ORCID.of(command.value),
            createdAt = OffsetDateTime.now(fixedClock),
        )

        every { contribtorRepository.findById(command.contributorId) } returns Optional.of(contributor)
        every { contributorIdentifierRepository.findByContributorIdAndValue(command.contributorId, command.value) } returns Optional.empty()
        every { contributorIdentifierRepository.save(any()) } just runs

        service.create(command) shouldBe expected

        verify(exactly = 1) { contribtorRepository.findById(command.contributorId) }
        verify(exactly = 1) { contributorIdentifierRepository.findByContributorIdAndValue(command.contributorId, command.value) }
        verify(exactly = 1) { contributorIdentifierRepository.save(expected) }
    }

    @Test
    fun `Given a contributor identifier is created, when contributor does not exist, it throws an exception`() {
        val command = createContributorIdentifierCommand()

        every { contribtorRepository.findById(command.contributorId) } returns Optional.empty()

        shouldThrow<ContributorNotFound> { service.create(command) }

        verify(exactly = 1) { contribtorRepository.findById(command.contributorId) }
    }

    @Test
    fun `Given a contributor identifier is created, when identifier already exists for contributor, it throws an exception`() {
        val command = createContributorIdentifierCommand().copy(value = "invalid orcid")
        val contributor = createContributor(command.contributorId)
        val identifier = createContributorIdentifier()

        every { contribtorRepository.findById(command.contributorId) } returns Optional.of(contributor)
        every { contributorIdentifierRepository.findByContributorIdAndValue(command.contributorId, command.value) } returns Optional.of(identifier)

        shouldThrow<ContributorIdentifierAlreadyExists> { service.create(command) }

        verify(exactly = 1) { contribtorRepository.findById(command.contributorId) }
        verify(exactly = 1) { contributorIdentifierRepository.findByContributorIdAndValue(command.contributorId, command.value) }
    }

    @Test
    fun `Given a contributor identifier is created, when value does not match specified identifier type, it throws an exception`() {
        val command = createContributorIdentifierCommand().copy(value = "invalid orcid")
        val contributor = createContributor(command.contributorId)

        every { contribtorRepository.findById(command.contributorId) } returns Optional.of(contributor)
        every { contributorIdentifierRepository.findByContributorIdAndValue(command.contributorId, command.value) } returns Optional.empty()

        shouldThrow<InvalidIdentifier> { service.create(command) }

        verify(exactly = 1) { contribtorRepository.findById(command.contributorId) }
        verify(exactly = 1) { contributorIdentifierRepository.findByContributorIdAndValue(command.contributorId, command.value) }
    }
}
