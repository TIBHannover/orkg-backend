package org.orkg.contenttypes.domain.actions.comparisons

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
import org.orkg.contenttypes.domain.actions.UpdateComparisonState
import org.orkg.contenttypes.input.testing.fixtures.updateComparisonCommand
import org.orkg.graph.input.UnsafeResourceUseCases
import org.orkg.graph.input.UpdateResourceUseCase

internal class ComparisonResourceUpdaterUnitTest : MockkBaseTest {
    private val unsafeResourceUseCases: UnsafeResourceUseCases = mockk()

    private val comparisonResourceUpdater = ComparisonResourceUpdater(unsafeResourceUseCases)

    @Test
    fun `Given a comparison update command, it updates the comparison resource`() {
        val command = updateComparisonCommand()
        val state = UpdateComparisonState()

        val resourceUpdateCommand = UpdateResourceUseCase.UpdateCommand(
            id = command.comparisonId,
            contributorId = command.contributorId,
            label = command.title,
            observatoryId = command.observatories!!.single(),
            organizationId = command.organizations!!.single(),
            extractionMethod = command.extractionMethod,
            visibility = command.visibility,
        )

        every { unsafeResourceUseCases.update(resourceUpdateCommand) } just runs

        val result = comparisonResourceUpdater(command, state)

        result.asClue {
            it.comparison shouldBe state.comparison
        }

        verify(exactly = 1) { unsafeResourceUseCases.update(resourceUpdateCommand) }
    }

    @Test
    fun `Given a comparison update command, when observatories are empty, it removes the associated observatory`() {
        val command = updateComparisonCommand().copy(observatories = emptyList())
        val state = UpdateComparisonState()

        val resourceUpdateCommand = UpdateResourceUseCase.UpdateCommand(
            id = command.comparisonId,
            contributorId = command.contributorId,
            label = command.title,
            observatoryId = ObservatoryId.UNKNOWN,
            organizationId = command.organizations!!.single(),
            extractionMethod = command.extractionMethod,
            visibility = command.visibility,
        )

        every { unsafeResourceUseCases.update(resourceUpdateCommand) } just runs

        val result = comparisonResourceUpdater(command, state)

        result.asClue {
            it.comparison shouldBe state.comparison
        }

        verify(exactly = 1) { unsafeResourceUseCases.update(resourceUpdateCommand) }
    }

    @Test
    fun `Given a comparison update command, when observatories are not set, it does not update the associated observatory`() {
        val command = updateComparisonCommand().copy(observatories = null)
        val state = UpdateComparisonState()

        val resourceUpdateCommand = UpdateResourceUseCase.UpdateCommand(
            id = command.comparisonId,
            contributorId = command.contributorId,
            label = command.title,
            observatoryId = null,
            organizationId = command.organizations!!.single(),
            extractionMethod = command.extractionMethod,
            visibility = command.visibility,
        )

        every { unsafeResourceUseCases.update(resourceUpdateCommand) } just runs

        val result = comparisonResourceUpdater(command, state)

        result.asClue {
            it.comparison shouldBe state.comparison
        }

        verify(exactly = 1) { unsafeResourceUseCases.update(resourceUpdateCommand) }
    }

    @Test
    fun `Given a comparison update command, when organizations are empty, it removes the associated organizations`() {
        val command = updateComparisonCommand().copy(organizations = emptyList())
        val state = UpdateComparisonState()

        val resourceUpdateCommand = UpdateResourceUseCase.UpdateCommand(
            id = command.comparisonId,
            contributorId = command.contributorId,
            label = command.title,
            observatoryId = command.observatories!!.single(),
            organizationId = OrganizationId.UNKNOWN,
            extractionMethod = command.extractionMethod,
            visibility = command.visibility,
        )

        every { unsafeResourceUseCases.update(resourceUpdateCommand) } just runs

        val result = comparisonResourceUpdater(command, state)

        result.asClue {
            it.comparison shouldBe state.comparison
        }

        verify(exactly = 1) { unsafeResourceUseCases.update(resourceUpdateCommand) }
    }

    @Test
    fun `Given a comparison update command, when organizations are not set, it does not update the associated organizations`() {
        val command = updateComparisonCommand().copy(organizations = null)
        val state = UpdateComparisonState()

        val resourceUpdateCommand = UpdateResourceUseCase.UpdateCommand(
            id = command.comparisonId,
            contributorId = command.contributorId,
            label = command.title,
            observatoryId = command.observatories!!.single(),
            organizationId = null,
            extractionMethod = command.extractionMethod,
            visibility = command.visibility,
        )

        every { unsafeResourceUseCases.update(resourceUpdateCommand) } just runs

        val result = comparisonResourceUpdater(command, state)

        result.asClue {
            it.comparison shouldBe state.comparison
        }

        verify(exactly = 1) { unsafeResourceUseCases.update(resourceUpdateCommand) }
    }
}
