package org.orkg.contenttypes.domain.actions.comparisons

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
import org.orkg.contenttypes.domain.actions.UpdateComparisonState
import org.orkg.contenttypes.input.testing.fixtures.dummyUpdateComparisonCommand
import org.orkg.graph.input.ResourceUseCases
import org.orkg.graph.input.UpdateResourceUseCase

class ComparisonResourceUpdaterUnitTest {
    private val resourceService: ResourceUseCases = mockk()

    private val comparisonResourceUpdater = ComparisonResourceUpdater(resourceService)

    @BeforeEach
    fun resetState() {
        clearAllMocks()
    }

    @AfterEach
    fun verifyMocks() {
        confirmVerified(resourceService)
    }

    @Test
    fun `Given a comparison update command, it updates the comparison resource`() {
        val command = dummyUpdateComparisonCommand()
        val state = UpdateComparisonState()

        val resourceUpdateCommand = UpdateResourceUseCase.UpdateCommand(
            id = command.comparisonId,
            label = command.title,
            observatoryId = command.observatories!!.single(),
            organizationId = command.organizations!!.single(),
            extractionMethod = command.extractionMethod
        )

        every { resourceService.update(resourceUpdateCommand) } just runs

        val result = comparisonResourceUpdater(command, state)

        result.asClue {
            it.comparison shouldBe state.comparison
        }

        verify(exactly = 1) { resourceService.update(resourceUpdateCommand) }
    }

    @Test
    fun `Given a comparison update command, when observatories are empty, it removes the associated observatory`() {
        val command = dummyUpdateComparisonCommand().copy(observatories = emptyList())
        val state = UpdateComparisonState()

        val resourceUpdateCommand = UpdateResourceUseCase.UpdateCommand(
            id = command.comparisonId,
            label = command.title,
            observatoryId = ObservatoryId.UNKNOWN,
            organizationId = command.organizations!!.single(),
            extractionMethod = command.extractionMethod
        )

        every { resourceService.update(resourceUpdateCommand) } just runs

        val result = comparisonResourceUpdater(command, state)

        result.asClue {
            it.comparison shouldBe state.comparison
        }

        verify(exactly = 1) { resourceService.update(resourceUpdateCommand) }
    }

    @Test
    fun `Given a comparison update command, when observatories are not set, it does not update the associated observatory`() {
        val command = dummyUpdateComparisonCommand().copy(observatories = null)
        val state = UpdateComparisonState()

        val resourceUpdateCommand = UpdateResourceUseCase.UpdateCommand(
            id = command.comparisonId,
            label = command.title,
            observatoryId = null,
            organizationId = command.organizations!!.single(),
            extractionMethod = command.extractionMethod
        )

        every { resourceService.update(resourceUpdateCommand) } just runs

        val result = comparisonResourceUpdater(command, state)

        result.asClue {
            it.comparison shouldBe state.comparison
        }

        verify(exactly = 1) { resourceService.update(resourceUpdateCommand) }
    }

    @Test
    fun `Given a comparison update command, when organizations are empty, it removes the associated organizations`() {
        val command = dummyUpdateComparisonCommand().copy(organizations = emptyList())
        val state = UpdateComparisonState()

        val resourceUpdateCommand = UpdateResourceUseCase.UpdateCommand(
            id = command.comparisonId,
            label = command.title,
            observatoryId = command.observatories!!.single(),
            organizationId = OrganizationId.UNKNOWN,
            extractionMethod = command.extractionMethod
        )

        every { resourceService.update(resourceUpdateCommand) } just runs

        val result = comparisonResourceUpdater(command, state)

        result.asClue {
            it.comparison shouldBe state.comparison
        }

        verify(exactly = 1) { resourceService.update(resourceUpdateCommand) }
    }

    @Test
    fun `Given a comparison update command, when organizations are not set, it does not update the associated organizations`() {
        val command = dummyUpdateComparisonCommand().copy(organizations = null)
        val state = UpdateComparisonState()

        val resourceUpdateCommand = UpdateResourceUseCase.UpdateCommand(
            id = command.comparisonId,
            label = command.title,
            observatoryId = command.observatories!!.single(),
            organizationId = null,
            extractionMethod = command.extractionMethod
        )

        every { resourceService.update(resourceUpdateCommand) } just runs

        val result = comparisonResourceUpdater(command, state)

        result.asClue {
            it.comparison shouldBe state.comparison
        }

        verify(exactly = 1) { resourceService.update(resourceUpdateCommand) }
    }
}
