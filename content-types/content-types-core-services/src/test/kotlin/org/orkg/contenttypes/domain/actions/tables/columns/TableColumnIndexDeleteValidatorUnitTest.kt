package org.orkg.contenttypes.domain.actions.tables.columns

import io.kotest.assertions.throwables.shouldThrow
import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.Test
import org.orkg.contenttypes.domain.TableColumnNotFound
import org.orkg.contenttypes.domain.TooFewTableColumns
import org.orkg.contenttypes.domain.actions.DeleteTableColumnState
import org.orkg.contenttypes.domain.testing.fixtures.createTable
import org.orkg.contenttypes.input.testing.fixtures.deleteTableColumnCommand

internal class TableColumnIndexDeleteValidatorUnitTest {
    private val tableColumnIndexDeleteValidator = TableColumnIndexDeleteValidator()

    @Test
    fun `Given a table column delete command, when validating the column index, it returns success`() {
        val command = deleteTableColumnCommand()
        val state = DeleteTableColumnState(table = createTable())

        tableColumnIndexDeleteValidator(command, state) shouldBe state
    }

    @Test
    fun `Given a table column delete command, when validating the column index, and table only has one column, it throws an exception`() {
        val command = deleteTableColumnCommand()
        val table = createTable().let { table ->
            table.copy(
                rows = table.rows.map { row ->
                    row.copy(data = row.data.take(1))
                }
            )
        }
        val state = DeleteTableColumnState(table = table)

        shouldThrow<TooFewTableColumns> { tableColumnIndexDeleteValidator(command, state) }
    }

    @Test
    fun `Given a table column delete command, when validating the column index, and column index does not exist, it throws an exception`() {
        val command = deleteTableColumnCommand().copy(columnIndex = 10)
        val state = DeleteTableColumnState(table = createTable())

        shouldThrow<TableColumnNotFound> { tableColumnIndexDeleteValidator(command, state) }
    }
}
