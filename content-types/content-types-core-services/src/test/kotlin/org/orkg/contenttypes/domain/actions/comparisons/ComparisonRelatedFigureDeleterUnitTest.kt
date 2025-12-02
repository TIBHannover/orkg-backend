package org.orkg.contenttypes.domain.actions.comparisons

import io.mockk.every
import io.mockk.just
import io.mockk.mockk
import io.mockk.runs
import io.mockk.verify
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.orkg.common.ContributorId
import org.orkg.common.PageRequests
import org.orkg.common.ThingId
import org.orkg.common.testing.fixtures.MockkBaseTest
import org.orkg.contenttypes.domain.ComparisonRelatedFigureNotFound
import org.orkg.contenttypes.domain.ComparisonRelatedFigureNotModifiable
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

internal class ComparisonRelatedFigureDeleterUnitTest : MockkBaseTest {
    private val statementService: StatementUseCases = mockk()
    private val resourceService: ResourceUseCases = mockk()

    private val comparisonRelatedFigureDeleter = ComparisonRelatedFigureDeleter(statementService, resourceService)

    @Test
    fun `Given a comparison related figure, when deleting, it deletes the comparison related figure`() {
        val comparisonId = ThingId("comparisonId")
        val comparisonRelatedFigureId = ThingId("comparisonRelatedFigureId")
        val contributorId = ContributorId(MockUserId.USER)
        val comparison = createResource(comparisonId, classes = setOf(Classes.comparison))
        val comparisonRelatedFigure = createResource(comparisonRelatedFigureId, classes = setOf(Classes.comparisonRelatedFigure))
        val hasRelatedFigure = createStatement(
            subject = comparison,
            predicate = createPredicate(Predicates.hasRelatedFigure),
            `object` = comparisonRelatedFigure
        )

        every {
            statementService.findAll(
                subjectId = comparisonId,
                predicateId = Predicates.hasRelatedFigure,
                objectId = comparisonRelatedFigureId,
                objectClasses = setOf(Classes.comparisonRelatedFigure),
                pageable = PageRequests.SINGLE
            )
        } returns pageOf(hasRelatedFigure)
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
                objectId = comparisonRelatedFigureId,
                pageable = PageRequests.ALL
            )
        } returns pageOf(hasRelatedFigure)
        every {
            statementService.findAll(
                subjectId = comparisonRelatedFigureId,
                pageable = PageRequests.ALL
            )
        } returns pageOf(
            createStatement(StatementId("Statement1")),
            createStatement(StatementId("Statement2"))
        )
        every { statementService.deleteAllById(any<Set<StatementId>>()) } just runs
        every { resourceService.delete(comparisonRelatedFigureId, contributorId) } just runs

        comparisonRelatedFigureDeleter.execute(comparisonId, comparisonRelatedFigureId, contributorId)

        verify(exactly = 1) {
            statementService.findAll(
                subjectId = comparisonId,
                predicateId = Predicates.hasRelatedFigure,
                objectId = comparisonRelatedFigureId,
                objectClasses = setOf(Classes.comparisonRelatedFigure),
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
                objectId = comparisonRelatedFigureId,
                pageable = PageRequests.ALL
            )
        }
        verify(exactly = 1) {
            statementService.findAll(
                subjectId = comparisonRelatedFigureId,
                pageable = PageRequests.ALL
            )
        }
        verify(exactly = 1) {
            statementService.deleteAllById(
                setOf(hasRelatedFigure.id, StatementId("Statement1"), StatementId("Statement2"))
            )
        }
        verify(exactly = 1) { resourceService.delete(comparisonRelatedFigureId, contributorId) }
    }

    @Test
    fun `Given a comparison related figure, when comparison related figure does not exist, it throws an exception`() {
        val comparisonId = ThingId("comparisonId")
        val comparisonRelatedFigureId = ThingId("comparisonRelatedFigureId")
        val contributorId = ContributorId(MockUserId.USER)

        every {
            statementService.findAll(
                subjectId = comparisonId,
                predicateId = Predicates.hasRelatedFigure,
                objectId = comparisonRelatedFigureId,
                objectClasses = setOf(Classes.comparisonRelatedFigure),
                pageable = PageRequests.SINGLE
            )
        } returns pageOf()

        assertThrows<ComparisonRelatedFigureNotFound> {
            comparisonRelatedFigureDeleter.execute(comparisonId, comparisonRelatedFigureId, contributorId)
        }

        verify(exactly = 1) {
            statementService.findAll(
                subjectId = comparisonId,
                predicateId = Predicates.hasRelatedFigure,
                objectId = comparisonRelatedFigureId,
                objectClasses = setOf(Classes.comparisonRelatedFigure),
                pageable = PageRequests.SINGLE
            )
        }
    }

    @Test
    fun `Given a comparison related figure, when comparison related figure is part of a previous version comparison, it throws an exception`() {
        val comparisonId = ThingId("comparisonId")
        val comparisonRelatedFigureId = ThingId("comparisonRelatedFigureId")
        val contributorId = ContributorId(MockUserId.USER)
        val comparison = createResource(comparisonId, classes = setOf(Classes.comparison))
        val comparisonRelatedFigure = createResource(comparisonRelatedFigureId, classes = setOf(Classes.comparisonRelatedFigure))
        val hasRelatedFigure = createStatement(
            subject = comparison,
            predicate = createPredicate(Predicates.hasRelatedFigure),
            `object` = comparisonRelatedFigure
        )

        every {
            statementService.findAll(
                subjectId = comparisonId,
                predicateId = Predicates.hasRelatedFigure,
                objectId = comparisonRelatedFigureId,
                objectClasses = setOf(Classes.comparisonRelatedFigure),
                pageable = PageRequests.SINGLE
            )
        } returns pageOf(hasRelatedFigure)
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

        assertThrows<ComparisonRelatedFigureNotModifiable> {
            comparisonRelatedFigureDeleter.execute(comparisonId, comparisonRelatedFigureId, contributorId)
        }

        verify(exactly = 1) {
            statementService.findAll(
                subjectId = comparisonId,
                predicateId = Predicates.hasRelatedFigure,
                objectId = comparisonRelatedFigureId,
                objectClasses = setOf(Classes.comparisonRelatedFigure),
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
    fun `Given a comparison related figure, when comparison related figure is not modifiable, it throws an exception`() {
        val comparisonId = ThingId("comparisonId")
        val comparisonRelatedFigureId = ThingId("comparisonRelatedFigureId")
        val contributorId = ContributorId(MockUserId.USER)
        val comparison = createResource(comparisonId, classes = setOf(Classes.comparison))
        val comparisonRelatedFigure = createResource(
            comparisonRelatedFigureId,
            classes = setOf(Classes.comparisonRelatedFigure),
            modifiable = false
        )
        val hasRelatedFigure = createStatement(
            subject = comparison,
            predicate = createPredicate(Predicates.hasRelatedFigure),
            `object` = comparisonRelatedFigure
        )

        every {
            statementService.findAll(
                subjectId = comparisonId,
                predicateId = Predicates.hasRelatedFigure,
                objectId = comparisonRelatedFigureId,
                objectClasses = setOf(Classes.comparisonRelatedFigure),
                pageable = PageRequests.SINGLE
            )
        } returns pageOf(hasRelatedFigure)
        every {
            statementService.findAll(
                subjectClasses = setOf(Classes.comparison),
                predicateId = Predicates.hasPreviousVersion,
                objectId = comparisonId,
                pageable = PageRequests.SINGLE
            )
        } returns pageOf()

        assertThrows<ComparisonRelatedFigureNotModifiable> {
            comparisonRelatedFigureDeleter.execute(comparisonId, comparisonRelatedFigureId, contributorId)
        }

        verify(exactly = 1) {
            statementService.findAll(
                subjectId = comparisonId,
                predicateId = Predicates.hasRelatedFigure,
                objectId = comparisonRelatedFigureId,
                objectClasses = setOf(Classes.comparisonRelatedFigure),
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
    fun `Given a comparison related figure, when comparison related figure is referenced by another resource other than the comparison, it only unlinks the comparison related figure from the comparison`() {
        val comparisonId = ThingId("comparisonId")
        val comparisonRelatedFigureId = ThingId("comparisonRelatedFigureId")
        val contributorId = ContributorId(MockUserId.USER)
        val comparison = createResource(comparisonId, classes = setOf(Classes.comparison))
        val comparisonRelatedFigure = createResource(comparisonRelatedFigureId, classes = setOf(Classes.comparisonRelatedFigure))
        val hasRelatedFigure = createStatement(
            subject = comparison,
            predicate = createPredicate(Predicates.hasRelatedFigure),
            `object` = comparisonRelatedFigure
        )

        every {
            statementService.findAll(
                subjectId = comparisonId,
                predicateId = Predicates.hasRelatedFigure,
                objectId = comparisonRelatedFigureId,
                objectClasses = setOf(Classes.comparisonRelatedFigure),
                pageable = PageRequests.SINGLE
            )
        } returns pageOf(hasRelatedFigure)
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
                objectId = comparisonRelatedFigureId,
                pageable = PageRequests.ALL
            )
        } returns pageOf(hasRelatedFigure, createStatement(`object` = comparisonRelatedFigure))
        every { statementService.deleteAllById(any<Set<StatementId>>()) } just runs

        comparisonRelatedFigureDeleter.execute(comparisonId, comparisonRelatedFigureId, contributorId)

        verify(exactly = 1) {
            statementService.findAll(
                subjectId = comparisonId,
                predicateId = Predicates.hasRelatedFigure,
                objectId = comparisonRelatedFigureId,
                objectClasses = setOf(Classes.comparisonRelatedFigure),
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
                objectId = comparisonRelatedFigureId,
                pageable = PageRequests.ALL
            )
        }
        verify(exactly = 1) { statementService.deleteAllById(setOf(hasRelatedFigure.id)) }
    }
}
