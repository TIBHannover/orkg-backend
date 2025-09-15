package org.orkg.contenttypes.domain.actions.tables.columns

import io.kotest.assertions.throwables.shouldThrow
import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.Test
import org.orkg.contenttypes.domain.InvalidTableColumnIndex
import org.orkg.contenttypes.domain.actions.CreateTableColumnState
import org.orkg.contenttypes.domain.testing.fixtures.createTable
import org.orkg.contenttypes.input.testing.fixtures.createTableColumnCommand

internal class TableColumnIndexCreateValidatorUnitTest {
    private val tableColumnIndexCreateValidator = TableColumnIndexCreateValidator()

    @Test
    fun `Given a table column create command, when validating the column index, it returns success`() {
        val command = createTableColumnCommand()
        val state = CreateTableColumnState(table = createTable())

        tableColumnIndexCreateValidator(command, state) shouldBe state
    }

    @Test
    fun `Given a table column create command, when validating the column index, and column index is bigger than column count, it returns success`() {
        val command = createTableColumnCommand().copy(columnIndex = 10)
        val state = CreateTableColumnState(table = createTable())

        tableColumnIndexCreateValidator(command, state) shouldBe state
    }

    @Test
    fun `Given a table column create command, when validating the column index, and column index is null, it returns success`() {
        val command = createTableColumnCommand().copy(columnIndex = null)
        val state = CreateTableColumnState(table = createTable())

        tableColumnIndexCreateValidator(command, state) shouldBe state
    }

    @Test
    fun `Given a table column create command, when validating the column index, and column index is invalid, it throws an exception`() {
        val command = createTableColumnCommand().copy(columnIndex = -1)
        val state = CreateTableColumnState(table = createTable())

        shouldThrow<InvalidTableColumnIndex> { tableColumnIndexCreateValidator(command, state) }
    }
}
