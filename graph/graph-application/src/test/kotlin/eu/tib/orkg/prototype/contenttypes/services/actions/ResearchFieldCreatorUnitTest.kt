package eu.tib.orkg.prototype.contenttypes.services.actions

import eu.tib.orkg.prototype.community.domain.model.ContributorId
import eu.tib.orkg.prototype.contenttypes.testing.fixtures.dummyCreatePaperCommand
import eu.tib.orkg.prototype.statements.api.Predicates
import eu.tib.orkg.prototype.statements.api.StatementUseCases
import eu.tib.orkg.prototype.statements.domain.model.ThingId
import io.kotest.assertions.asClue
import io.kotest.matchers.shouldBe
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
