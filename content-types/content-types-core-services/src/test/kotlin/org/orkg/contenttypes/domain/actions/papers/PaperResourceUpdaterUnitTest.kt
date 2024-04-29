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
import org.orkg.contenttypes.domain.actions.UpdatePaperState
import org.orkg.contenttypes.input.testing.fixtures.dummyUpdatePaperCommand
import org.orkg.graph.input.ResourceUseCases
import org.orkg.graph.input.UpdateResourceUseCase

class PaperResourceUpdaterUnitTest {
    private val resourceService: ResourceUseCases = mockk()

    private val paperResourceUpdater = PaperResourceUpdater(resourceService)

    @BeforeEach
    fun resetState() {
        clearAllMocks()
    }

    @AfterEach
    fun verifyMocks() {
        confirmVerified(resourceService)
    }

    @Test
    fun `Given a paper update command, it updates the paper resource`() {
        val command = dummyUpdatePaperCommand()
        val state = UpdatePaperState()

        val resourceUpdateCommand = UpdateResourceUseCase.UpdateCommand(
            id = command.paperId,
            label = command.title,
            observatoryId = command.observatories!!.single(),
            organizationId = command.organizations!!.single()
        )

        every { resourceService.update(resourceUpdateCommand) } just runs

        val result = paperResourceUpdater(command, state)

        result.asClue {
            it.paper shouldBe state.paper
            it.authors.size shouldBe 0
        }

        verify(exactly = 1) { resourceService.update(resourceUpdateCommand) }
    }
}
