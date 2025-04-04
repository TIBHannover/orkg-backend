package org.orkg.contenttypes.domain.actions.smartreviews

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
import org.orkg.contenttypes.domain.actions.UpdateSmartReviewState
import org.orkg.contenttypes.input.testing.fixtures.updateSmartReviewCommand
import org.orkg.graph.input.UnsafeResourceUseCases
import org.orkg.graph.input.UpdateResourceUseCase

internal class SmartReviewResourceUpdaterUnitTest : MockkBaseTest {
    private val unsafeResourceUseCases: UnsafeResourceUseCases = mockk()

    private val smartReviewResourceUpdater = SmartReviewResourceUpdater(unsafeResourceUseCases)

    @Test
    fun `Given a smart review update command, it updates the smart review resource`() {
        val command = updateSmartReviewCommand()
        val state = UpdateSmartReviewState()

        val resourceUpdateCommand = UpdateResourceUseCase.UpdateCommand(
            id = command.smartReviewId,
            contributorId = command.contributorId,
            label = command.title,
            observatoryId = command.observatories!!.single(),
            organizationId = command.organizations!!.single(),
            extractionMethod = command.extractionMethod!!,
            visibility = command.visibility,
        )

        every { unsafeResourceUseCases.update(resourceUpdateCommand) } just runs

        val result = smartReviewResourceUpdater(command, state)

        result.asClue {
            it.smartReview shouldBe state.smartReview
            it.statements shouldBe state.statements
            it.authors.size shouldBe 0
        }

        verify(exactly = 1) { unsafeResourceUseCases.update(resourceUpdateCommand) }
    }

    @Test
    fun `Given a smart review update command, when observatories are empty, it removes the associated observatory`() {
        val command = updateSmartReviewCommand().copy(observatories = emptyList())
        val state = UpdateSmartReviewState()

        val resourceUpdateCommand = UpdateResourceUseCase.UpdateCommand(
            id = command.smartReviewId,
            contributorId = command.contributorId,
            label = command.title,
            observatoryId = ObservatoryId.UNKNOWN,
            organizationId = command.organizations!!.single(),
            extractionMethod = command.extractionMethod,
            visibility = command.visibility,
        )

        every { unsafeResourceUseCases.update(resourceUpdateCommand) } just runs

        val result = smartReviewResourceUpdater(command, state)

        result.asClue {
            it.smartReview shouldBe state.smartReview
        }

        verify(exactly = 1) { unsafeResourceUseCases.update(resourceUpdateCommand) }
    }

    @Test
    fun `Given a smart review update command, when observatories are not set, it does not update the associated observatory`() {
        val command = updateSmartReviewCommand().copy(observatories = null)
        val state = UpdateSmartReviewState()

        val resourceUpdateCommand = UpdateResourceUseCase.UpdateCommand(
            id = command.smartReviewId,
            contributorId = command.contributorId,
            label = command.title,
            observatoryId = null,
            organizationId = command.organizations!!.single(),
            extractionMethod = command.extractionMethod,
            visibility = command.visibility,
        )

        every { unsafeResourceUseCases.update(resourceUpdateCommand) } just runs

        val result = smartReviewResourceUpdater(command, state)

        result.asClue {
            it.smartReview shouldBe state.smartReview
        }

        verify(exactly = 1) { unsafeResourceUseCases.update(resourceUpdateCommand) }
    }

    @Test
    fun `Given a smart review update command, when organizations are empty, it removes the associated organizations`() {
        val command = updateSmartReviewCommand().copy(organizations = emptyList())
        val state = UpdateSmartReviewState()

        val resourceUpdateCommand = UpdateResourceUseCase.UpdateCommand(
            id = command.smartReviewId,
            contributorId = command.contributorId,
            label = command.title,
            observatoryId = command.observatories!!.single(),
            organizationId = OrganizationId.UNKNOWN,
            extractionMethod = command.extractionMethod,
            visibility = command.visibility,
        )

        every { unsafeResourceUseCases.update(resourceUpdateCommand) } just runs

        val result = smartReviewResourceUpdater(command, state)

        result.asClue {
            it.smartReview shouldBe state.smartReview
        }

        verify(exactly = 1) { unsafeResourceUseCases.update(resourceUpdateCommand) }
    }

    @Test
    fun `Given a smart review update command, when organizations are not set, it does not update the associated organizations`() {
        val command = updateSmartReviewCommand().copy(organizations = null)
        val state = UpdateSmartReviewState()

        val resourceUpdateCommand = UpdateResourceUseCase.UpdateCommand(
            id = command.smartReviewId,
            contributorId = command.contributorId,
            label = command.title,
            observatoryId = command.observatories!!.single(),
            organizationId = null,
            extractionMethod = command.extractionMethod,
            visibility = command.visibility,
        )

        every { unsafeResourceUseCases.update(resourceUpdateCommand) } just runs

        val result = smartReviewResourceUpdater(command, state)

        result.asClue {
            it.smartReview shouldBe state.smartReview
        }

        verify(exactly = 1) { unsafeResourceUseCases.update(resourceUpdateCommand) }
    }
}
