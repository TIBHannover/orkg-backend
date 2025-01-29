package org.orkg.contenttypes.domain.actions.papers

import io.kotest.assertions.asClue
import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.just
import io.mockk.mockk
import io.mockk.runs
import io.mockk.verify
import org.junit.jupiter.api.Test
import org.orkg.common.ObservatoryId
import org.orkg.common.OrganizationId
import org.orkg.common.testing.fixtures.MockkBaseTest
import org.orkg.contenttypes.domain.actions.UpdatePaperState
import org.orkg.contenttypes.input.testing.fixtures.updatePaperCommand
import org.orkg.graph.input.UnsafeResourceUseCases
import org.orkg.graph.input.UpdateResourceUseCase

internal class PaperResourceUpdaterUnitTest : MockkBaseTest {
    private val unsafeResourceUseCases: UnsafeResourceUseCases = mockk()

    private val paperResourceUpdater = PaperResourceUpdater(unsafeResourceUseCases)

    @Test
    fun `Given a paper update command, it updates the paper resource`() {
        val command = updatePaperCommand()
        val state = UpdatePaperState()

        val resourceUpdateCommand = UpdateResourceUseCase.UpdateCommand(
            id = command.paperId,
            contributorId = command.contributorId,
            label = command.title,
            observatoryId = command.observatories!!.single(),
            organizationId = command.organizations!!.single(),
            visibility = command.visibility,
        )

        every { unsafeResourceUseCases.update(resourceUpdateCommand) } just runs

        val result = paperResourceUpdater(command, state)

        result.asClue {
            it.paper shouldBe state.paper
            it.authors.size shouldBe 0
        }

        verify(exactly = 1) { unsafeResourceUseCases.update(resourceUpdateCommand) }
    }

    @Test
    fun `Given a paper update command, when observatories are empty, it removes the associated observatory`() {
        val command = updatePaperCommand().copy(observatories = emptyList())
        val state = UpdatePaperState()

        val resourceUpdateCommand = UpdateResourceUseCase.UpdateCommand(
            id = command.paperId,
            contributorId = command.contributorId,
            label = command.title,
            observatoryId = ObservatoryId.UNKNOWN,
            organizationId = command.organizations!!.single(),
            visibility = command.visibility,
        )

        every { unsafeResourceUseCases.update(resourceUpdateCommand) } just runs

        val result = paperResourceUpdater(command, state)

        result.asClue {
            it.paper shouldBe state.paper
        }

        verify(exactly = 1) { unsafeResourceUseCases.update(resourceUpdateCommand) }
    }

    @Test
    fun `Given a paper update command, when observatories are not set, it does not update the associated observatory`() {
        val command = updatePaperCommand().copy(observatories = null)
        val state = UpdatePaperState()

        val resourceUpdateCommand = UpdateResourceUseCase.UpdateCommand(
            id = command.paperId,
            contributorId = command.contributorId,
            label = command.title,
            observatoryId = null,
            organizationId = command.organizations!!.single(),
            visibility = command.visibility,
        )

        every { unsafeResourceUseCases.update(resourceUpdateCommand) } just runs

        val result = paperResourceUpdater(command, state)

        result.asClue {
            it.paper shouldBe state.paper
        }

        verify(exactly = 1) { unsafeResourceUseCases.update(resourceUpdateCommand) }
    }

    @Test
    fun `Given a paper update command, when organizations are empty, it removes the associated organizations`() {
        val command = updatePaperCommand().copy(organizations = emptyList())
        val state = UpdatePaperState()

        val resourceUpdateCommand = UpdateResourceUseCase.UpdateCommand(
            id = command.paperId,
            contributorId = command.contributorId,
            label = command.title,
            observatoryId = command.observatories!!.single(),
            organizationId = OrganizationId.UNKNOWN,
            visibility = command.visibility,
        )

        every { unsafeResourceUseCases.update(resourceUpdateCommand) } just runs

        val result = paperResourceUpdater(command, state)

        result.asClue {
            it.paper shouldBe state.paper
        }

        verify(exactly = 1) { unsafeResourceUseCases.update(resourceUpdateCommand) }
    }

    @Test
    fun `Given a paper update command, when organizations are not set, it does not update the associated organizations`() {
        val command = updatePaperCommand().copy(organizations = null)
        val state = UpdatePaperState()

        val resourceUpdateCommand = UpdateResourceUseCase.UpdateCommand(
            id = command.paperId,
            contributorId = command.contributorId,
            label = command.title,
            observatoryId = command.observatories!!.single(),
            organizationId = null,
            visibility = command.visibility,
        )

        every { unsafeResourceUseCases.update(resourceUpdateCommand) } just runs

        val result = paperResourceUpdater(command, state)

        result.asClue {
            it.paper shouldBe state.paper
        }

        verify(exactly = 1) { unsafeResourceUseCases.update(resourceUpdateCommand) }
    }
}
