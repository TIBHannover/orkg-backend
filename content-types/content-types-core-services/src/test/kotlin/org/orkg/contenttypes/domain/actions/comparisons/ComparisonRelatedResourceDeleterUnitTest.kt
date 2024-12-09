package org.orkg.contenttypes.domain.actions.comparisons

import io.mockk.clearAllMocks
import io.mockk.confirmVerified
import io.mockk.every
import io.mockk.just
import io.mockk.mockk
import io.mockk.runs
import io.mockk.verify
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.orkg.common.ContributorId
import org.orkg.common.PageRequests
import org.orkg.common.ThingId
import org.orkg.contenttypes.domain.ComparisonRelatedResourceNotFound
import org.orkg.contenttypes.domain.ComparisonRelatedResourceNotModifiable
import org.orkg.graph.domain.Classes
import org.orkg.graph.domain.Predicates
import org.orkg.graph.domain.StatementId
import org.orkg.graph.input.ResourceUseCases
import org.orkg.graph.input.StatementUseCases
import org.orkg.graph.testing.fixtures.createPredicate
import org.orkg.graph.testing.fixtures.createResource
import org.orkg.graph.testing.fixtures.createStatement
import org.orkg.testing.MockUserId
import org.orkg.testing.pageOf

internal class ComparisonRelatedResourceDeleterUnitTest {
    private val statementService: StatementUseCases = mockk()
    private val resourceService: ResourceUseCases = mockk()

    private val contributionCreator = ComparisonRelatedResourceDeleter(statementService, resourceService)

    @BeforeEach
    fun resetState() {
        clearAllMocks()
    }

    @AfterEach
    fun verifyMocks() {
        confirmVerified(statementService, resourceService)
    }

    @Test
    fun `Given a comparison related resource, when deleting, it deletes the comparison related resource`() {
        val comparisonId = ThingId("comparisonId")
        val comparisonRelatedResourceId = ThingId("comparisonRelatedResourceId")
        val contributorId = ContributorId(MockUserId.USER)
        val comparison = createResource(comparisonId, classes = setOf(Classes.comparison))
        val comparisonRelatedResource = createResource(comparisonRelatedResourceId, classes = setOf(Classes.comparisonRelatedResource))
        val hasRelatedResource = createStatement(
            subject = comparison,
            predicate = createPredicate(Predicates.hasRelatedResource),
            `object` = comparisonRelatedResource
        )

        every {
            statementService.findAll(
                subjectId = comparisonId,
                predicateId = Predicates.hasRelatedResource,
                objectId = comparisonRelatedResourceId,
                objectClasses = setOf(Classes.comparisonRelatedResource),
                pageable = PageRequests.SINGLE
            )
        } returns pageOf(hasRelatedResource)
        every {
            statementService.findAll(
                subjectClasses = setOf(Classes.comparison),
                predicateId = Predicates.hasPreviousVersion,
                objectId = comparisonId,
                pageable = PageRequests.SINGLE
            )
        } returns pageOf()
        every {
            statementService.findAll(
                objectId = comparisonRelatedResourceId,
                pageable = PageRequests.ALL
            )
        } returns pageOf(hasRelatedResource)
        every {
            statementService.findAll(
                subjectId = comparisonRelatedResourceId,
                pageable = PageRequests.ALL
            )
        } returns pageOf(
            createStatement(StatementId("Statement1")),
            createStatement(StatementId("Statement2"))
        )
        every { statementService.delete(any<Set<StatementId>>()) } just runs
        every { resourceService.delete(comparisonRelatedResourceId, contributorId) } just runs

        contributionCreator.execute(comparisonId, comparisonRelatedResourceId, contributorId)

        verify(exactly = 1) {
            statementService.findAll(
                subjectId = comparisonId,
                predicateId = Predicates.hasRelatedResource,
                objectId = comparisonRelatedResourceId,
                objectClasses = setOf(Classes.comparisonRelatedResource),
                pageable = PageRequests.SINGLE
            )
        }
        verify(exactly = 1) {
            statementService.findAll(
                subjectClasses = setOf(Classes.comparison),
                predicateId = Predicates.hasPreviousVersion,
                objectId = comparisonId,
                pageable = PageRequests.SINGLE
            )
        }
        verify(exactly = 1) {
            statementService.findAll(
                objectId = comparisonRelatedResourceId,
                pageable = PageRequests.ALL
            )
        }
        verify(exactly = 1) {
            statementService.findAll(
                subjectId = comparisonRelatedResourceId,
                pageable = PageRequests.ALL
            )
        }
        verify(exactly = 1) {
            statementService.delete(
                setOf(hasRelatedResource.id, StatementId("Statement1"), StatementId("Statement2"))
            )
        }
        verify(exactly = 1) { resourceService.delete(comparisonRelatedResourceId, contributorId) }
    }

