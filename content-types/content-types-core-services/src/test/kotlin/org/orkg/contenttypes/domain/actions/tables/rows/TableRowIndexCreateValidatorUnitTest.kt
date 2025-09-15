package org.orkg.contenttypes.domain.actions.tables.rows

import io.kotest.assertions.throwables.shouldThrow
import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.Test
import org.orkg.contenttypes.domain.InvalidTableRowIndex
import org.orkg.contenttypes.domain.actions.CreateTableRowState
import org.orkg.contenttypes.domain.testing.fixtures.createTable
import org.orkg.contenttypes.input.testing.fixtures.createTableRowCommand

internal class TableRowIndexCreateValidatorUnitTest {
    private val tableRowIndexCreateValidator = TableRowIndexCreateValidator()

    @Test
    fun `Given a table row create command, when validating the row index, it returns success`() {
        val command = createTableRowCommand()
        val state = CreateTableRowState(table = createTable())

        tableRowIndexCreateValidator(command, state) shouldBe state
    }

    @Test
    fun `Given a table row create command, when validating the row index, and row index is bigger than row count, it returns success`() {
        val command = createTableRowCommand().copy(rowIndex = 10)
        val state = CreateTableRowState(table = createTable())

        tableRowIndexCreateValidator(command, state) shouldBe state
    }

    @Test
    fun `Given a table row create command, when validating the row index, and row index is null, it returns success`() {
        val command = createTableRowCommand().copy(rowIndex = null)
        val state = CreateTableRowState(table = createTable())

        tableRowIndexCreateValidator(command, state) shouldBe state
    }

    @Test
    fun `Given a table row create command, when validating the row index, and row index is invalid, it throws an exception`() {
        val command = createTableRowCommand().copy(rowIndex = -1)
        val state = CreateTableRowState(table = createTable())

        shouldThrow<InvalidTableRowIndex> { tableRowIndexCreateValidator(command, state) }
    }
}
