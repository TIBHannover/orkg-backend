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
import org.orkg.common.ObservatoryId
import org.orkg.common.OrganizationId
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

    @Test
    fun `Given a paper update command, when observatories are empty, it removes the associated observatory`() {
        val command = dummyUpdatePaperCommand().copy(observatories = emptyList())
        val state = UpdatePaperState()

        val resourceUpdateCommand = UpdateResourceUseCase.UpdateCommand(
            id = command.paperId,
            label = command.title,
            observatoryId = ObservatoryId.UNKNOWN,
            organizationId = command.organizations!!.single()
        )

        every { resourceService.update(resourceUpdateCommand) } just runs

        val result = paperResourceUpdater(command, state)

        result.asClue {
            it.paper shouldBe state.paper
        }

        verify(exactly = 1) { resourceService.update(resourceUpdateCommand) }
    }

    @Test
    fun `Given a paper update command, when observatories are not set, it does not update the associated observatory`() {
        val command = dummyUpdatePaperCommand().copy(observatories = null)
        val state = UpdatePaperState()

        val resourceUpdateCommand = UpdateResourceUseCase.UpdateCommand(
            id = command.paperId,
            label = command.title,
            observatoryId = null,
            organizationId = command.organizations!!.single()
        )

        every { resourceService.update(resourceUpdateCommand) } just runs

        val result = paperResourceUpdater(command, state)

        result.asClue {
            it.paper shouldBe state.paper
        }

        verify(exactly = 1) { resourceService.update(resourceUpdateCommand) }
    }

    @Test
    fun `Given a paper update command, when organizations are empty, it removes the associated organizations`() {
        val command = dummyUpdatePaperCommand().copy(organizations = emptyList())
        val state = UpdatePaperState()

        val resourceUpdateCommand = UpdateResourceUseCase.UpdateCommand(
            id = command.paperId,
            label = command.title,
            observatoryId = command.observatories!!.single(),
            organizationId = OrganizationId.UNKNOWN
        )

        every { resourceService.update(resourceUpdateCommand) } just runs

        val result = paperResourceUpdater(command, state)

        result.asClue {
            it.paper shouldBe state.paper
        }

        verify(exactly = 1) { resourceService.update(resourceUpdateCommand) }
    }

    @Test
    fun `Given a paper update command, when organizations are not set, it does not update the associated organizations`() {
        val command = dummyUpdatePaperCommand().copy(organizations = null)
        val state = UpdatePaperState()

        val resourceUpdateCommand = UpdateResourceUseCase.UpdateCommand(
            id = command.paperId,
            label = command.title,
            observatoryId = command.observatories!!.single(),
            organizationId = null
        )

        every { resourceService.update(resourceUpdateCommand) } just runs

        val result = paperResourceUpdater(command, state)

        result.asClue {
            it.paper shouldBe state.paper
        }

        verify(exactly = 1) { resourceService.update(resourceUpdateCommand) }
    }
}