    @Test
    fun `Given a comparison related resource, when comparison related resource does not exist, it throws an exception`() {
        val comparisonId = ThingId("comparisonId")
        val comparisonRelatedResourceId = ThingId("comparisonRelatedResourceId")
        val contributorId = ContributorId(MockUserId.USER)

        every {
            statementService.findAll(
                subjectId = comparisonId,
                predicateId = Predicates.hasRelatedResource,
                objectId = comparisonRelatedResourceId,
                objectClasses = setOf(Classes.comparisonRelatedResource),
                pageable = PageRequests.SINGLE
            )
        } returns pageOf()

        assertThrows<ComparisonRelatedResourceNotFound> {
            contributionCreator.execute(comparisonId, comparisonRelatedResourceId, contributorId)
        }

        verify(exactly = 1) {
            statementService.findAll(
                subjectId = comparisonId,
                predicateId = Predicates.hasRelatedResource,
                objectId = comparisonRelatedResourceId,
                objectClasses = setOf(Classes.comparisonRelatedResource),
                pageable = PageRequests.SINGLE
            )
        }
    }

    @Test
    fun `Given a comparison related resource, when comparison related resource is part of a previous version comparison, it throws an exception`() {
        val comparisonId = ThingId("comparisonId")
        val comparisonRelatedResourceId = ThingId("comparisonRelatedResourceId")
        val contributorId = ContributorId(MockUserId.USER)
        val comparison = createResource(comparisonId, classes = setOf(Classes.comparison))
        val comparisonRelatedResource = createResource(comparisonRelatedResourceId, classes = setOf(Classes.comparisonRelatedResource))
        val hasRelatedResource = createStatement(
            subject = comparison,
            predicate = createPredicate(Predicates.hasRelatedResource),
            `object` = comparisonRelatedResource
        )

        every {
            statementService.findAll(
                subjectId = comparisonId,
                predicateId = Predicates.hasRelatedResource,
                objectId = comparisonRelatedResourceId,
                objectClasses = setOf(Classes.comparisonRelatedResource),
                pageable = PageRequests.SINGLE
            )
        } returns pageOf(hasRelatedResource)
        every {
            statementService.findAll(
                subjectClasses = setOf(Classes.comparison),
                predicateId = Predicates.hasPreviousVersion,
                objectId = comparisonId,
                pageable = PageRequests.SINGLE
            )
        } returns pageOf(
            createStatement(subject = createResource(classes = setOf(Classes.comparison)), `object` = comparison)
        )

        assertThrows<ComparisonRelatedResourceNotModifiable> {
            contributionCreator.execute(comparisonId, comparisonRelatedResourceId, contributorId)
        }

        verify(exactly = 1) {
            statementService.findAll(
                subjectId = comparisonId,
                predicateId = Predicates.hasRelatedResource,
                objectId = comparisonRelatedResourceId,
                objectClasses = setOf(Classes.comparisonRelatedResource),
                pageable = PageRequests.SINGLE
            )
        }
        verify(exactly = 1) {
            statementService.findAll(
                subjectClasses = setOf(Classes.comparison),
                predicateId = Predicates.hasPreviousVersion,
                objectId = comparisonId,
                pageable = PageRequests.SINGLE
            )
        }
    }

