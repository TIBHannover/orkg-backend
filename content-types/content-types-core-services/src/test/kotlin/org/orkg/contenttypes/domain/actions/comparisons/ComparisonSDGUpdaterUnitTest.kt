package org.orkg.contenttypes.domain.actions.comparisons

import io.mockk.every
import io.mockk.just
import io.mockk.mockk
import io.mockk.runs
import io.mockk.verify
import org.junit.jupiter.api.Test
import org.orkg.common.testing.fixtures.MockkBaseTest
import org.orkg.contenttypes.domain.ObjectIdAndLabel
import org.orkg.contenttypes.domain.actions.StatementCollectionPropertyUpdater
import org.orkg.contenttypes.domain.actions.UpdateComparisonState
import org.orkg.contenttypes.domain.testing.fixtures.createComparison
import org.orkg.contenttypes.input.testing.fixtures.dummyUpdateComparisonCommand
import org.orkg.graph.domain.Predicates

internal class ComparisonSDGUpdaterUnitTest : MockkBaseTest {
    private val statementCollectionPropertyUpdater: StatementCollectionPropertyUpdater = mockk()

    private val comparisonSDGUpdater = ComparisonSDGUpdater(statementCollectionPropertyUpdater)

    @Test
    fun `Given a comparison update command, it updates the sdgs`() {
        val command = dummyUpdateComparisonCommand()
        val state = UpdateComparisonState(comparison = createComparison())

        every {
            statementCollectionPropertyUpdater.update(
                contributorId = command.contributorId,
                subjectId = command.comparisonId,
                predicateId = Predicates.sustainableDevelopmentGoal,
                objects = command.sustainableDevelopmentGoals!!
            )
        } just runs

        comparisonSDGUpdater(command, state)

        verify(exactly = 1) {
            statementCollectionPropertyUpdater.update(
                contributorId = command.contributorId,
                subjectId = command.comparisonId,
                predicateId = Predicates.sustainableDevelopmentGoal,
                objects = command.sustainableDevelopmentGoals!!
            )
        }
    }

    @Test
    fun `Given a comparison update command, when new sdgs set is identical to new sdgs set, it does nothing`() {
        val command = dummyUpdateComparisonCommand()
        val state = UpdateComparisonState(
            comparison = createComparison().copy(
                sustainableDevelopmentGoals = command.sustainableDevelopmentGoals!!.map { ObjectIdAndLabel(it, "irrelevant") }.toSet()
            )
        )
        comparisonSDGUpdater(command, state)
    }

    @Test
    fun `Given a comparison update command, when no sdgs set is set, it does nothing`() {
        val command = dummyUpdateComparisonCommand().copy(sustainableDevelopmentGoals = null)
        val state = UpdateComparisonState()

        comparisonSDGUpdater(command, state)
    }
}
