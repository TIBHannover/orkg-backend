package org.orkg.contenttypes.domain.actions.tables

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
import org.orkg.contenttypes.domain.actions.UpdateTableState
import org.orkg.contenttypes.input.testing.fixtures.updateTableCommand
import org.orkg.graph.input.UnsafeResourceUseCases
import org.orkg.graph.input.UpdateResourceUseCase

internal class TableResourceUpdaterUnitTest : MockkBaseTest {
    private val unsafeResourceUseCases: UnsafeResourceUseCases = mockk()

    private val tableResourceUpdater = TableResourceUpdater(unsafeResourceUseCases)

    @Test
    fun `Given a table update command, it updates the table resource`() {
        val command = updateTableCommand()
        val state = UpdateTableState()

        val resourceUpdateCommand = UpdateResourceUseCase.UpdateCommand(
            id = command.tableId,
            contributorId = command.contributorId,
            label = command.label,
            observatoryId = command.observatories!!.single(),
            organizationId = command.organizations!!.single(),
            extractionMethod = command.extractionMethod!!,
            visibility = command.visibility,
        )

        every { unsafeResourceUseCases.update(resourceUpdateCommand) } just runs

        tableResourceUpdater(command, state) shouldBe state

        verify(exactly = 1) { unsafeResourceUseCases.update(resourceUpdateCommand) }
    }

    @Test
    fun `Given a table update command, when observatories are empty, it removes the associated observatory`() {
        val command = updateTableCommand().copy(observatories = emptyList())
        val state = UpdateTableState()

        val resourceUpdateCommand = UpdateResourceUseCase.UpdateCommand(
            id = command.tableId,
            contributorId = command.contributorId,
            label = command.label,
            observatoryId = ObservatoryId.UNKNOWN,
            organizationId = command.organizations!!.single(),
            extractionMethod = command.extractionMethod,
            visibility = command.visibility,
        )

        every { unsafeResourceUseCases.update(resourceUpdateCommand) } just runs

        tableResourceUpdater(command, state) shouldBe state

        verify(exactly = 1) { unsafeResourceUseCases.update(resourceUpdateCommand) }
    }

    @Test
    fun `Given a table update command, when observatories are not set, it does not update the associated observatory`() {
        val command = updateTableCommand().copy(observatories = null)
        val state = UpdateTableState()

        val resourceUpdateCommand = UpdateResourceUseCase.UpdateCommand(
            id = command.tableId,
            contributorId = command.contributorId,
            label = command.label,
            observatoryId = null,
            organizationId = command.organizations!!.single(),
            extractionMethod = command.extractionMethod,
            visibility = command.visibility,
        )

        every { unsafeResourceUseCases.update(resourceUpdateCommand) } just runs

        tableResourceUpdater(command, state) shouldBe state

        verify(exactly = 1) { unsafeResourceUseCases.update(resourceUpdateCommand) }
    }

    @Test
    fun `Given a table update command, when organizations are empty, it removes the associated organizations`() {
        val command = updateTableCommand().copy(organizations = emptyList())
        val state = UpdateTableState()

        val resourceUpdateCommand = UpdateResourceUseCase.UpdateCommand(
            id = command.tableId,
            contributorId = command.contributorId,
            label = command.label,
            observatoryId = command.observatories!!.single(),
            organizationId = OrganizationId.UNKNOWN,
            extractionMethod = command.extractionMethod,
            visibility = command.visibility,
        )

        every { unsafeResourceUseCases.update(resourceUpdateCommand) } just runs

        tableResourceUpdater(command, state) shouldBe state

        verify(exactly = 1) { unsafeResourceUseCases.update(resourceUpdateCommand) }
    }

    @Test
    fun `Given a table update command, when organizations are not set, it does not update the associated organizations`() {
        val command = updateTableCommand().copy(organizations = null)
        val state = UpdateTableState()

        val resourceUpdateCommand = UpdateResourceUseCase.UpdateCommand(
            id = command.tableId,
            contributorId = command.contributorId,
            label = command.label,
            observatoryId = command.observatories!!.single(),
            organizationId = null,
            extractionMethod = command.extractionMethod,
            visibility = command.visibility,
        )

        every { unsafeResourceUseCases.update(resourceUpdateCommand) } just runs

        tableResourceUpdater(command, state) shouldBe state

        verify(exactly = 1) { unsafeResourceUseCases.update(resourceUpdateCommand) }
    }
}
