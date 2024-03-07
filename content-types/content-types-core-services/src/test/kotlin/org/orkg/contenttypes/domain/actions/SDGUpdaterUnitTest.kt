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
import org.orkg.graph.domain.GeneralStatement
import org.orkg.graph.domain.Predicates
import org.orkg.graph.domain.StatementId
import org.orkg.graph.input.StatementUseCases
import org.orkg.graph.testing.fixtures.createPredicate
import org.orkg.graph.testing.fixtures.createResource
import org.orkg.graph.testing.fixtures.createStatement
import org.orkg.testing.pageOf

class SDGUpdaterUnitTest {
    private val statementService: StatementUseCases = mockk()

    private val sdgUpdater = SDGUpdater(statementService)

    @BeforeEach
    fun resetState() {
        clearAllMocks()
    }

    @AfterEach
    fun verifyMocks() {
        confirmVerified(statementService)
    }

    @Test
    fun `Given set of sdgs, it replaces the all sdg statements`() {
        val subjectId = ThingId("R123")
        val newSDGs = listOf(ThingId("SDG_3"), ThingId("SDG_4"))
        val oldSDGStatements = setOf(ThingId("SDG_1"), ThingId("SDG_2")).toSDGStatements(subjectId)
        val contributorId = ContributorId(UUID.randomUUID())

        every {
            statementService.findAll(
                subjectId = subjectId,
                predicateId = Predicates.sustainableDevelopmentGoal,
                pageable = PageRequests.ALL
            )
        } returns pageOf(oldSDGStatements)
        every { statementService.delete(any<Set<StatementId>>()) } just runs
        every { statementService.add(any(), any(), any(), any()) } just runs

        sdgUpdater.update(contributorId, newSDGs.toSet(), subjectId)

        verify(exactly = 1) {
            statementService.findAll(
                subjectId = subjectId,
                predicateId = Predicates.sustainableDevelopmentGoal,
                pageable = PageRequests.ALL
            )
        }
        verify(exactly = 1) { statementService.delete(oldSDGStatements.map { it.id!! }.toSet()) }
        verify(exactly = 1) {
            statementService.add(
                userId = contributorId,
                subject = subjectId,
                predicate = Predicates.sustainableDevelopmentGoal,
                `object` = newSDGs[0]
            )
        }
        verify(exactly = 1) {
            statementService.add(
                userId = contributorId,
                subject = subjectId,
                predicate = Predicates.sustainableDevelopmentGoal,
                `object` = newSDGs[1]
            )
        }
    }

    @Test
    fun `Given set of sdgs, when some sdgs are identical to old sdgs, it only updates changed sdgs`() {
        val subjectId = ThingId("R123")
        val newSDGs = listOf(ThingId("SDG_2"), ThingId("SDG_3"))
        val oldSDGStatements = setOf(ThingId("SDG_1"), ThingId("SDG_2")).toSDGStatements(subjectId)
        val contributorId = ContributorId(UUID.randomUUID())

        every {
            statementService.findAll(
                subjectId = subjectId,
                predicateId = Predicates.sustainableDevelopmentGoal,
                pageable = PageRequests.ALL
            )
        } returns pageOf(oldSDGStatements)
        every { statementService.delete(any<Set<StatementId>>()) } just runs
        every { statementService.add(any(), any(), any(), any()) } just runs

        sdgUpdater.update(contributorId, newSDGs.toSet(), subjectId)

        verify(exactly = 1) {
            statementService.findAll(
                subjectId = subjectId,
                predicateId = Predicates.sustainableDevelopmentGoal,
                pageable = PageRequests.ALL
            )
        }
        verify(exactly = 1) { statementService.delete(setOf(oldSDGStatements[0].id!!)) }
        verify(exactly = 1) {
            statementService.add(
                userId = contributorId,
                subject = subjectId,
                predicate = Predicates.sustainableDevelopmentGoal,
                `object` = newSDGs[1]
            )
        }
    }

    @Test
    fun `Given a set of sdgs, when new sdgs are identical to old sdgs, it does not modify any statements`() {
        val subjectId = ThingId("R123")
        val newSDGs = listOf(ThingId("SDG_1"), ThingId("SDG_2"))
        val oldSDGStatements = newSDGs.toSDGStatements(subjectId)
        val contributorId = ContributorId(UUID.randomUUID())

        every {
            statementService.findAll(
                subjectId = subjectId,
                predicateId = Predicates.sustainableDevelopmentGoal,
                pageable = PageRequests.ALL
            )
        } returns pageOf(oldSDGStatements)

        sdgUpdater.update(contributorId, newSDGs.toSet(), subjectId)

        verify(exactly = 1) {
            statementService.findAll(
                subjectId = subjectId,
                predicateId = Predicates.sustainableDevelopmentGoal,
                pageable = PageRequests.ALL
            )
        }
    }

    @Test
    fun `Given a set of sdgs, when new set of sdgs is empty, it removes all old sdg statements`() {
        val subjectId = ThingId("R123")
        val newSDGs = emptySet<ThingId>()
        val oldSDGStatements = listOf(ThingId("SDG_1"), ThingId("SDG_2")).toSDGStatements(subjectId)
        val contributorId = ContributorId(UUID.randomUUID())

        every {
            statementService.findAll(
                subjectId = subjectId,
                predicateId = Predicates.sustainableDevelopmentGoal,
                pageable = PageRequests.ALL
            )
        } returns pageOf(oldSDGStatements)
        every { statementService.delete(any<Set<StatementId>>()) } just runs

        sdgUpdater.update(contributorId, newSDGs.toSet(), subjectId)

        verify(exactly = 1) {
            statementService.findAll(
                subjectId = subjectId,
                predicateId = Predicates.sustainableDevelopmentGoal,
                pageable = PageRequests.ALL
            )
        }
        verify(exactly = 1) { statementService.delete(oldSDGStatements.map { it.id!! }.toSet()) }
    }

    @Test
    fun `Given a set of sdgs, when old set of sdgs is empty, it creates new sdgs statements`() {
        val subjectId = ThingId("R123")
        val newSDGs = listOf(ThingId("SDG_1"), ThingId("SDG_2"))
        val contributorId = ContributorId(UUID.randomUUID())

        every {
            statementService.findAll(
                subjectId = subjectId,
                predicateId = Predicates.sustainableDevelopmentGoal,
                pageable = PageRequests.ALL
            )
        } returns pageOf()
        every { statementService.add(any(), any(), any(), any()) } just runs

        sdgUpdater.update(contributorId, newSDGs.toSet(), subjectId)

        verify(exactly = 1) {
            statementService.findAll(
                subjectId = subjectId,
                predicateId = Predicates.sustainableDevelopmentGoal,
                pageable = PageRequests.ALL
            )
        }
        verify(exactly = 1) {
            statementService.add(
                userId = contributorId,
                subject = subjectId,
                predicate = Predicates.sustainableDevelopmentGoal,
                `object` = newSDGs[0]
            )
        }
        verify(exactly = 1) {
            statementService.add(
                userId = contributorId,
                subject = subjectId,
                predicate = Predicates.sustainableDevelopmentGoal,
                `object` = newSDGs[1]
            )
        }
    }

    private fun Collection<ThingId>.toSDGStatements(subjectId: ThingId): List<GeneralStatement> = map {
        createStatement(
            id = StatementId("S${it.value}"),
            subject = createResource(subjectId),
            predicate = createPredicate(Predicates.sustainableDevelopmentGoal),
            `object` = createResource(it, classes = setOf(Classes.sustainableDevelopmentGoal))
        )
    }
}
