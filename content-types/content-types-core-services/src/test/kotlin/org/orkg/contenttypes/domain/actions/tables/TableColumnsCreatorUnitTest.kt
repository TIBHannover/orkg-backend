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
import org.orkg.contenttypes.input.testing.fixtures.from

internal class TableColumnsCreatorUnitTest : MockkBaseTest {
    private val abstractTableColumnCreator: AbstractTableColumnCreator = mockk()

    private val tableColumnsCreator = TableColumnsCreator(abstractTableColumnCreator)

    @Test
    fun `Given a table create command, when creating table columns, it returns success`() {
        val command = createTableCommand()
        val state = CreateTableState().copy(
            tableId = ThingId("TableId"),
            validationCache = mapOf(
                "#temp1" from command,
                "#temp2" from command,
                "#temp3" from command
            ),
            tempIdToThing = mapOf(
                "#temp1" to ThingId("L1"),
                "#temp2" to ThingId("L2"),
                "#temp3" to ThingId("L3")
            )
        )
        val columns = listOf(
            ThingId("Column1"),
            ThingId("Column2"),
            ThingId("Column3")
        )

        every { abstractTableColumnCreator.create(any(), any(), any(), any()) } returnsMany columns

        val result = tableColumnsCreator(command, state)

        result.asClue {
            it.tableId shouldBe state.tableId
            it.validationCache shouldBe state.validationCache
            it.tempIdToThing shouldBe state.tempIdToThing
            it.columns shouldBe columns
            it.rows shouldBe state.rows
        }

        verify(exactly = 1) {
            abstractTableColumnCreator.create(
                contributorId = command.contributorId,
                tableId = state.tableId!!,
                index = 0,
                titleLiteralId = ThingId("L1")
            )
        }
        verify(exactly = 1) {
            abstractTableColumnCreator.create(
                contributorId = command.contributorId,
                tableId = state.tableId!!,
                index = 1,
                titleLiteralId = ThingId("L2")
            )
        }
        verify(exactly = 1) {
            abstractTableColumnCreator.create(
                contributorId = command.contributorId,
                tableId = state.tableId!!,
                index = 2,
                titleLiteralId = ThingId("L3")
            )
        }
    }
}
