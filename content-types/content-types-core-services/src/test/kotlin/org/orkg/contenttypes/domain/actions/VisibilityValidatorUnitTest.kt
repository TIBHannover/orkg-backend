package org.orkg.contenttypes.domain.actions

import io.kotest.assertions.throwables.shouldThrow
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import java.util.*
import org.junit.jupiter.api.Test
import org.orkg.common.ContributorId
import org.orkg.common.testing.fixtures.MockkBaseTest
import org.orkg.community.output.ContributorRepository
import org.orkg.community.testing.fixtures.createContributor
import org.orkg.contenttypes.domain.ContentType
import org.orkg.contenttypes.domain.testing.fixtures.createPaper
import org.orkg.graph.domain.NeitherOwnerNorCurator
import org.orkg.graph.domain.Visibility
import org.orkg.testing.MockUserId

internal class VisibilityValidatorUnitTest : MockkBaseTest {
    private val contributorRepository: ContributorRepository = mockk()

    private val visibilityValidator = VisibilityValidator<Pair<ContributorId, Visibility?>, ContentType>(
        contributorRepository = contributorRepository,
        contributorSelector = { (contributorId, _) -> contributorId },
        contentTypeExtractor = { it },
        newValueSelector = { (_, newVisibility) -> newVisibility }
    )

    @Test
    fun `Given a content type and a visibility, when updating the visibility to deleted as a curator, it returns success`() {
        val contentType = createPaper().copy(createdBy = ContributorId(MockUserId.USER))
        val curatorId = ContributorId(MockUserId.CURATOR)
        val command = curatorId to Visibility.DELETED
        val curator = createContributor(curatorId, isCurator = true)

        every { contributorRepository.findById(curatorId) } returns Optional.of(curator)

        visibilityValidator(command, contentType)

        verify(exactly = 1) { contributorRepository.findById(curatorId) }
    }

    @Test
    fun `Given a content type and a visibility, when updating the visibility to deleted as the owner, it returns success`() {
        val contentType = createPaper().copy(createdBy = ContributorId(MockUserId.USER))
        val command = contentType.createdBy to Visibility.DELETED

        visibilityValidator(command, contentType)
    }

    @Test
    fun `Given a content type and a visibility, when updating the visibility to deleted as some user, it throws an exception`() {
        val contentType = createPaper().copy(createdBy = ContributorId(MockUserId.USER))
        val contributorId = ContributorId("89b13df4-22ae-4685-bed0-4bb1f1873c78")
        val command = contributorId to Visibility.DELETED
        val someUser = createContributor(contributorId, isCurator = false)

        every { contributorRepository.findById(contributorId) } returns Optional.of(someUser)

        shouldThrow<NeitherOwnerNorCurator> { visibilityValidator(command, contentType) }

        verify(exactly = 1) { contributorRepository.findById(contributorId) }
    }

    @Test
    fun `Given a content type and a visibility, when updating the visibility to featured as a curator, it returns success`() {
        val contentType = createPaper().copy(createdBy = ContributorId(MockUserId.USER))
        val curatorId = ContributorId(MockUserId.CURATOR)
        val command = curatorId to Visibility.FEATURED
        val curator = createContributor(curatorId, isCurator = true)

        every { contributorRepository.findById(curatorId) } returns Optional.of(curator)

        visibilityValidator(command, contentType)

        verify(exactly = 1) { contributorRepository.findById(curatorId) }
    }

    @Test
    fun `Given a content type and a visibility, when updating the visibility to featured as the owner, it throws an exception`() {
        val contentType = createPaper().copy(createdBy = ContributorId(MockUserId.USER))
        val contributorId = contentType.createdBy
        val command = contributorId to Visibility.FEATURED
        val owner = createContributor(contributorId)

        every { contributorRepository.findById(contributorId) } returns Optional.of(owner)

        shouldThrow<NeitherOwnerNorCurator> { visibilityValidator(command, contentType) }

        verify(exactly = 1) { contributorRepository.findById(contributorId) }
    }

    @Test
    fun `Given a content type and a visibility, when updating the visibility to featured as some user, it throws an exception`() {
        val contentType = createPaper().copy(createdBy = ContributorId(MockUserId.USER))
        val contributorId = ContributorId("89b13df4-22ae-4685-bed0-4bb1f1873c78")
        val command = contributorId to Visibility.FEATURED
        val someUser = createContributor(contributorId)

        every { contributorRepository.findById(contributorId) } returns Optional.of(someUser)

        shouldThrow<NeitherOwnerNorCurator> { visibilityValidator(command, contentType) }

        verify(exactly = 1) { contributorRepository.findById(contributorId) }
    }

    @Test
    fun `Given a content type and a visibility, when visibility is the same, it does nothing`() {
        val contentType = createPaper()
        val contributorId = ContributorId(MockUserId.USER)
        val command = contributorId to contentType.visibility

        visibilityValidator(command, contentType)
    }

    @Test
    fun `Given a content type and a visibility, when visibility is not set, it does nothing`() {
        val contentType = createPaper()
        val contributorId = ContributorId(MockUserId.USER)
        val command = contributorId to null

        visibilityValidator(command, contentType)
    }
}
