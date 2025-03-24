package org.orkg.contenttypes.domain.actions.tables

import io.kotest.assertions.asClue
import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.junit.jupiter.api.Test
import org.orkg.common.ThingId
import org.orkg.common.testing.fixtures.MockkBaseTest
import org.orkg.contenttypes.domain.actions.CreateTableState
import org.orkg.contenttypes.input.testing.fixtures.createTableCommand
import org.orkg.graph.domain.Classes
import org.orkg.graph.input.CreateResourceUseCase
import org.orkg.graph.input.UnsafeResourceUseCases

internal class TableResourceCreatorUnitTest : MockkBaseTest {
    private val unsafeResourceUseCases: UnsafeResourceUseCases = mockk()

    private val tableResourceCreator = TableResourceCreator(unsafeResourceUseCases)

    @Test
    fun `Given a table create command, it crates a new table resource`() {
        val command = createTableCommand()
        val state = CreateTableState()

        val resourceCreateCommand = CreateResourceUseCase.CreateCommand(
            label = command.label,
            classes = setOf(Classes.table),
            contributorId = command.contributorId,
            observatoryId = command.observatories.firstOrNull(),
            organizationId = command.organizations.firstOrNull(),
            extractionMethod = command.extractionMethod
        )
        val id = ThingId("Table")

        every { unsafeResourceUseCases.create(resourceCreateCommand) } returns id

        val result = tableResourceCreator(command, state)

        result.asClue {
            it.tableId shouldBe id
            it.validationCache shouldBe state.validationCache
            it.tempIdToThing shouldBe state.tempIdToThing
            it.columns shouldBe state.columns
            it.rows shouldBe state.rows
        }

        verify(exactly = 1) { unsafeResourceUseCases.create(resourceCreateCommand) }
    }
}
