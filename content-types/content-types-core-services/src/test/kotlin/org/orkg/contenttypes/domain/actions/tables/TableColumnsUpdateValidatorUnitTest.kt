package org.orkg.contenttypes.domain.actions.tables

import io.kotest.assertions.asClue
import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.junit.jupiter.api.Test
import org.orkg.common.Either
import org.orkg.common.ThingId
import org.orkg.common.testing.fixtures.MockkBaseTest
import org.orkg.contenttypes.domain.actions.UpdateTableState
import org.orkg.contenttypes.domain.testing.fixtures.createTable
import org.orkg.contenttypes.domain.testing.fixtures.createTableStatements
import org.orkg.contenttypes.input.CreateThingCommandPart
import org.orkg.contenttypes.input.testing.fixtures.updateTableCommand
import org.orkg.graph.domain.Thing
import org.orkg.graph.testing.fixtures.createResource

internal class TableColumnsUpdateValidatorUnitTest : MockkBaseTest {
    private val abstractTableColumnsValidator: AbstractTableColumnsValidator = mockk()

    private val tableColumnsUpdateValidator = TableColumnsUpdateValidator(abstractTableColumnsValidator)

    @Test
    fun `Given a table update command, when validating its columns, it returns success`() {
        val command = updateTableCommand()
        val columnIds = listOf(
            ThingId("Column_1"),
            ThingId("Column_2"),
            ThingId("Column_3")
        )
        val state = UpdateTableState(
            table = createTable(),
            statements = createTableStatements(
                tableId = command.tableId,
                rowCount = 2,
                columnCount = 3,
            ).groupBy { it.subject.id }
        )

        val validationCache = mapOf<String, Either<CreateThingCommandPart, Thing>>(
            "R100" to Either.right(createResource())
        )

        every {
            abstractTableColumnsValidator.validate(
                rows = command.rows!!,
                thingCommands = command.all(),
                validationCacheIn = state.validationCache
            )
        } returns validationCache

        val result = tableColumnsUpdateValidator(command, state)

        result.asClue {
            it.table shouldBe state.table
            it.statements shouldBe state.statements
            it.validationCache shouldBe validationCache
            it.tempIdToThingId shouldBe state.tempIdToThingId
            it.headerIndices shouldBe listOf(1, 2, 3)
            it.columns shouldBe state.columns
            it.rows shouldBe state.rows
            it.existingColumns.map { it.columnId } shouldBe columnIds
            it.existingRows shouldBe state.existingRows
            it.thingsToDelete shouldBe state.thingsToDelete
            it.statementsToDelete shouldBe state.statementsToDelete
        }

        verify(exactly = 1) {
            abstractTableColumnsValidator.validate(
                rows = command.rows!!,
                thingCommands = command.all(),
                validationCacheIn = state.validationCache
            )
        }
    }

    @Test
    fun `Given a table update command, when rows are empty, it does nothing`() {
        val command = updateTableCommand().copy(rows = null)
        val state = UpdateTableState(table = createTable())

        val result = tableColumnsUpdateValidator(command, state)

        result.asClue {
            it.table shouldBe state.table
            it.statements shouldBe state.statements
            it.validationCache shouldBe state.validationCache
            it.tempIdToThingId shouldBe state.tempIdToThingId
            it.columns shouldBe state.columns
            it.rows shouldBe state.rows
            it.existingColumns shouldBe state.existingColumns
            it.existingRows shouldBe state.existingRows
            it.thingsToDelete shouldBe state.thingsToDelete
            it.statementsToDelete shouldBe state.statementsToDelete
        }
    }
}
