package org.orkg.contenttypes.domain.actions.papers

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
import org.orkg.contenttypes.domain.actions.UpdatePaperState
import org.orkg.contenttypes.domain.testing.fixtures.createDummyPaper
import org.orkg.contenttypes.input.testing.fixtures.dummyUpdatePaperCommand
import org.orkg.graph.domain.Predicates

class PaperSDGUpdaterUnitTest {
    private val statementCollectionPropertyUpdater: StatementCollectionPropertyUpdater = mockk()

    private val paperSDGUpdater = PaperSDGUpdater(statementCollectionPropertyUpdater)

    @BeforeEach
    fun resetState() {
        clearAllMocks()
    }

    @AfterEach
    fun verifyMocks() {
        confirmVerified(statementCollectionPropertyUpdater)
    }

    @Test
    fun `Given a paper update command, it updates the sdgs`() {
        val command = dummyUpdatePaperCommand()
        val state = UpdatePaperState(paper = createDummyPaper())

        every {
            statementCollectionPropertyUpdater.update(
                contributorId = command.contributorId,
                subjectId = command.paperId,
                predicateId = Predicates.sustainableDevelopmentGoal,
                objects = command.sustainableDevelopmentGoals!!
            )
        } just runs

        paperSDGUpdater(command, state)

        verify(exactly = 1) {
            statementCollectionPropertyUpdater.update(
                contributorId = command.contributorId,
                subjectId = command.paperId,
                predicateId = Predicates.sustainableDevelopmentGoal,
                objects = command.sustainableDevelopmentGoals!!
            )
        }
    }

    @Test
    fun `Given a paper update command, when new sdgs set is identical to new sdgs set, it does nothing`() {
        val command = dummyUpdatePaperCommand()
        val state = UpdatePaperState(
            paper = createDummyPaper().copy(
                sustainableDevelopmentGoals = command.sustainableDevelopmentGoals!!.map { ObjectIdAndLabel(it, "irrelevant") }.toSet()
            )
        )
        paperSDGUpdater(command, state)
    }

    @Test
    fun `Given a paper update command, when no sdgs set is set, it does nothing`() {
        val command = dummyUpdatePaperCommand().copy(sustainableDevelopmentGoals = null)
        val state = UpdatePaperState()

        paperSDGUpdater(command, state)
    }
}
