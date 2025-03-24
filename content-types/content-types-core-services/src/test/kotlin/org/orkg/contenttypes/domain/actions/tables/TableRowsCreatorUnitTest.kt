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

internal class TableRowsCreatorUnitTest : MockkBaseTest {
    private val abstractTableRowCreator: AbstractTableRowCreator = mockk()

    private val tableRowsCreator = TableRowsCreator(abstractTableRowCreator)

    @Test
    fun `Given a table create command, when creating table rows, it returns success`() {
        val command = createTableCommand()
        val state = CreateTableState().copy(
            tableId = ThingId("TableId")
        )
        val rows = listOf(
            ThingId("Row1"),
            ThingId("Row2")
        )

        every { abstractTableRowCreator.create(any(), any(), any(), any()) } returnsMany rows

        val result = tableRowsCreator(command, state)

        result.asClue {
            it.tableId shouldBe state.tableId
            it.validationCache shouldBe state.validationCache
            it.tempIdToThing shouldBe state.tempIdToThing
            it.columns shouldBe state.columns
            it.rows shouldBe rows
        }

        verify(exactly = 1) {
            abstractTableRowCreator.create(
                contributorId = command.contributorId,
                tableId = state.tableId!!,
                index = 0,
                label = null
            )
        }
        verify(exactly = 1) {
            abstractTableRowCreator.create(
                contributorId = command.contributorId,
                tableId = state.tableId!!,
                index = 1,
                label = "row 2"
            )
        }
    }
}
