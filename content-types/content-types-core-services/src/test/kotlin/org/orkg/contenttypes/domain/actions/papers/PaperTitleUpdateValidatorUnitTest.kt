package org.orkg.contenttypes.domain.actions.papers

import io.kotest.assertions.asClue
import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.orkg.common.ThingId
import org.orkg.common.testing.fixtures.MockkBaseTest
import org.orkg.contenttypes.domain.PaperAlreadyExists
import org.orkg.contenttypes.domain.actions.UpdatePaperState
import org.orkg.contenttypes.domain.testing.fixtures.createPaper
import org.orkg.contenttypes.input.testing.fixtures.updatePaperCommand
import org.orkg.graph.domain.InvalidLabel
import org.orkg.graph.input.ResourceUseCases
import org.orkg.graph.testing.fixtures.createResource

internal class PaperTitleUpdateValidatorUnitTest : MockkBaseTest {
    private val resourceService: ResourceUseCases = mockk()

    private val paperTitleUpdateValidator = PaperTitleUpdateValidator(
        resourceService = resourceService
    )

    @Test
    fun `Given a paper update command, when searching for existing papers, it returns success`() {
        val command = updatePaperCommand()
        val state = UpdatePaperState(paper = createPaper())

        every { resourceService.findAllPapersByTitle(command.title) } returns listOf(
            createResource(id = command.paperId, label = command.title!!)
        )

        val result = paperTitleUpdateValidator(command, state)

        result.asClue {
            it.paper shouldBe state.paper
            it.authors shouldBe state.authors
        }

        verify(exactly = 1) { resourceService.findAllPapersByTitle(command.title) }
    }

    @Test
    fun `Given a paper update command, when searching for existing papers, and title matches, it throws an exception`() {
        val command = updatePaperCommand()
        val state = UpdatePaperState(paper = createPaper())
        val expected = PaperAlreadyExists.withTitle(command.title!!)

        every { resourceService.findAllPapersByTitle(command.title) } returns listOf(
            createResource(id = command.paperId, label = command.title!!),
            createResource(id = ThingId("R12346"), label = command.title!!)
        )

        assertThrows<PaperAlreadyExists> { paperTitleUpdateValidator(command, state) }.message shouldBe expected.message

        verify(exactly = 1) { resourceService.findAllPapersByTitle(command.title) }
    }

    @Test
    fun `Given a paper update command, when paper label is invalid, it throws an exception`() {
        val command = updatePaperCommand().copy(title = "\n")
        val state = UpdatePaperState(paper = createPaper())

        assertThrows<InvalidLabel> { paperTitleUpdateValidator(command, state) }.asClue {
            it.property shouldBe "title"
        }
    }

    @Test
    fun `Given a paper update command, when new title is identical to existing title, it does nothing`() {
        val title = "paper title"
        val command = updatePaperCommand().copy(title = title)
        val state = UpdatePaperState(paper = createPaper().copy(title = title))

        paperTitleUpdateValidator(command, state)
    }

    @Test
    fun `Given a paper update command, when there is no new title set, it does nothing`() {
        val command = updatePaperCommand().copy(title = null)
        val state = UpdatePaperState(paper = createPaper())

        paperTitleUpdateValidator(command, state)
    }
}
