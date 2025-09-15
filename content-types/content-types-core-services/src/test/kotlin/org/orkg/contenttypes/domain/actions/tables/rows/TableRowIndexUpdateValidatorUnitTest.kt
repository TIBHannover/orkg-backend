package org.orkg.contenttypes.domain.actions.tables.rows

import io.kotest.assertions.throwables.shouldThrow
import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.ValueSource
import org.orkg.contenttypes.domain.TableRowNotFound
import org.orkg.contenttypes.domain.actions.UpdateTableRowState
import org.orkg.contenttypes.domain.testing.fixtures.createTable
import org.orkg.contenttypes.input.testing.fixtures.updateTableRowCommand

internal class TableRowIndexUpdateValidatorUnitTest {
    private val tableRowIndexUpdateValidator = TableRowIndexUpdateValidator()

    @Test
    fun `Given a table row update command, when validating the row index, it returns success`() {
        val command = updateTableRowCommand()
        val state = UpdateTableRowState(table = createTable())

        tableRowIndexUpdateValidator(command, state) shouldBe state
    }

    @ParameterizedTest
    @ValueSource(ints = [-1, 10])
    fun `Given a table row update command, when validating the row index, and row index is out of bounds, it throws an exception`(index: Int) {
        val command = updateTableRowCommand().copy(rowIndex = index)
        val state = UpdateTableRowState(table = createTable())

        shouldThrow<TableRowNotFound> { tableRowIndexUpdateValidator(command, state) }
    }
}
