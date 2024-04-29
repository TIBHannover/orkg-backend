package org.orkg.contenttypes.domain.actions.papers

import io.kotest.assertions.asClue
import io.kotest.matchers.shouldBe
import io.mockk.clearAllMocks
import io.mockk.confirmVerified
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.orkg.contenttypes.domain.PaperAlreadyExists
import org.orkg.contenttypes.domain.actions.CreatePaperState
import org.orkg.contenttypes.input.testing.fixtures.dummyCreatePaperCommand
import org.orkg.graph.domain.InvalidLabel
import org.orkg.graph.input.ResourceUseCases
import org.orkg.graph.testing.fixtures.createResource
import org.orkg.testing.pageOf
import org.springframework.data.domain.Page

class PaperTitleCreateValidatorUnitTest {
    private val resourceService: ResourceUseCases = mockk()

    private val paperTitleCreateValidator = PaperTitleCreateValidator(
        resourceService = resourceService
    )

    @BeforeEach
    fun resetState() {
        clearAllMocks()
    }

    @AfterEach
    fun verifyMocks() {
        confirmVerified(resourceService)
    }

    @Test
    fun `Given a paper create command, when searching for existing papers, it returns success`() {
        val command = dummyCreatePaperCommand()
        val state = CreatePaperState()

        every { resourceService.findAllPapersByTitle(command.title) } returns Page.empty()

        val result = paperTitleCreateValidator(command, state)

        result.asClue {
            it.tempIds.size shouldBe 0
            it.validatedIds.size shouldBe 0
            it.bakedStatements.size shouldBe 0
            it.authors.size shouldBe 0
            it.paperId shouldBe null
        }

        verify(exactly = 1) { resourceService.findAllPapersByTitle(command.title) }
    }

    @Test
    fun `Given a paper create command, when searching for existing papers, and title matches, it throws an exception`() {
        val command = dummyCreatePaperCommand()
        val state = CreatePaperState()
        val paper = createResource(label = command.title)
        val expected = PaperAlreadyExists.withTitle(paper.label)

        every { resourceService.findAllPapersByTitle(command.title) } returns pageOf(paper)

        assertThrows<PaperAlreadyExists> { paperTitleCreateValidator(command, state) }.message shouldBe expected.message

        verify(exactly = 1) { resourceService.findAllPapersByTitle(command.title) }
    }

    @Test
    fun `Given a paper create command, when paper label is invalid, it throws an exception`() {
        val command = dummyCreatePaperCommand().copy(title = "\n")
        val state = CreatePaperState()

        assertThrows<InvalidLabel> { paperTitleCreateValidator(command, state) }.asClue {
            it.property shouldBe "title"
        }
    }
}
