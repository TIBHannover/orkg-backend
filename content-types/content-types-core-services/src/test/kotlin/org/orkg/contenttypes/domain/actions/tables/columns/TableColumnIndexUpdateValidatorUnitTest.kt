package org.orkg.contenttypes.domain.actions.tables.columns

import io.kotest.assertions.throwables.shouldThrow
import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.ValueSource
import org.orkg.contenttypes.domain.TableColumnNotFound
import org.orkg.contenttypes.domain.actions.UpdateTableColumnState
import org.orkg.contenttypes.domain.testing.fixtures.createTable
import org.orkg.contenttypes.input.testing.fixtures.updateTableColumnCommand

internal class TableColumnIndexUpdateValidatorUnitTest {
    private val tableColumnIndexUpdateValidator = TableColumnIndexUpdateValidator()

    @Test
    fun `Given a table column update command, when validating the column index, it returns success`() {
        val command = updateTableColumnCommand()
        val state = UpdateTableColumnState(table = createTable())

        tableColumnIndexUpdateValidator(command, state) shouldBe state
    }

    @ParameterizedTest
    @ValueSource(ints = [-1, 10])
    fun `Given a table column update command, when validating the column index, and column index is out of bounds, it throws an exception`(index: Int) {
        val command = updateTableColumnCommand().copy(columnIndex = index)
        val state = UpdateTableColumnState(table = createTable())

        shouldThrow<TableColumnNotFound> { tableColumnIndexUpdateValidator(command, state) }
    }
}
