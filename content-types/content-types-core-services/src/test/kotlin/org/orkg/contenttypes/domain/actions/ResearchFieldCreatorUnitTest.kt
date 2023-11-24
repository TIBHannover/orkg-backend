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
import org.orkg.common.ThingId
import org.orkg.graph.domain.Predicates
import org.orkg.graph.input.StatementUseCases

class ResearchFieldCreatorUnitTest {
    private val statementService: StatementUseCases = mockk()

    private val researchFieldCreator = object : ResearchFieldCreator(statementService) {}

    @BeforeEach
    fun resetState() {
        clearAllMocks()
    }

    @AfterEach
    fun verifyMocks() {
        confirmVerified(statementService)
    }

    @Test
    fun `Given a subject resource, when linking research fields, it returns success`() {
        val id = ThingId("R12")
        val contributorId = ContributorId(UUID.randomUUID())
        val subjectId = ThingId("R123")

        every {
            statementService.add(
                userId = contributorId,
                subject = subjectId,
                predicate = Predicates.hasResearchField,
                `object` = id
            )
        } just runs

        researchFieldCreator.create(contributorId, listOf(id), subjectId)

        verify(exactly = 1) {
            statementService.add(
                userId = contributorId,
                subject = subjectId,
                predicate = Predicates.hasResearchField,
                `object` = id
            )
        }
    }
}
