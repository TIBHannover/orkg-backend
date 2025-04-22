package org.orkg.contenttypes.domain.actions.tables

import io.kotest.assertions.asClue
import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.just
import io.mockk.mockk
import io.mockk.runs
import io.mockk.verify
import org.junit.jupiter.api.Test
import org.orkg.common.Either
import org.orkg.common.ThingId
import org.orkg.common.testing.fixtures.MockkBaseTest
import org.orkg.contenttypes.domain.actions.UpdateTableState
import org.orkg.contenttypes.domain.testing.fixtures.createRowGraph
import org.orkg.contenttypes.input.CreateThingCommandPart
import org.orkg.contenttypes.input.RowCommand
import org.orkg.contenttypes.input.testing.fixtures.updateTableCommand
import org.orkg.graph.domain.Predicates
import org.orkg.graph.domain.StatementId
import org.orkg.graph.domain.Thing
import org.orkg.graph.input.CreateStatementUseCase
import org.orkg.graph.input.UnsafeResourceUseCases
import org.orkg.graph.input.UnsafeStatementUseCases
import org.orkg.graph.testing.fixtures.createResource

internal class TableCellsUpdaterUnitTest : MockkBaseTest {
    private val unsafeResourceUseCases: UnsafeResourceUseCases = mockk()
    private val unsafeStatementUseCases: UnsafeStatementUseCases = mockk()
    private val abstractTableCellCreator: AbstractTableCellCreator = mockk()

    private val tableCellsUpdater = TableCellsUpdater(unsafeResourceUseCases, unsafeStatementUseCases, abstractTableCellCreator)

    @Test
    fun `Given a table update command, when updating table cells and cells are identical, it does nothing`() {
        val command = updateTableCommand().copy(
            rows = listOf(
                RowCommand(
                    label = "header",
                    data = listOf("Column_1_Title", "Column_2_Title", "Column_3_Title")
                ),
                RowCommand(
                    label = "Row Row_1 Label",
                    data = listOf("R123", "R456", "R789")
                )
            )
        )
        val existingRows = listOf(
            createRowGraph(
                ThingId("Row_1"),
                listOf(
                    createResource(ThingId("R123")),
                    createResource(ThingId("R456")),
                    createResource(ThingId("R789")),
                )
            ),
        )
        val columns = listOf(
            ThingId("Column_1"),
            ThingId("Column_2"),
            ThingId("Column_3"),
        )
        val rows = listOf(
            ThingId("Row_1")
        )
        val state = UpdateTableState().copy(
            columns = columns,
            rows = rows,
            existingRows = existingRows,
            thingsToDelete = setOf(ThingId("ToBeDeleted")),
            statementsToDelete = setOf(StatementId("SalreadyExists")),
            validationCache = mapOf(
                "ToBeDeleted" to Either.right(createResource(ThingId("ToBeDeleted")))
            )
        )

        every { unsafeResourceUseCases.delete(any(), command.contributorId) } just runs
        every { unsafeStatementUseCases.deleteAllById(state.statementsToDelete) } just runs

        val result = tableCellsUpdater(command, state)

        result.asClue {
            it.table shouldBe state.table
            it.statements shouldBe state.statements
            it.validationCache shouldBe state.validationCache
            it.tempIdToThingId shouldBe state.tempIdToThingId
            it.headerIndices shouldBe state.headerIndices
            it.columns shouldBe state.columns
            it.rows shouldBe state.rows
            it.existingColumns shouldBe state.existingColumns
            it.existingRows shouldBe state.existingRows
            it.thingsToDelete shouldBe state.thingsToDelete
            it.statementsToDelete shouldBe state.statementsToDelete
        }

        verify(exactly = 1) { unsafeResourceUseCases.delete(any(), command.contributorId) }
        verify(exactly = 1) { unsafeStatementUseCases.deleteAllById(state.statementsToDelete) }
    }

