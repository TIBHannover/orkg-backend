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
import org.orkg.graph.domain.NeitherOwnerNorCurator
import org.orkg.graph.domain.Resource
import org.orkg.graph.testing.fixtures.createResource
import java.util.Optional

internal class ResourceOwnerValidatorUnitTest : MockkBaseTest {
    private val contributorRepository: ContributorRepository = mockk()

    private val resourceValidator = ResourceOwnerValidator<ContributorId, Resource?>(
        contributorRepository = contributorRepository,
        resourceSelector = { it },
        contributorIdSelector = { it },
    )

    @Test
    fun `Given a resource, when it is null, it returns success`() {
        val theOwningContributorId = ContributorId("1255bbe4-1850-4033-ba10-c80d4b370e3e")

        resourceValidator(theOwningContributorId, null)
    }

    @Test
    fun `Given a resource, when it is owned by the user, it returns success`() {
        val theOwningContributorId = ContributorId("1255bbe4-1850-4033-ba10-c80d4b370e3e")
        val mockResource = createResource(createdBy = theOwningContributorId)

        resourceValidator(theOwningContributorId, mockResource)
    }

    @Test
    fun `Given a resource, when it is not owned by the user, but the user is a curator, it returns success`() {
        val theOwningContributorId = ContributorId("1255bbe4-1850-4033-ba10-c80d4b370e3e")
        val aCurator = createContributor(id = ContributorId("645fabd1-9952-41f8-9239-627ee67c1940"), isCurator = true)
        val mockResource = createResource(createdBy = theOwningContributorId)

        every { contributorRepository.findById(aCurator.id) } returns Optional.of(aCurator)

        resourceValidator(aCurator.id, mockResource)

        verify(exactly = 1) { contributorRepository.findById(aCurator.id) }
    }

    @Test
    fun `Given a resource, when it is not owned by the user, and the user is not a curator, it throws an exception`() {
        val theOwningContributorId = ContributorId("1255bbe4-1850-4033-ba10-c80d4b370e3e")
        val loggedInUserId = ContributorId("89b13df4-22ae-4685-bed0-4bb1f1873c78")
        val loggedInUser = createContributor(id = loggedInUserId)
        val mockResource = createResource(createdBy = theOwningContributorId)

        every { contributorRepository.findById(loggedInUserId) } returns Optional.of(loggedInUser)

        shouldThrow<NeitherOwnerNorCurator> { resourceValidator(loggedInUserId, mockResource) }

        verify(exactly = 1) { contributorRepository.findById(loggedInUserId) }
    }
}
