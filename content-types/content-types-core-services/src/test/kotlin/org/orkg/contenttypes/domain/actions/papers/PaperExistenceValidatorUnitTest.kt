package org.orkg.contenttypes.domain.actions.papers

import io.kotest.assertions.asClue
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.matchers.shouldBe
import io.mockk.clearAllMocks
import io.mockk.confirmVerified
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import java.util.*
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.orkg.contenttypes.domain.PaperNotFound
import org.orkg.contenttypes.domain.actions.UpdatePaperState
import org.orkg.contenttypes.domain.testing.fixtures.createDummyPaper
import org.orkg.contenttypes.input.PaperUseCases
import org.orkg.contenttypes.input.testing.fixtures.dummyUpdatePaperCommand

class PaperExistenceValidatorUnitTest {
    private val paperService: PaperUseCases = mockk()

    private val paperExistenceValidator = PaperExistenceValidator(paperService)

    @BeforeEach
    fun resetState() {
        clearAllMocks()
    }

    @AfterEach
    fun verifyMocks() {
        confirmVerified(paperService)
    }

    @Test
    fun `Given a paper update command, when checking for paper existence, it returns success`() {
        val paper = createDummyPaper()
        val command = dummyUpdatePaperCommand().copy(paperId = paper.id)
        val state = UpdatePaperState()

        every { paperService.findById(paper.id) } returns Optional.of(paper)

        paperExistenceValidator(command, state).asClue {
            it.paper shouldBe paper
        }

        verify(exactly = 1) { paperService.findById(paper.id) }
    }

    @Test
    fun `Given a paper update command, when checking for paper existence and paper is not found, it throws an exception`() {
        val paper = createDummyPaper()
        val command = dummyUpdatePaperCommand().copy(paperId = paper.id)
        val state = UpdatePaperState()

        every { paperService.findById(paper.id) } returns Optional.empty()

        shouldThrow<PaperNotFound> { paperExistenceValidator(command, state) }

        verify(exactly = 1) { paperService.findById(paper.id) }
    }
}