    @Test
    fun `Given a table update command, when updating table cells and cells value has changed, it links the new value`() {
        val command = updateTableCommand().copy(
            rows = listOf(
                RowCommand(
                    label = "header",
                    data = listOf("Column_1_Title", "Column_2_Title", "Column_3_Title")
                ),
                RowCommand(
                    label = "Row Row_1 Label",
                    data = listOf("R123", "R456", "RNEW")
                )
            )
        )
        val existingRows = listOf(
            createRowGraph(
                ThingId("Row_1"),
                listOf(
                    createResource(ThingId("R123")),
                    createResource(ThingId("R456")),
                    createResource(ThingId("R789")),
                )
            ),
        )
        val columns = listOf(
            ThingId("Column_1"),
            ThingId("Column_2"),
            ThingId("Column_3"),
        )
        val rows = listOf(
            ThingId("Row_1")
        )
        val validationCache: Map<String, Either<CreateThingCommandPart, Thing>> = mapOf(
            "RNEW" to Either.right(createResource(ThingId("RNEW"))),
            "ToBeDeleted" to Either.right(createResource(ThingId("ToBeDeleted")))
        )
        val state = UpdateTableState().copy(
            validationCache = validationCache,
            columns = columns,
            rows = rows,
            existingRows = existingRows,
            thingsToDelete = setOf(ThingId("ToBeDeleted")),
            statementsToDelete = setOf(StatementId("SalreadyExists"))
        )
        val expectedStatementsToDelete = state.statementsToDelete + setOf(
            StatementId("S_Cell_Row_1_2--CSVW_Value--R789")
        )

        every {
            unsafeStatementUseCases.create(
                CreateStatementUseCase.CreateCommand(
                    contributorId = command.contributorId,
                    subjectId = ThingId("Cell_Row_1_2"),
                    predicateId = Predicates.csvwValue,
                    objectId = ThingId("RNEW")
                )
            )
        } returns StatementId("S1")
        every { unsafeResourceUseCases.delete(any(), command.contributorId) } just runs
        every { unsafeStatementUseCases.deleteAllById(expectedStatementsToDelete) } just runs

        val result = tableCellsUpdater(command, state)

        result.asClue {
            it.table shouldBe state.table
            it.statements shouldBe state.statements
            it.validationCache shouldBe state.validationCache
            it.tempIdToThingId shouldBe state.tempIdToThingId
            it.headerIndices shouldBe state.headerIndices
            it.columns shouldBe state.columns
            it.rows shouldBe state.rows
            it.existingColumns shouldBe state.existingColumns
            it.existingRows shouldBe state.existingRows
            it.thingsToDelete shouldBe state.thingsToDelete
            it.statementsToDelete shouldBe expectedStatementsToDelete
        }

        verify(exactly = 1) {
            unsafeStatementUseCases.create(
                CreateStatementUseCase.CreateCommand(
                    contributorId = command.contributorId,
                    subjectId = ThingId("Cell_Row_1_2"),
                    predicateId = Predicates.csvwValue,
                    objectId = ThingId("RNEW")
                )
            )
        }
        verify(exactly = 1) { unsafeResourceUseCases.delete(any(), command.contributorId) }
        verify(exactly = 1) { unsafeStatementUseCases.deleteAllById(expectedStatementsToDelete) }
    }

    @Test
    fun `Given a table update command, when updating table cells and cells was previously missing, it creates a new cell`() {
        val command = updateTableCommand().copy(
            rows = listOf(
                RowCommand(
                    label = "header",
                    data = listOf("Column_1_Title", "Column_2_Title", "Column_3_Title")
                ),
                RowCommand(
                    label = "Row Row_1 Label",
                    data = listOf("R123", "R456", "RNEW")
                )
            )
        )
        val existingRows = listOf(
            createRowGraph(
                ThingId("Row_1"),
                listOf(
                    createResource(ThingId("R123")),
                    createResource(ThingId("R456")),
                    null,
                )
            ).let { rowGraph ->
                rowGraph.copy(
                    cells = rowGraph.cells.toMutableList()
                        .also { it[it.lastIndex] = null }
                )
            }
        )
        val columns = listOf(
            ThingId("Column_1"),
            ThingId("Column_2"),
            ThingId("Column_3"),
        )
        val rows = listOf(
            ThingId("Row_1")
        )
        val validationCache: Map<String, Either<CreateThingCommandPart, Thing>> = mapOf(
            "RNEW" to Either.right(createResource(ThingId("RNEW"))),
            "ToBeDeleted" to Either.right(createResource(ThingId("ToBeDeleted")))
        )
        val state = UpdateTableState().copy(
            validationCache = validationCache,
            columns = columns,
            rows = rows,
            existingRows = existingRows,
            thingsToDelete = setOf(ThingId("ToBeDeleted")),
            statementsToDelete = setOf(StatementId("SalreadyExists"))
        )

        every {
            abstractTableCellCreator.create(
                contributorId = command.contributorId,
                rowId = ThingId("Row_1"),
                columnId = ThingId("Column_3"),
                value = ThingId("RNEW")
            )
        } returns ThingId("NewCellId")
        every { unsafeResourceUseCases.delete(any(), command.contributorId) } just runs
        every { unsafeStatementUseCases.deleteAllById(state.statementsToDelete) } just runs

        val result = tableCellsUpdater(command, state)

        result.asClue {
            it.table shouldBe state.table
            it.statements shouldBe state.statements
            it.validationCache shouldBe state.validationCache
            it.tempIdToThingId shouldBe state.tempIdToThingId
            it.headerIndices shouldBe state.headerIndices
            it.columns shouldBe state.columns
            it.rows shouldBe state.rows
            it.existingColumns shouldBe state.existingColumns
            it.existingRows shouldBe state.existingRows
            it.thingsToDelete shouldBe state.thingsToDelete
            it.statementsToDelete shouldBe state.statementsToDelete
        }

        verify(exactly = 1) {
            abstractTableCellCreator.create(
                contributorId = command.contributorId,
                rowId = ThingId("Row_1"),
                columnId = ThingId("Column_3"),
                value = ThingId("RNEW")
            )
        }
        verify(exactly = 1) { unsafeResourceUseCases.delete(any(), command.contributorId) }
        verify(exactly = 1) { unsafeStatementUseCases.deleteAllById(state.statementsToDelete) }
    }

