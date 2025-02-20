package org.orkg.contenttypes.domain.actions

import io.kotest.assertions.throwables.shouldThrow
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.junit.jupiter.api.Test
import org.orkg.common.ContributorId
import org.orkg.common.testing.fixtures.MockkBaseTest
import org.orkg.community.output.ContributorRepository
import org.orkg.community.testing.fixtures.createContributor
import org.orkg.graph.domain.NotACurator
import org.orkg.testing.MockUserId
import java.util.Optional

internal class VerifiedValidatorUnitTest : MockkBaseTest {
    private val contributorRepository: ContributorRepository = mockk()

    private val verifiedValidator = VerifiedValidator<Pair<ContributorId, Boolean?>, Boolean>(
        contributorRepository = contributorRepository,
        contributorSelector = { (contributorId, _) -> contributorId },
        oldValueSelector = { it },
        newValueSelector = { (_, verified) -> verified }
    )

    @Test
    fun `Given a verified flag, when performing user is a curator, it returns success`() {
        val state = false
        val curatorId = ContributorId(MockUserId.CURATOR)
        val command = curatorId to true
        val curator = createContributor(curatorId, isCurator = true)

        every { contributorRepository.findById(curatorId) } returns Optional.of(curator)

        verifiedValidator(command, state)

        verify(exactly = 1) { contributorRepository.findById(curatorId) }
    }

    @Test
    fun `Given a verified flag, when performing user is not a curator, it throws an exception`() {
        val state = false
        val contributorId = ContributorId(MockUserId.CURATOR)
        val command = contributorId to true
        val someUser = createContributor(contributorId, isCurator = false)

        every { contributorRepository.findById(contributorId) } returns Optional.of(someUser)

        shouldThrow<NotACurator> { verifiedValidator(command, state) }

        verify(exactly = 1) { contributorRepository.findById(contributorId) }
    }

    @Test
    fun `Given a verified flag, when old verified flag is identical, it returns success`() {
        val state = true
        val contributorId = ContributorId(MockUserId.CURATOR)
        val command = contributorId to true

        verifiedValidator(command, state)
    }

    @Test
    fun `Given a verified flag, when flag is null, it does nothing`() {
        val state = true
        val contributorId = ContributorId(MockUserId.CURATOR)
        val command = contributorId to null

        verifiedValidator(command, state)
    }
}
