package org.orkg.contenttypes.domain.actions.tables.cells

import io.kotest.assertions.throwables.shouldThrow
import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.ValueSource
import org.orkg.contenttypes.domain.TableColumnNotFound
import org.orkg.contenttypes.domain.TableRowNotFound
import org.orkg.contenttypes.domain.actions.UpdateTableCellState
import org.orkg.contenttypes.domain.testing.fixtures.createTable
import org.orkg.contenttypes.input.testing.fixtures.updateTableCellCommand

internal class TableCellIndexValidatorUnitTest {
    private val tableCellIndexValidator = TableCellIndexValidator()

    @Test
    fun `Given a table cell update command, when validating the column index, it returns success`() {
        val command = updateTableCellCommand()
        val state = UpdateTableCellState(table = createTable())

        tableCellIndexValidator(command, state) shouldBe state
    }

    @ParameterizedTest
    @ValueSource(ints = [-1, 10])
    fun `Given a table cell update command, when validating the cell position, and row index is out of bounds, it throws an exception`(index: Int) {
        val command = updateTableCellCommand().copy(rowIndex = index)
        val state = UpdateTableCellState(table = createTable())

        shouldThrow<TableRowNotFound> { tableCellIndexValidator(command, state) }
    }

    @ParameterizedTest
    @ValueSource(ints = [-1, 10])
    fun `Given a table cell update command, when validating the cell position, and column index is out of bounds, it throws an exception`(index: Int) {
        val command = updateTableCellCommand().copy(columnIndex = index)
        val state = UpdateTableCellState(table = createTable())

        shouldThrow<TableColumnNotFound> { tableCellIndexValidator(command, state) }
    }
}