    @Test
    fun `Given a table update command, when updating table cells and a new row got added, it creates a new cell for each column of the new row`() {
        val command = updateTableCommand().copy(
            rows = listOf(
                RowCommand(
                    label = "header",
                    data = listOf("Column_1_Title", "Column_2_Title", "Column_3_Title")
                ),
                RowCommand(
                    label = "Row Row_1 Label",
                    data = listOf("R123", "R456", "R789")
                ),
                RowCommand(
                    label = "Row Row_2 Label",
                    data = listOf("R321", "R654", "R987")
                )
            )
        )
        val existingRows = listOf(
            createRowGraph(
                ThingId("Row_1"),
                listOf(
                    createResource(ThingId("R123")),
                    createResource(ThingId("R456")),
                    createResource(ThingId("R789")),
                )
            ),
        )
        val columns = listOf(
            ThingId("Column_1"),
            ThingId("Column_2"),
            ThingId("Column_3"),
        )
        val rows = listOf(
            ThingId("Row_1"),
            ThingId("Row_2"),
        )
        val validationCache: Map<String, Either<CreateThingCommandPart, Thing>> = mapOf(
            "R321" to Either.right(createResource(ThingId("R321"))),
            "R654" to Either.right(createResource(ThingId("R654"))),
            "R987" to Either.right(createResource(ThingId("R987"))),
            "ToBeDeleted" to Either.right(createResource(ThingId("ToBeDeleted")))
        )
        val state = UpdateTableState().copy(
            validationCache = validationCache,
            columns = columns,
            rows = rows,
            existingRows = existingRows,
            thingsToDelete = setOf(ThingId("ToBeDeleted")),
            statementsToDelete = setOf(StatementId("SalreadyExists"))
        )

        every {
            abstractTableCellCreator.create(
                contributorId = command.contributorId,
                rowId = ThingId("Row_2"),
                columnId = any(),
                value = any()
            )
        } returnsMany listOf(ThingId("NewCellId1"), ThingId("NewCellId2"), ThingId("NewCellId3"))
        every { unsafeResourceUseCases.delete(any(), command.contributorId) } just runs
        every { unsafeStatementUseCases.deleteAllById(state.statementsToDelete) } just runs

        val result = tableCellsUpdater(command, state)

        result.asClue {
            it.table shouldBe state.table
            it.statements shouldBe state.statements
            it.validationCache shouldBe state.validationCache
            it.tempIdToThingId shouldBe state.tempIdToThingId
            it.headerIndices shouldBe state.headerIndices
            it.columns shouldBe state.columns
            it.rows shouldBe state.rows
            it.existingColumns shouldBe state.existingColumns
            it.existingRows shouldBe state.existingRows
            it.thingsToDelete shouldBe state.thingsToDelete
            it.statementsToDelete shouldBe state.statementsToDelete
        }

        verify(exactly = 1) {
            abstractTableCellCreator.create(
                contributorId = command.contributorId,
                rowId = ThingId("Row_2"),
                columnId = ThingId("Column_1"),
                value = ThingId("R321")
            )
        }
        verify(exactly = 1) {
            abstractTableCellCreator.create(
                contributorId = command.contributorId,
                rowId = ThingId("Row_2"),
                columnId = ThingId("Column_2"),
                value = ThingId("R654")
            )
        }
        verify(exactly = 1) {
            abstractTableCellCreator.create(
                contributorId = command.contributorId,
                rowId = ThingId("Row_2"),
                columnId = ThingId("Column_3"),
                value = ThingId("R987")
            )
        }
        verify(exactly = 1) { unsafeResourceUseCases.delete(any(), command.contributorId) }
        verify(exactly = 1) { unsafeStatementUseCases.deleteAllById(state.statementsToDelete) }
    }

