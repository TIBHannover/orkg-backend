package org.orkg.contenttypes.domain.actions.tables.rows

import io.kotest.assertions.throwables.shouldThrow
import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.Test
import org.orkg.contenttypes.domain.CannotDeleteTableHeader
import org.orkg.contenttypes.domain.TableRowNotFound
import org.orkg.contenttypes.domain.TooFewTableRows
import org.orkg.contenttypes.domain.actions.DeleteTableRowState
import org.orkg.contenttypes.domain.testing.fixtures.createTable
import org.orkg.contenttypes.input.testing.fixtures.deleteTableRowCommand

internal class TableRowIndexDeleteValidatorUnitTest {
    private val tableRowIndexDeleteValidator = TableRowIndexDeleteValidator()

    @Test
    fun `Given a table row delete command, when validating the row index, it returns success`() {
        val command = deleteTableRowCommand()
        val state = DeleteTableRowState(table = createTable())

        tableRowIndexDeleteValidator(command, state) shouldBe state
    }

    @Test
    fun `Given a table row delete command, when validating the row index, and table only has one row, it throws an exception`() {
        val command = deleteTableRowCommand()
        val state = DeleteTableRowState(table = createTable().let { it.copy(rows = it.rows.take(1)) })

        shouldThrow<TooFewTableRows> { tableRowIndexDeleteValidator(command, state) }
    }

    @Test
    fun `Given a table row delete command, when validating the row index, and header row should be deleted, it throws an exception`() {
        val command = deleteTableRowCommand().copy(rowIndex = 0)
        val state = DeleteTableRowState(table = createTable())

        shouldThrow<CannotDeleteTableHeader> { tableRowIndexDeleteValidator(command, state) }
    }

    @Test
    fun `Given a table row delete command, when validating the row index, and row index does not exist, it throws an exception`() {
        val command = deleteTableRowCommand().copy(rowIndex = 10)
        val state = DeleteTableRowState(table = createTable())

        shouldThrow<TableRowNotFound> { tableRowIndexDeleteValidator(command, state) }
    }
}
