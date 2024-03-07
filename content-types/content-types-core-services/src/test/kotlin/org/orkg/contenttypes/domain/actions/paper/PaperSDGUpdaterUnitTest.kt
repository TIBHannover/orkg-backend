package org.orkg.contenttypes.domain.actions.paper

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
import org.orkg.contenttypes.domain.actions.SDGUpdater
import org.orkg.contenttypes.domain.actions.UpdatePaperState
import org.orkg.contenttypes.domain.testing.fixtures.createDummyPaper
import org.orkg.contenttypes.input.testing.fixtures.dummyUpdatePaperCommand

class PaperSDGUpdaterUnitTest {
    private val sdgUpdater: SDGUpdater = mockk()

    private val paperSDGUpdater = PaperSDGUpdater(sdgUpdater)

    @BeforeEach
    fun resetState() {
        clearAllMocks()
    }

    @AfterEach
    fun verifyMocks() {
        confirmVerified(sdgUpdater)
    }

    @Test
    fun `Given a paper update command, it updates the sdgs`() {
        val command = dummyUpdatePaperCommand()
        val state = UpdatePaperState(paper = createDummyPaper())

        every { sdgUpdater.update(command.contributorId, command.sustainableDevelopmentGoals!!, command.paperId) } just runs

        paperSDGUpdater(command, state)

        verify(exactly = 1) { sdgUpdater.update(command.contributorId, command.sustainableDevelopmentGoals!!, command.paperId) }
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
