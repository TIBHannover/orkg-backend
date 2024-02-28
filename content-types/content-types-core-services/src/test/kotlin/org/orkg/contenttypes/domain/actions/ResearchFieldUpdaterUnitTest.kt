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
import org.orkg.common.ContributorId
import org.orkg.common.PageRequests
import org.orkg.common.ThingId
import org.orkg.graph.domain.Classes
import org.orkg.graph.domain.Predicates
import org.orkg.graph.domain.StatementId
import org.orkg.graph.input.StatementUseCases
import org.orkg.graph.testing.fixtures.createPredicate
import org.orkg.graph.testing.fixtures.createResource
import org.orkg.graph.testing.fixtures.createStatement
import org.orkg.testing.pageOf

class ResearchFieldUpdaterUnitTest {
    private val statementService: StatementUseCases = mockk()
    private val researchFieldCreator: ResearchFieldCreator = mockk()

    private val researchFieldUpdater = object : ResearchFieldUpdater(statementService, researchFieldCreator) {}

    @BeforeEach
    fun resetState() {
        clearAllMocks()
    }

    @AfterEach
    fun verifyMocks() {
        confirmVerified(statementService)
    }

    @Test
    fun `Given a subject, when updating with a previously unseen research field, it links the subject to the new research field`() {
        val id = ThingId("R12")
        val contributorId = ContributorId(UUID.randomUUID())
        val subjectId = ThingId("R123")

        every {
            statementService.findAll(
                subjectId = subjectId,
                predicateId = Predicates.hasResearchField,
                pageable = PageRequests.ALL
            )
        } returns pageOf()
        every { researchFieldCreator.create(contributorId, listOf(id), subjectId) } just runs

        researchFieldUpdater.update(contributorId, listOf(id), subjectId)

        verify(exactly = 1) {
            statementService.findAll(
                subjectId = subjectId,
                predicateId = Predicates.hasResearchField,
                pageable = PageRequests.ALL
            )
        }
        verify(exactly = 1) { researchFieldCreator.create(contributorId, listOf(id), subjectId) }
    }

    @Test
    fun `Given a subject with attached research fields, when updating with an empty list of research fields, it deletes the old research field statements`() {
        val contributorId = ContributorId(UUID.randomUUID())
        val subjectId = ThingId("R123")
        val statementId = StatementId("S1")

        every {
            statementService.findAll(
                subjectId = subjectId,
                predicateId = Predicates.hasResearchField,
                pageable = PageRequests.ALL
            )
        } returns pageOf(
            createStatement(
                id = statementId,
                subject = createResource(subjectId),
                predicate = createPredicate(Predicates.hasResearchField),
                `object` = createResource(classes = setOf(Classes.researchField))
            )
        )
        every { statementService.delete(statementId) } just runs

        researchFieldUpdater.update(contributorId, emptyList(), subjectId)

        verify(exactly = 1) {
            statementService.findAll(
                subjectId = subjectId,
                predicateId = Predicates.hasResearchField,
                pageable = PageRequests.ALL
            )
        }
        verify(exactly = 1) { statementService.delete(statementId) }
    }

    @Test
    fun `Given a subject with attached research fields, when updating the research field, it links the subject to the new research field and deletes the link to the old research field`() {
        val id = ThingId("R12")
        val contributorId = ContributorId(UUID.randomUUID())
        val subjectId = ThingId("R123")
        val statementId = StatementId("S1")

        every {
            statementService.findAll(
                subjectId = subjectId,
                predicateId = Predicates.hasResearchField,
                pageable = PageRequests.ALL
            )
        } returns pageOf(
            createStatement(
                id = statementId,
                subject = createResource(subjectId),
                predicate = createPredicate(Predicates.hasResearchField),
                `object` = createResource(classes = setOf(Classes.researchField))
            )
        )
        every { statementService.delete(statementId) } just runs
        every { researchFieldCreator.create(contributorId, listOf(id), subjectId) } just runs

        researchFieldUpdater.update(contributorId, listOf(id), subjectId)

        verify(exactly = 1) {
            statementService.findAll(
                subjectId = subjectId,
                predicateId = Predicates.hasResearchField,
                pageable = PageRequests.ALL
            )
        }
        verify(exactly = 1) { statementService.delete(statementId) }
        verify(exactly = 1) { researchFieldCreator.create(contributorId, listOf(id), subjectId) }
    }
}
