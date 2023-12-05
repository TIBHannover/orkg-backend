package org.orkg.contenttypes.domain.actions.paper

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
import org.orkg.contenttypes.testing.fixtures.dummyCreatePaperCommand
import org.orkg.graph.input.ResourceUseCases
import org.orkg.graph.testing.fixtures.createResource
import org.orkg.testing.pageOf
import org.springframework.data.domain.Page

class PaperTitleValidatorUnitTest {
    private val resourceService: ResourceUseCases = mockk()

    private val paperTitleValidator = PaperTitleValidator(
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

        every { resourceService.findAllByTitle(command.title) } returns Page.empty()

        val result = paperTitleValidator(command, state)

        result.asClue {
            it.tempIds.size shouldBe 0
            it.validatedIds.size shouldBe 0
            it.bakedStatements.size shouldBe 0
            it.authors.size shouldBe 0
            it.paperId shouldBe null
        }

        verify(exactly = 1) { resourceService.findAllByTitle(command.title) }
    }

    @Test
    fun `Given a paper create command, when searching for existing papers, and title matches, it throws an exception`() {
        val command = dummyCreatePaperCommand()
        val state = CreatePaperState()
        val paper = createResource(label = command.title)
        val expected = PaperAlreadyExists.withTitle(paper.label)

        every { resourceService.findAllByTitle(command.title) } returns pageOf(paper)

        assertThrows<PaperAlreadyExists> { paperTitleValidator(command, state) }.message shouldBe expected.message

        verify(exactly = 1) { resourceService.findAllByTitle(command.title) }
    }
}
