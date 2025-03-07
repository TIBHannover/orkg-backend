package org.orkg.contenttypes.domain.actions.comparisons

import io.kotest.assertions.asClue
import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.junit.jupiter.api.Test
import org.orkg.common.ThingId
import org.orkg.common.testing.fixtures.MockkBaseTest
import org.orkg.contenttypes.domain.actions.CreateComparisonState
import org.orkg.contenttypes.input.testing.fixtures.createComparisonCommand
import org.orkg.graph.domain.Predicates
import org.orkg.graph.domain.StatementId
import org.orkg.graph.input.CreateStatementUseCase
import org.orkg.graph.input.UnsafeStatementUseCases

internal class ComparisonVisualizationCreatorUnitTest : MockkBaseTest {
    private val unsafeStatementUseCases: UnsafeStatementUseCases = mockk()

    private val visualizationCreator = ComparisonVisualizationCreator(unsafeStatementUseCases)

    @Test
    fun `Given a subject resource, when linking visualizations, it returns success`() {
        val comparisonId = ThingId("R12")
        val command = createComparisonCommand()
        val state = CreateComparisonState(
            comparisonId = comparisonId
        )

        every { unsafeStatementUseCases.create(any()) } returns StatementId("S1")

        val result = visualizationCreator(command, state)

        result.asClue {
            it.authors.size shouldBe 0
            it.comparisonId shouldBe state.comparisonId
        }

        command.visualizations.forEach {
            verify(exactly = 1) {
                unsafeStatementUseCases.create(
                    CreateStatementUseCase.CreateCommand(
                        contributorId = command.contributorId,
                        subjectId = comparisonId,
                        predicateId = Predicates.hasVisualization,
                        objectId = it
                    )
                )
            }
        }
    }
}