    @Test
    fun `Given a comparison related resource, when comparison related resource is not modifiable, it throws an exception`() {
        val comparisonId = ThingId("comparisonId")
        val comparisonRelatedResourceId = ThingId("comparisonRelatedResourceId")
        val contributorId = ContributorId(MockUserId.USER)
        val comparison = createResource(comparisonId, classes = setOf(Classes.comparison))
        val comparisonRelatedResource = createResource(
            comparisonRelatedResourceId,
            classes = setOf(Classes.comparisonRelatedResource),
            modifiable = false
        )
        val hasRelatedResource = createStatement(
            subject = comparison,
            predicate = createPredicate(Predicates.hasRelatedResource),
            `object` = comparisonRelatedResource
        )

        every {
            statementService.findAll(
                subjectId = comparisonId,
                predicateId = Predicates.hasRelatedResource,
                objectId = comparisonRelatedResourceId,
                objectClasses = setOf(Classes.comparisonRelatedResource),
                pageable = PageRequests.SINGLE
            )
        } returns pageOf(hasRelatedResource)
        every {
            statementService.findAll(
                subjectClasses = setOf(Classes.comparison),
                predicateId = Predicates.hasPreviousVersion,
                objectId = comparisonId,
                pageable = PageRequests.SINGLE
            )
        } returns pageOf()

        assertThrows<ComparisonRelatedResourceNotModifiable> {
            contributionCreator.execute(comparisonId, comparisonRelatedResourceId, contributorId)
        }

        verify(exactly = 1) {
            statementService.findAll(
                subjectId = comparisonId,
                predicateId = Predicates.hasRelatedResource,
                objectId = comparisonRelatedResourceId,
                objectClasses = setOf(Classes.comparisonRelatedResource),
                pageable = PageRequests.SINGLE
            )
        }
        verify(exactly = 1) {
            statementService.findAll(
                subjectClasses = setOf(Classes.comparison),
                predicateId = Predicates.hasPreviousVersion,
                objectId = comparisonId,
                pageable = PageRequests.SINGLE
            )
        }
    }

    @Test
    fun `Given a comparison related resource, when comparison related resource is referenced by another resource other than the comparison, it only unlinks the comparison related resource from the comparison`() {
        val comparisonId = ThingId("comparisonId")
        val comparisonRelatedResourceId = ThingId("comparisonRelatedResourceId")
        val contributorId = ContributorId(MockUserId.USER)
        val comparison = createResource(comparisonId, classes = setOf(Classes.comparison))
        val comparisonRelatedResource = createResource(comparisonRelatedResourceId, classes = setOf(Classes.comparisonRelatedResource))
        val hasRelatedResource = createStatement(
            subject = comparison,
            predicate = createPredicate(Predicates.hasRelatedResource),
            `object` = comparisonRelatedResource
        )

        every {
            statementService.findAll(
                subjectId = comparisonId,
                predicateId = Predicates.hasRelatedResource,
                objectId = comparisonRelatedResourceId,
                objectClasses = setOf(Classes.comparisonRelatedResource),
                pageable = PageRequests.SINGLE
            )
        } returns pageOf(hasRelatedResource)
        every {
            statementService.findAll(
                subjectClasses = setOf(Classes.comparison),
                predicateId = Predicates.hasPreviousVersion,
                objectId = comparisonId,
                pageable = PageRequests.SINGLE
            )
        } returns pageOf()
        every {
            statementService.findAll(
                objectId = comparisonRelatedResourceId,
                pageable = PageRequests.ALL
            )
        } returns pageOf(hasRelatedResource, createStatement(`object` = comparisonRelatedResource))
        every { statementService.delete(any<Set<StatementId>>()) } just runs

        contributionCreator.execute(comparisonId, comparisonRelatedResourceId, contributorId)

        verify(exactly = 1) {
            statementService.findAll(
                subjectId = comparisonId,
                predicateId = Predicates.hasRelatedResource,
                objectId = comparisonRelatedResourceId,
                objectClasses = setOf(Classes.comparisonRelatedResource),
                pageable = PageRequests.SINGLE
            )
        }
        verify(exactly = 1) {
            statementService.findAll(
                subjectClasses = setOf(Classes.comparison),
                predicateId = Predicates.hasPreviousVersion,
                objectId = comparisonId,
                pageable = PageRequests.SINGLE
            )
        }
        verify(exactly = 1) {
            statementService.findAll(
                objectId = comparisonRelatedResourceId,
                pageable = PageRequests.ALL
            )
        }
        verify(exactly = 1) { statementService.delete(setOf(hasRelatedResource.id)) }
    }
}