    @Test
    fun `Given a table update command, when updating table cells and a new column got added, it creates a new cell for the column in each already existing row`() {
        val command = updateTableCommand().copy(
            rows = listOf(
                RowCommand(
                    label = "header",
                    data = listOf("Column_1_Title", "Column_2_Title", "Column_3_Title")
                ),
                RowCommand(
                    label = "Row Row_1 Label",
                    data = listOf("R123", "R456", "R789")
                ),
            )
        )
        val existingRows = listOf(
            createRowGraph(
                ThingId("Row_1"),
                listOf(
                    createResource(ThingId("R123")),
                    createResource(ThingId("R456"))
                )
            )
        )
        val columns = listOf(
            ThingId("Column_1"),
            ThingId("Column_2"),
            ThingId("Column_3"),
        )
        val rows = listOf(
            ThingId("Row_1"),
        )
        val validationCache: Map<String, Either<CreateThingCommandPart, Thing>> = mapOf(
            "R789" to Either.right(createResource(ThingId("R789"))),
            "ToBeDeleted" to Either.right(createResource(ThingId("ToBeDeleted")))
        )
        val state = UpdateTableState().copy(
            validationCache = validationCache,
            columns = columns,
            rows = rows,
            existingRows = existingRows,
            thingsToDelete = setOf(ThingId("ToBeDeleted")),
            statementsToDelete = setOf(StatementId("SalreadyExists"))
        )

        every {
            abstractTableCellCreator.create(
                contributorId = command.contributorId,
                rowId = ThingId("Row_1"),
                columnId = ThingId("Column_3"),
                value = ThingId("R789")
            )
        } returns ThingId("NewCellId")
        every { unsafeResourceUseCases.delete(any(), command.contributorId) } just runs
        every { unsafeStatementUseCases.deleteAllById(state.statementsToDelete) } just runs

        val result = tableCellsUpdater(command, state)

        result.asClue {
            it.table shouldBe state.table
            it.statements shouldBe state.statements
            it.validationCache shouldBe state.validationCache
            it.tempIdToThingId shouldBe state.tempIdToThingId
            it.headerIndices shouldBe state.headerIndices
            it.columns shouldBe state.columns
            it.rows shouldBe state.rows
            it.existingColumns shouldBe state.existingColumns
            it.existingRows shouldBe state.existingRows
            it.thingsToDelete shouldBe state.thingsToDelete
            it.statementsToDelete shouldBe state.statementsToDelete
        }

        verify(exactly = 1) {
            abstractTableCellCreator.create(
                contributorId = command.contributorId,
                rowId = ThingId("Row_1"),
                columnId = ThingId("Column_3"),
                value = ThingId("R789")
            )
        }
        verify(exactly = 1) { unsafeResourceUseCases.delete(any(), command.contributorId) }
        verify(exactly = 1) { unsafeStatementUseCases.deleteAllById(state.statementsToDelete) }
    }

    @Test
    fun `Given a table update command, when updating table cells and a column gets deleted, it deletes all cell subgraphs of that column`() {
        val command = updateTableCommand().copy(
            rows = listOf(
                RowCommand(
                    label = "header",
                    data = listOf("Column_1_Title", "Column_2_Title")
                ),
                RowCommand(
                    label = "Row Row_1 Label",
                    data = listOf("R123", "R456")
                ),
            )
        )
        val existingRows = listOf(
            createRowGraph(
                ThingId("Row_1"),
                listOf(
                    createResource(ThingId("R123")),
                    createResource(ThingId("R456")),
                    createResource(ThingId("R789"))
                )
            ),
        )
        val columns = listOf(
            ThingId("Column_1"),
            ThingId("Column_2"),
        )
        val rows = listOf(
            ThingId("Row_1"),
        )
        val state = UpdateTableState().copy(
            columns = columns,
            rows = rows,
            existingRows = existingRows,
            thingsToDelete = setOf(ThingId("ToBeDeleted")),
            statementsToDelete = setOf(StatementId("SalreadyExists")),
            validationCache = mapOf(
                "ToBeDeleted" to Either.right(createResource(ThingId("ToBeDeleted"))),
                "Cell_Row_1_2" to Either.right(createResource(ThingId("Cell_Row_1_2"))),
            )
        )
        val expectedThingsToDelete = state.thingsToDelete + ThingId("Cell_Row_1_2")
        val expectedStatementsToDelete = state.statementsToDelete + existingRows.first().let { it.cells.last()!!.statementIds }

        expectedThingsToDelete.forEach {
            every { unsafeResourceUseCases.delete(it, command.contributorId) } just runs
        }
        every { unsafeStatementUseCases.deleteAllById(expectedStatementsToDelete) } just runs

        val result = tableCellsUpdater(command, state)

        result.asClue {
            it.table shouldBe state.table
            it.statements shouldBe state.statements
            it.validationCache shouldBe state.validationCache
            it.tempIdToThingId shouldBe state.tempIdToThingId
            it.headerIndices shouldBe state.headerIndices
            it.columns shouldBe state.columns
            it.rows shouldBe state.rows
            it.existingColumns shouldBe state.existingColumns
            it.existingRows shouldBe state.existingRows
            it.thingsToDelete shouldBe expectedThingsToDelete
            it.statementsToDelete shouldBe expectedStatementsToDelete
        }

        expectedThingsToDelete.forEach {
            verify(exactly = 1) { unsafeResourceUseCases.delete(it, command.contributorId) }
        }
        verify(exactly = 1) { unsafeStatementUseCases.deleteAllById(expectedStatementsToDelete) }
    }

