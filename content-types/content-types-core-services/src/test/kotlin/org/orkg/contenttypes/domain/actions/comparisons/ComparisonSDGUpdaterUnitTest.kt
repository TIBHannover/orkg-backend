package org.orkg.contenttypes.domain.actions.comparisons

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
import org.orkg.contenttypes.domain.ObjectIdAndLabel
import org.orkg.contenttypes.domain.actions.StatementCollectionPropertyUpdater
import org.orkg.contenttypes.domain.actions.UpdateComparisonState
import org.orkg.contenttypes.domain.testing.fixtures.createDummyComparison
import org.orkg.contenttypes.input.testing.fixtures.dummyUpdateComparisonCommand
import org.orkg.graph.domain.Predicates

class ComparisonSDGUpdaterUnitTest {
    private val statementCollectionPropertyUpdater: StatementCollectionPropertyUpdater = mockk()

    private val comparisonSDGUpdater = ComparisonSDGUpdater(statementCollectionPropertyUpdater)

    @BeforeEach
    fun resetState() {
        clearAllMocks()
    }

    @AfterEach
    fun verifyMocks() {
        confirmVerified(statementCollectionPropertyUpdater)
    }

    @Test
    fun `Given a comparison update command, it updates the sdgs`() {
        val command = dummyUpdateComparisonCommand()
        val state = UpdateComparisonState(comparison = createDummyComparison())

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
            comparison = createDummyComparison().copy(
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
