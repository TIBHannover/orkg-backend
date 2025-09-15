package org.orkg.contenttypes.domain.actions.tables.columns

import io.kotest.assertions.throwables.shouldThrow
import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.Test
import org.orkg.contenttypes.domain.MissingTableColumnValues
import org.orkg.contenttypes.domain.TooManyTableColumnValues
import org.orkg.contenttypes.domain.actions.CreateTableColumnState
import org.orkg.contenttypes.domain.testing.fixtures.createTable
import org.orkg.contenttypes.input.testing.fixtures.createTableColumnCommand

internal class TableColumnCreateValidatorUnitTest {
    private val tableColumnCreateValidator = TableColumnCreateValidator()

    @Test
    fun `Given a table column create command, when validating the column dimensions, it returns success`() {
        val command = createTableColumnCommand()
        val state = CreateTableColumnState(table = createTable())

        tableColumnCreateValidator(command, state) shouldBe state
    }

    @Test
    fun `Given a table column create command, when validating the column dimensions, and provided column is missing values, it throws an exception`() {
        val command = createTableColumnCommand().let { it.copy(column = it.column.drop(1)) }
        val state = CreateTableColumnState(table = createTable())

        shouldThrow<MissingTableColumnValues> { tableColumnCreateValidator(command, state) }
    }

    @Test
    fun `Given a table column create command, when validating the column dimensions, and provided column has too many values, it throws an exception`() {
        val command = createTableColumnCommand().let { it.copy(column = it.column + "R456") }
        val state = CreateTableColumnState(table = createTable())

        shouldThrow<TooManyTableColumnValues> { tableColumnCreateValidator(command, state) }
    }
}
