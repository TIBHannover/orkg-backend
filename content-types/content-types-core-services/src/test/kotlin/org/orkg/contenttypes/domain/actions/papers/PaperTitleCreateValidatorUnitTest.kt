package org.orkg.contenttypes.domain.actions.papers

import io.kotest.assertions.asClue
import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.orkg.common.testing.fixtures.MockkBaseTest
import org.orkg.contenttypes.domain.PaperAlreadyExists
import org.orkg.contenttypes.domain.actions.CreatePaperState
import org.orkg.contenttypes.input.testing.fixtures.createPaperCommand
import org.orkg.graph.domain.InvalidLabel
import org.orkg.graph.input.ResourceUseCases
import org.orkg.graph.testing.fixtures.createResource

internal class PaperTitleCreateValidatorUnitTest : MockkBaseTest {
    private val resourceService: ResourceUseCases = mockk()

    private val paperTitleCreateValidator = PaperTitleCreateValidator(
        resourceService = resourceService
    )

    @Test
    fun `Given a paper create command, when searching for existing papers, it returns success`() {
        val command = createPaperCommand()
        val state = CreatePaperState()

        every { resourceService.findAllPapersByTitle(command.title) } returns emptyList()

        val result = paperTitleCreateValidator(command, state)

        result.asClue {
            it.validationCache.size shouldBe 0
            it.bakedStatements.size shouldBe 0
            it.authors.size shouldBe 0
            it.paperId shouldBe null
        }

        verify(exactly = 1) { resourceService.findAllPapersByTitle(command.title) }
    }

    @Test
    fun `Given a paper create command, when searching for existing papers, and title matches, it throws an exception`() {
        val command = createPaperCommand()
        val state = CreatePaperState()
        val paper = createResource(label = command.title)
        val expected = PaperAlreadyExists.withTitle(paper.label)

        every { resourceService.findAllPapersByTitle(command.title) } returns listOf(paper)

        assertThrows<PaperAlreadyExists> { paperTitleCreateValidator(command, state) }.message shouldBe expected.message

        verify(exactly = 1) { resourceService.findAllPapersByTitle(command.title) }
    }

    @Test
    fun `Given a paper create command, when paper label is invalid, it throws an exception`() {
        val command = createPaperCommand().copy(title = "\n")
        val state = CreatePaperState()

        assertThrows<InvalidLabel> { paperTitleCreateValidator(command, state) }.asClue {
            it.property shouldBe "title"
        }
    }
}
