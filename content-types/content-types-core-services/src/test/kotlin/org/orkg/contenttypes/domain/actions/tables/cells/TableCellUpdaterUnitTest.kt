package org.orkg.contenttypes.domain.actions.tables.cells

import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.just
import io.mockk.mockk
import io.mockk.runs
import io.mockk.verify
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.NullSource
import org.junit.jupiter.params.provider.ValueSource
import org.orkg.common.ThingId
import org.orkg.common.testing.fixtures.MockkBaseTest
import org.orkg.contenttypes.domain.actions.SingleStatementPropertyUpdater
import org.orkg.contenttypes.domain.actions.UpdateTableCellState
import org.orkg.contenttypes.domain.actions.tables.AbstractTableCellCreator
import org.orkg.contenttypes.domain.testing.fixtures.createTable
import org.orkg.contenttypes.domain.testing.fixtures.createTableStatements
import org.orkg.contenttypes.input.testing.fixtures.updateTableCellCommand
import org.orkg.graph.domain.Predicates

internal class TableCellUpdaterUnitTest : MockkBaseTest {
    private val abstractTableCellCreator: AbstractTableCellCreator = mockk()
    private val singleStatementPropertyUpdater: SingleStatementPropertyUpdater = mockk()
    private val tableCellUpdater = TableCellUpdater(abstractTableCellCreator, singleStatementPropertyUpdater)

    @ParameterizedTest
    @NullSource
    @ValueSource(strings = ["R123"])
    fun `Given a table cell update command, when updating a cell value, and cell graph does not exist, it creates a new cell graph`(id: String?) {
        val command = updateTableCellCommand().copy(id = id?.let(::ThingId))
        val statements = createTableStatements(
            tableId = command.tableId,
            rowCount = 2,
            columnCount = 2
        ).filter {
            it.subject.id != ThingId("Cell_1_2") && it.`object`.id != ThingId("Cell_1_2")
        }
        val state = UpdateTableCellState(
            table = createTable(),
            statements = statements.groupBy { it.subject.id }
        )

        every { abstractTableCellCreator.create(any(), any(), any(), any()) } returns ThingId("irrelevant")

        tableCellUpdater(command, state) shouldBe state

        verify(exactly = 1) {
            abstractTableCellCreator.create(
                contributorId = command.contributorId,
                rowId = ThingId("Row_1"),
                columnId = ThingId("Column_2"),
                value = command.id,
            )
        }
    }

    @ParameterizedTest
    @NullSource
    @ValueSource(strings = ["R123"])
    fun `Given a table cell update command, when updating a cell value, and cell graph exists, it updates the value statement`(id: String?) {
        val command = updateTableCellCommand().copy(id = id?.let(::ThingId))
        val statements = createTableStatements(
            tableId = command.tableId,
            rowCount = 2,
            columnCount = 2
        )
        val state = UpdateTableCellState(
            table = createTable(),
            statements = statements.groupBy { it.subject.id }
        )

        every { singleStatementPropertyUpdater.updateOptionalProperty(any(), any(), any(), any(), any<ThingId>()) } just runs

        tableCellUpdater(command, state) shouldBe state

        verify(exactly = 1) {
            singleStatementPropertyUpdater.updateOptionalProperty(
                statements = state.statements[ThingId("Cell_1_2")].orEmpty(),
                contributorId = command.contributorId,
                subjectId = ThingId("Cell_1_2"),
                predicateId = Predicates.csvwValue,
                objectId = command.id,
            )
        }
    }

    // Note: The provided id in the command is assumed to be non-null. The validation is not part of this class.
    @Test
    fun `Given a table cell update command, when updating a cell value, and value is part of the header, it updates the title statement`() {
        val command = updateTableCellCommand().copy(rowIndex = 0)
        val statements = createTableStatements(
            tableId = command.tableId,
            rowCount = 2,
            columnCount = 2
        )
        val state = UpdateTableCellState(
            table = createTable(),
            statements = statements.groupBy { it.subject.id }
        )

        every { singleStatementPropertyUpdater.updateRequiredProperty(any(), any(), any(), any(), any<ThingId>()) } just runs

        tableCellUpdater(command, state) shouldBe state

        verify(exactly = 1) {
            singleStatementPropertyUpdater.updateRequiredProperty(
                statements = state.statements[ThingId("Column_2")].orEmpty(),
                contributorId = command.contributorId,
                subjectId = ThingId("Column_2"),
                predicateId = Predicates.csvwTitles,
                objectId = command.id!!,
            )
        }
    }
}
