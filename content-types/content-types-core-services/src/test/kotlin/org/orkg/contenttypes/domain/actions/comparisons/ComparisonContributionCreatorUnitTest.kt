package org.orkg.contenttypes.domain.actions.comparisons

import io.kotest.assertions.asClue
import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.just
import io.mockk.mockk
import io.mockk.runs
import io.mockk.verify
import org.junit.jupiter.api.Test
import org.orkg.common.ThingId
import org.orkg.common.testing.fixtures.MockkBaseTest
import org.orkg.contenttypes.domain.actions.CreateComparisonState
import org.orkg.contenttypes.input.testing.fixtures.createComparisonCommand
import org.orkg.graph.domain.Predicates
import org.orkg.graph.input.StatementUseCases

internal class ComparisonContributionCreatorUnitTest : MockkBaseTest {
    private val statementService: StatementUseCases = mockk()

    private val contributionCreator = ComparisonContributionCreator(statementService)

    @Test
    fun `Given a subject resource, when linking contributions, it returns success`() {
        val comparisonId = ThingId("R12")
        val command = createComparisonCommand()
        val state = CreateComparisonState(
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