    @Test
    fun `Given a table update command, when updating table cells and a row gets deleted, it deletes all cell subgraphs of that row`() {
        val command = updateTableCommand().copy(
            rows = listOf(
                RowCommand(
                    label = "header",
                    data = listOf("Column_1_Title", "Column_2_Title", "Column_3_Title")
                ),
                RowCommand(
                    label = "Row Row_1 Label",
                    data = listOf("R123", "R456", "R789")
                ),
            )
        )
        val existingRows = listOf(
            createRowGraph(
                ThingId("Row_1"),
                listOf(
                    createResource(ThingId("R123")),
                    createResource(ThingId("R456")),
                    createResource(ThingId("R789"))
                )
            ),
            createRowGraph(
                ThingId("Row_2"),
                listOf(
                    createResource(ThingId("R321")),
                    createResource(ThingId("R654")),
                    createResource(ThingId("R987"))
                )
            ),
        )
        val columns = listOf(
            ThingId("Column_1"),
            ThingId("Column_2"),
            ThingId("Column_3"),
        )
        val rows = listOf(
            ThingId("Row_1"),
        )
        val state = UpdateTableState().copy(
            columns = columns,
            rows = rows,
            existingRows = existingRows,
            thingsToDelete = setOf(ThingId("ToBeDeleted")),
            statementsToDelete = setOf(StatementId("SalreadyExists")),
            validationCache = mapOf(
                "ToBeDeleted" to Either.right(createResource(ThingId("ToBeDeleted"))),
                "Cell_Row_2_0" to Either.right(createResource(ThingId("Cell_Row_2_0"))),
                "Cell_Row_2_1" to Either.right(createResource(ThingId("Cell_Row_2_1"))),
                "Cell_Row_2_2" to Either.right(createResource(ThingId("Cell_Row_2_2"))),
            )
        )
        val expectedThingsToDelete = state.thingsToDelete + setOf(
            ThingId("Cell_Row_2_0"),
            ThingId("Cell_Row_2_1"),
            ThingId("Cell_Row_2_2")
        )
        val expectedStatementsToDelete = state.statementsToDelete + existingRows.last()
            .let { rowGraph -> rowGraph.cells.flatMap { it!!.statementIds } }

        expectedThingsToDelete.forEach {
            every { unsafeResourceUseCases.delete(it, command.contributorId) } just runs
        }
        every { unsafeStatementUseCases.deleteAllById(expectedStatementsToDelete) } just runs

        val result = tableCellsUpdater(command, state)

        result.asClue {
            it.table shouldBe state.table
            it.statements shouldBe state.statements
            it.validationCache shouldBe state.validationCache
            it.tempIdToThingId shouldBe state.tempIdToThingId
            it.headerIndices shouldBe state.headerIndices
            it.columns shouldBe state.columns
            it.rows shouldBe state.rows
            it.existingColumns shouldBe state.existingColumns
            it.existingRows shouldBe state.existingRows
            it.thingsToDelete shouldBe expectedThingsToDelete
            it.statementsToDelete shouldBe expectedStatementsToDelete
        }

        expectedThingsToDelete.forEach {
            verify(exactly = 1) { unsafeResourceUseCases.delete(it, command.contributorId) }
        }
        verify(exactly = 1) { unsafeStatementUseCases.deleteAllById(expectedStatementsToDelete) }
    }

    @Test
    fun `Given a table update command, when updating table cells but rows are null, it does nothing`() {
        val command = updateTableCommand().copy(rows = null)
        val state = UpdateTableState()

        tableCellsUpdater(command, state)
    }
}
