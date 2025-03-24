package org.orkg.contenttypes.domain.actions.tables

import io.kotest.assertions.asClue
import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.junit.jupiter.api.Test
import org.orkg.common.Either
import org.orkg.common.ThingId
import org.orkg.common.testing.fixtures.MockkBaseTest
import org.orkg.contenttypes.domain.actions.CreateTableState
import org.orkg.contenttypes.input.testing.fixtures.createTableCommand
import org.orkg.contenttypes.input.testing.fixtures.from
import org.orkg.graph.testing.fixtures.createResource

internal class TableCellsCreatorUnitTest : MockkBaseTest {
    private val abstractTableCellCreator: AbstractTableCellCreator = mockk()

    private val tableCellsCreator = TableCellsCreator(abstractTableCellCreator)

    @Test
    fun `Given a table create command, when creating table columns, it returns success`() {
        val command = createTableCommand()
        val state = CreateTableState().copy(
            tableId = ThingId("TableId"),
            validationCache = mapOf(
                "R456" to Either.right(createResource(ThingId("R456"))),
                "#temp4" from command,
                "#temp5" from command,
                "#temp6" from command,
                "#temp7" from command
            ),
            tempIdToThing = mapOf(
                "#temp4" to ThingId("R1"),
                "#temp5" to ThingId("P2"),
                "#temp6" to ThingId("C3"),
                "#temp7" to ThingId("L4")
            ),
            columns = listOf(
                ThingId("Column1"),
                ThingId("Column2"),
                ThingId("Column3")
            ),
            rows = listOf(
                ThingId("Row1"),
                ThingId("Row2")
            )
        )

        every {
            abstractTableCellCreator.create(command.contributorId, any(), any(), any())
        } returns ThingId("irrelevant")

        val result = tableCellsCreator(command, state)

        result.asClue {
            it.tableId shouldBe state.tableId
            it.validationCache shouldBe state.validationCache
            it.tempIdToThing shouldBe state.tempIdToThing
            it.columns shouldBe state.columns
            it.rows shouldBe state.rows
        }

        verify(exactly = 1) {
            abstractTableCellCreator.create(
                contributorId = command.contributorId,
                rowId = ThingId("Row1"),
                columnId = ThingId("Column1"),
                value = ThingId("R456"),
            )
        }
        verify(exactly = 1) {
            abstractTableCellCreator.create(
                contributorId = command.contributorId,
                rowId = ThingId("Row1"),
                columnId = ThingId("Column2"),
                value = ThingId("R1"),
            )
        }
        verify(exactly = 1) {
            abstractTableCellCreator.create(
                contributorId = command.contributorId,
                rowId = ThingId("Row1"),
                columnId = ThingId("Column3"),
                value = ThingId("P2"),
            )
        }
        verify(exactly = 1) {
            abstractTableCellCreator.create(
                contributorId = command.contributorId,
                rowId = ThingId("Row2"),
                columnId = ThingId("Column1"),
                value = ThingId("C3"),
            )
        }
        verify(exactly = 1) {
            abstractTableCellCreator.create(
                contributorId = command.contributorId,
                rowId = ThingId("Row2"),
                columnId = ThingId("Column2"),
                value = null,
            )
        }
        verify(exactly = 1) {
            abstractTableCellCreator.create(
                contributorId = command.contributorId,
                rowId = ThingId("Row2"),
                columnId = ThingId("Column3"),
                value = ThingId("L4"),
            )
        }
    }
}
