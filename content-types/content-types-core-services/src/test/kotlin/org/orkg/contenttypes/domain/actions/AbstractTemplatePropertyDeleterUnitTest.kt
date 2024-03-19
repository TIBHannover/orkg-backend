package org.orkg.contenttypes.domain.actions

import io.mockk.clearAllMocks
import io.mockk.confirmVerified
import io.mockk.every
import io.mockk.just
import io.mockk.mockk
import io.mockk.runs
import io.mockk.verify
import java.util.*
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertDoesNotThrow
import org.orkg.common.ContributorId
import org.orkg.common.PageRequests
import org.orkg.common.ThingId
import org.orkg.graph.domain.NeitherOwnerNorCurator
import org.orkg.graph.domain.Predicates
import org.orkg.graph.domain.StatementId
import org.orkg.graph.input.ResourceUseCases
import org.orkg.graph.input.StatementUseCases
import org.orkg.graph.testing.fixtures.createPredicate
import org.orkg.graph.testing.fixtures.createResource
import org.orkg.graph.testing.fixtures.createStatement
import org.orkg.testing.pageOf

class AbstractTemplatePropertyDeleterUnitTest {
    private val resourceService: ResourceUseCases = mockk()
    private val statementService: StatementUseCases = mockk()

    private val abstractTemplatePropertyDeleter = AbstractTemplatePropertyDeleter(resourceService, statementService)

    @BeforeEach
    fun resetState() {
        clearAllMocks()
    }

    @AfterEach
    fun verifyMocks() {
        confirmVerified(statementService, resourceService)
    }

    @Test
    fun `Given a template property, when referenced by no resource other than the template, it deletes the template property`() {
        val contributorId = ContributorId(UUID.randomUUID())
        val templateId = ThingId("R123")
        val propertyId = ThingId("R456")
        val templateHasPropertyStatement = createStatement(
            subject = createResource(templateId),
            predicate = createPredicate(Predicates.shProperty),
            `object` = createResource(propertyId)
        )

        every {
            statementService.findAll(
                objectId = propertyId,
                pageable = PageRequests.ALL
            )
        } returns pageOf(templateHasPropertyStatement)
        every {
            statementService.findAll(
                subjectId = propertyId,
                pageable = PageRequests.ALL
            )
        } returns pageOf(
            createStatement(StatementId("S123")),
            createStatement(StatementId("S456"))
        )
        every {
            statementService.delete(setOf(templateHasPropertyStatement.id, StatementId("S123"), StatementId("S456")))
        } just runs
        every { resourceService.delete(propertyId, contributorId) } just runs

        abstractTemplatePropertyDeleter.delete(contributorId, templateId, propertyId)

        verify(exactly = 1) {
            statementService.findAll(
                objectId = propertyId,
                pageable = PageRequests.ALL
            )
        }
        verify(exactly = 1) {
            statementService.findAll(
                subjectId = propertyId,
                pageable = PageRequests.ALL
            )
        }
        verify(exactly = 1) {
            statementService.delete(setOf(templateHasPropertyStatement.id, StatementId("S123"), StatementId("S456")))
        }
        verify(exactly = 1) { resourceService.delete(propertyId, contributorId) }
    }

    @Test
    fun `Given a template property, when referenced by another resource, it just removes the template property from the template`() {
        val contributorId = ContributorId(UUID.randomUUID())
        val templateId = ThingId("R123")
        val propertyId = ThingId("R456")
        val templateHasPropertyStatement = createStatement(
            subject = createResource(templateId),
            predicate = createPredicate(Predicates.shProperty),
            `object` = createResource(propertyId)
        )
        val otherStatementAboutTemplateProperty = createStatement(
            subject = createResource(),
            predicate = createPredicate(Predicates.hasLink),
            `object` = createResource(propertyId)
        )

        every {
            statementService.findAll(
                objectId = propertyId,
                pageable = PageRequests.ALL
            )
        } returns pageOf(templateHasPropertyStatement, otherStatementAboutTemplateProperty)
        every { statementService.delete(setOf(templateHasPropertyStatement.id)) } just runs

        abstractTemplatePropertyDeleter.delete(contributorId, templateId, propertyId)

        verify(exactly = 1) {
            statementService.findAll(
                objectId = propertyId,
                pageable = PageRequests.ALL
            )
        }
        verify(exactly = 1) { statementService.delete(setOf(templateHasPropertyStatement.id)) }
    }

    @Test
    fun `Given a template property, when template property is owned by another user, it does not throw an exception`() {
        val contributorId = ContributorId(UUID.randomUUID())
        val templateId = ThingId("R123")
        val propertyId = ThingId("R456")
        val templateHasPropertyStatement = createStatement(
            subject = createResource(templateId),
            predicate = createPredicate(Predicates.shProperty),
            `object` = createResource(propertyId)
        )

        every {
            statementService.findAll(
                objectId = propertyId,
                pageable = PageRequests.ALL
            )
        } returns pageOf(templateHasPropertyStatement)
        every {
            statementService.findAll(
                subjectId = propertyId,
                pageable = PageRequests.ALL
            )
        } returns pageOf(
            createStatement(StatementId("S123")),
            createStatement(StatementId("S456"))
        )
        every {
            statementService.delete(setOf(templateHasPropertyStatement.id, StatementId("S123"), StatementId("S456")))
        } just runs
        every { resourceService.delete(propertyId, contributorId) } throws NeitherOwnerNorCurator(contributorId)

        assertDoesNotThrow { abstractTemplatePropertyDeleter.delete(contributorId, templateId, propertyId) }

        verify(exactly = 1) {
            statementService.findAll(
                objectId = propertyId,
                pageable = PageRequests.ALL
            )
        }
        verify(exactly = 1) {
            statementService.findAll(
                subjectId = propertyId,
                pageable = PageRequests.ALL
            )
        }
        verify(exactly = 1) {
            statementService.delete(setOf(templateHasPropertyStatement.id, StatementId("S123"), StatementId("S456")))
        }
        verify(exactly = 1) { resourceService.delete(propertyId, contributorId) }
    }
}
