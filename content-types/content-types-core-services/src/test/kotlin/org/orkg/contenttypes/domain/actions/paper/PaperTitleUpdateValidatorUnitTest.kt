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
import org.orkg.common.ThingId
import org.orkg.contenttypes.domain.PaperAlreadyExists
import org.orkg.contenttypes.domain.actions.UpdatePaperState
import org.orkg.contenttypes.input.testing.fixtures.dummyUpdatePaperCommand
import org.orkg.graph.input.ResourceUseCases
import org.orkg.graph.testing.fixtures.createResource
import org.orkg.testing.pageOf

class PaperTitleUpdateValidatorUnitTest {
    private val resourceService: ResourceUseCases = mockk()

    private val paperTitleUpdateValidator = PaperTitleUpdateValidator(
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
    fun `Given a paper update command, when searching for existing papers, it returns success`() {
        val command = dummyUpdatePaperCommand()
        val state = UpdatePaperState()

        every { resourceService.findAllPapersByTitle(command.title) } returns pageOf(
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
        val command = dummyUpdatePaperCommand()
        val state = UpdatePaperState()
        val expected = PaperAlreadyExists.withTitle(command.title!!)

        every { resourceService.findAllPapersByTitle(command.title) } returns pageOf(
            createResource(id = command.paperId, label = command.title!!),
            createResource(id = ThingId("R12346"), label = command.title!!)
        )

        assertThrows<PaperAlreadyExists> { paperTitleUpdateValidator(command, state) }.message shouldBe expected.message

        verify(exactly = 1) { resourceService.findAllPapersByTitle(command.title) }
    }
}
