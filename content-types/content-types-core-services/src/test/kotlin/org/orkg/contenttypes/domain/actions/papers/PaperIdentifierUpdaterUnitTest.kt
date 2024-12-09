package org.orkg.contenttypes.domain.actions.papers

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
import org.orkg.contenttypes.domain.Identifiers
import org.orkg.contenttypes.domain.actions.IdentifierUpdater
import org.orkg.contenttypes.domain.actions.UpdatePaperState
import org.orkg.contenttypes.domain.testing.fixtures.createDummyPaper
import org.orkg.contenttypes.input.testing.fixtures.dummyUpdatePaperCommand
import org.orkg.graph.testing.fixtures.createStatement

internal class PaperIdentifierUpdaterUnitTest {
    private val identifierUpdater: IdentifierUpdater = mockk()

    private val paperIdentifierUpdater = PaperIdentifierUpdater(identifierUpdater)

    @BeforeEach
    fun resetState() {
        clearAllMocks()
    }

    @AfterEach
    fun verifyMocks() {
        confirmVerified(identifierUpdater)
    }

    @Test
    fun `Given a paper update command, it updates paper identifier`() {
        val command = dummyUpdatePaperCommand()
        val paper = createDummyPaper()
        val state = UpdatePaperState(
            paper = paper,
            statements = mapOf(
                paper.id to listOf(createStatement())
            )
        )

        every {
            identifierUpdater.update(
                statements = state.statements,
                contributorId = command.contributorId,
                newIdentifiers = command.identifiers!!,
                identifierDefinitions = Identifiers.paper,
                subjectId = state.paper!!.id
            )
        } just runs

        val result = paperIdentifierUpdater(command, state)

        result.asClue {
            it.paper shouldBe paper
            it.statements shouldBe state.statements
            it.authors.size shouldBe 0
        }

        verify(exactly = 1) {
            identifierUpdater.update(
                statements = state.statements,
                contributorId = command.contributorId,
                newIdentifiers = command.identifiers!!,
                identifierDefinitions = Identifiers.paper,
                subjectId = state.paper!!.id
            )
        }
    }

    @Test
    fun `Given a paper update command, when no new identifiers are set, it does nothing`() {
        val command = dummyUpdatePaperCommand().copy(identifiers = null)
        val state = UpdatePaperState(paper = createDummyPaper())

        paperIdentifierUpdater(command, state)
    }
}
