package org.orkg.contenttypes.domain.actions.smartreviews

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
import org.orkg.contenttypes.domain.actions.UpdateSmartReviewState
import org.orkg.contenttypes.input.testing.fixtures.dummyUpdateSmartReviewCommand
import org.orkg.graph.input.ResourceUseCases
import org.orkg.graph.input.UpdateResourceUseCase

internal class SmartReviewResourceUpdaterUnitTest {
    private val resourceService: ResourceUseCases = mockk()

    private val smartReviewResourceUpdater = SmartReviewResourceUpdater(resourceService)

    @BeforeEach
    fun resetState() {
        clearAllMocks()
    }

    @AfterEach
    fun verifyMocks() {
        confirmVerified(resourceService)
    }

    @Test
    fun `Given a smart review update command, it updates the smart review resource`() {
        val command = dummyUpdateSmartReviewCommand()
        val state = UpdateSmartReviewState()

        val resourceUpdateCommand = UpdateResourceUseCase.UpdateCommand(
            id = command.smartReviewId,
            label = command.title,
            observatoryId = command.observatories!!.single(),
            organizationId = command.organizations!!.single(),
            extractionMethod = command.extractionMethod!!
        )

        every { resourceService.update(resourceUpdateCommand) } just runs

        val result = smartReviewResourceUpdater(command, state)

        result.asClue {
            it.smartReview shouldBe state.smartReview
            it.statements shouldBe state.statements
            it.authors.size shouldBe 0
        }

        verify(exactly = 1) { resourceService.update(resourceUpdateCommand) }
    }

    @Test
    fun `Given a smart review update command, when observatories are empty, it removes the associated observatory`() {
        val command = dummyUpdateSmartReviewCommand().copy(observatories = emptyList())
        val state = UpdateSmartReviewState()

        val resourceUpdateCommand = UpdateResourceUseCase.UpdateCommand(
            id = command.smartReviewId,
            label = command.title,
            observatoryId = ObservatoryId.UNKNOWN,
            organizationId = command.organizations!!.single(),
            extractionMethod = command.extractionMethod
        )

        every { resourceService.update(resourceUpdateCommand) } just runs

        val result = smartReviewResourceUpdater(command, state)

        result.asClue {
            it.smartReview shouldBe state.smartReview
        }

        verify(exactly = 1) { resourceService.update(resourceUpdateCommand) }
    }

    @Test
    fun `Given a smart review update command, when observatories are not set, it does not update the associated observatory`() {
        val command = dummyUpdateSmartReviewCommand().copy(observatories = null)
        val state = UpdateSmartReviewState()

        val resourceUpdateCommand = UpdateResourceUseCase.UpdateCommand(
            id = command.smartReviewId,
            label = command.title,
            observatoryId = null,
            organizationId = command.organizations!!.single(),
            extractionMethod = command.extractionMethod
        )

        every { resourceService.update(resourceUpdateCommand) } just runs

        val result = smartReviewResourceUpdater(command, state)

        result.asClue {
            it.smartReview shouldBe state.smartReview
        }

        verify(exactly = 1) { resourceService.update(resourceUpdateCommand) }
    }

    @Test
    fun `Given a smart review update command, when organizations are empty, it removes the associated organizations`() {
        val command = dummyUpdateSmartReviewCommand().copy(organizations = emptyList())
        val state = UpdateSmartReviewState()

        val resourceUpdateCommand = UpdateResourceUseCase.UpdateCommand(
            id = command.smartReviewId,
            label = command.title,
            observatoryId = command.observatories!!.single(),
            organizationId = OrganizationId.UNKNOWN,
            extractionMethod = command.extractionMethod
        )

        every { resourceService.update(resourceUpdateCommand) } just runs

        val result = smartReviewResourceUpdater(command, state)

        result.asClue {
            it.smartReview shouldBe state.smartReview
        }

        verify(exactly = 1) { resourceService.update(resourceUpdateCommand) }
    }

    @Test
    fun `Given a smart review update command, when organizations are not set, it does not update the associated organizations`() {
        val command = dummyUpdateSmartReviewCommand().copy(organizations = null)
        val state = UpdateSmartReviewState()

        val resourceUpdateCommand = UpdateResourceUseCase.UpdateCommand(
            id = command.smartReviewId,
            label = command.title,
            observatoryId = command.observatories!!.single(),
            organizationId = null,
            extractionMethod = command.extractionMethod
        )

        every { resourceService.update(resourceUpdateCommand) } just runs

        val result = smartReviewResourceUpdater(command, state)

        result.asClue {
            it.smartReview shouldBe state.smartReview
        }

        verify(exactly = 1) { resourceService.update(resourceUpdateCommand) }
    }
}
