package eu.tib.orkg.prototype.contenttypes.services.actions.comparison

import eu.tib.orkg.prototype.contenttypes.services.actions.ComparisonState
import eu.tib.orkg.prototype.contenttypes.testing.fixtures.dummyCreateComparisonCommand
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
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

class ComparisonContributionCreatorUnitTest {
    private val statementService: StatementUseCases = mockk()

    private val contributionCreator = ComparisonContributionCreator(statementService)

    @BeforeEach
    fun resetState() {
        clearAllMocks()
    }

    @AfterEach
    fun verifyMocks() {
        confirmVerified(statementService)
    }

    @Test
    fun `Given a subject resource, when linking contributions, it returns success`() {
        val comparisonId = ThingId("R12")
        val command = dummyCreateComparisonCommand()
        val state = ComparisonState(
            comparisonId = comparisonId
        )

        every {
            statementService.add(
                userId = command.contributorId,
                subject = comparisonId,
                predicate = Predicates.comparesContribution,
                `object` = any()
            )
        } just runs

        val result = contributionCreator(command, state)

        result.asClue {
            it.authors.size shouldBe 0
            it.comparisonId shouldBe state.comparisonId
        }

        command.contributions.forEach {
            verify(exactly = 1) {
                statementService.add(
                    userId = command.contributorId,
                    subject = comparisonId,
                    predicate = Predicates.comparesContribution,
                    `object` = it
                )
            }
        }
    }
}
