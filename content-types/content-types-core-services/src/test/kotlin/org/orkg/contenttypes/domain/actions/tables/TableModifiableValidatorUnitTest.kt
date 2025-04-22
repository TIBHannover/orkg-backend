package org.orkg.contenttypes.domain.actions.tables

import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.orkg.contenttypes.domain.TableNotModifiable
import org.orkg.contenttypes.domain.actions.UpdateTableState
import org.orkg.contenttypes.domain.testing.fixtures.createTable
import org.orkg.contenttypes.input.testing.fixtures.updateTableCommand

internal class TableModifiableValidatorUnitTest {
    private val tableModifiableValidator = TableModifiableValidator()

    @Test
    fun `Given a table update command, when table is modifiable, it returns success`() {
        val command = updateTableCommand()
        val state = UpdateTableState(table = createTable())

        tableModifiableValidator(command, state)
    }

    @Test
    fun `Given a table update command, when table is not modifiable, it throws an exception`() {
        val command = updateTableCommand()
        val state = UpdateTableState(table = createTable().copy(modifiable = false))

        assertThrows<TableNotModifiable> { tableModifiableValidator(command, state) }
    }
}
