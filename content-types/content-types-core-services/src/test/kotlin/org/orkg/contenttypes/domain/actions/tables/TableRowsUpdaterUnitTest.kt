package org.orkg.contenttypes.domain.actions.tables

import io.kotest.assertions.asClue
import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.just
import io.mockk.mockk
import io.mockk.runs
import io.mockk.verify
import org.junit.jupiter.api.Test
import org.orkg.common.ThingId
import org.orkg.common.testing.fixtures.MockkBaseTest
import org.orkg.contenttypes.domain.actions.UpdateTableState
import org.orkg.contenttypes.domain.testing.fixtures.createRowGraph
import org.orkg.contenttypes.input.RowCommand
import org.orkg.contenttypes.input.testing.fixtures.updateTableCommand
import org.orkg.graph.domain.Predicates
import org.orkg.graph.domain.StatementId
import org.orkg.graph.input.CreateLiteralUseCase
import org.orkg.graph.input.CreateStatementUseCase
import org.orkg.graph.input.UnsafeLiteralUseCases
import org.orkg.graph.input.UnsafeStatementUseCases
import org.orkg.graph.input.UpdateLiteralUseCase
import org.orkg.graph.testing.fixtures.createResource

internal class TableRowsUpdaterUnitTest : MockkBaseTest {
    private val unsafeStatementUseCases: UnsafeStatementUseCases = mockk()
    private val unsafeLiteralUseCases: UnsafeLiteralUseCases = mockk()
    private val abstractTableRowCreator: AbstractTableRowCreator = mockk()

    private val tableRowsUpdater = TableRowsUpdater(unsafeStatementUseCases, unsafeLiteralUseCases, abstractTableRowCreator)

    @Test
    fun `Given a table update command, when updating table rows and rows are identical, it does nothing`() {
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
        val state = UpdateTableState().copy(
            existingRows = existingRows,
            statementsToDelete = setOf(StatementId("SalreadyExists"))
        )
        val expectedRowIds = listOf(ThingId("Row_1"))

        val result = tableRowsUpdater(command, state)

        result.asClue {
            it.table shouldBe state.table
            it.statements shouldBe state.statements
            it.validationCache shouldBe state.validationCache
            it.tempIdToThingId shouldBe state.tempIdToThingId
            it.headerIndices shouldBe state.headerIndices
            it.columns shouldBe state.columns
            it.rows shouldBe expectedRowIds
            it.existingColumns shouldBe state.existingColumns
            it.existingRows shouldBe state.existingRows
            it.thingsToDelete shouldBe state.thingsToDelete
            it.statementsToDelete shouldBe state.statementsToDelete
        }
    }

    @Test
    fun `Given a table update command, when updating table rows and row title should be deleted, it marks the row title statement for deletion`() {
        val command = updateTableCommand().copy(
            rows = listOf(
                RowCommand(
                    label = "header",
                    data = listOf("Column_1_Title", "Column_2_Title", "Column_3_Title")
                ),
                RowCommand(
                    label = null,
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
        val state = UpdateTableState().copy(
            existingRows = existingRows,
            statementsToDelete = setOf(StatementId("SalreadyExists"))
        )
        val expectedRowIds = listOf(ThingId("Row_1"))
        val expectedStatementsToDelete = state.statementsToDelete + StatementId("S_Row_1--CSVW_Titles--Row_1_Title")

        val result = tableRowsUpdater(command, state)

        result.asClue {
            it.table shouldBe state.table
            it.statements shouldBe state.statements
            it.validationCache shouldBe state.validationCache
            it.tempIdToThingId shouldBe state.tempIdToThingId
            it.headerIndices shouldBe state.headerIndices
            it.columns shouldBe state.columns
            it.rows shouldBe expectedRowIds
            it.existingColumns shouldBe state.existingColumns
            it.existingRows shouldBe state.existingRows
            it.thingsToDelete shouldBe state.thingsToDelete
            it.statementsToDelete shouldBe expectedStatementsToDelete
        }
    }

    @Test
    fun `Given a table update command, when updating table rows and row title was previously missing, it creates a new row title statement`() {
        val command = updateTableCommand().copy(
            rows = listOf(
                RowCommand(
                    label = "header",
                    data = listOf("Column_1_Title", "Column_2_Title", "Column_3_Title")
                ),
                RowCommand(
                    label = "New row title",
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
            ).copy(labelStatement = null),
        )
        val state = UpdateTableState().copy(
            existingRows = existingRows,
            statementsToDelete = setOf(StatementId("SalreadyExists"))
        )
        val expectedRowIds = listOf(ThingId("Row_1"))
        val rowTitleLiteralId = ThingId("Row_1_Title")
        val rowTitleStatementId = StatementId("S_Row1TitleStatementId")

        every { unsafeLiteralUseCases.create(any()) } returns rowTitleLiteralId
        every { unsafeStatementUseCases.create(any()) } returns rowTitleStatementId

        val result = tableRowsUpdater(command, state)

        result.asClue {
            it.table shouldBe state.table
            it.statements shouldBe state.statements
            it.validationCache shouldBe state.validationCache
            it.tempIdToThingId shouldBe state.tempIdToThingId
            it.headerIndices shouldBe state.headerIndices
            it.columns shouldBe state.columns
            it.rows shouldBe expectedRowIds
            it.existingColumns shouldBe state.existingColumns
            it.existingRows shouldBe state.existingRows
            it.thingsToDelete shouldBe state.thingsToDelete
            it.statementsToDelete shouldBe state.statementsToDelete
        }

        verify(exactly = 1) {
            unsafeLiteralUseCases.create(
                CreateLiteralUseCase.CreateCommand(
                    contributorId = command.contributorId,
                    label = "New row title"
                )
            )
        }
        verify(exactly = 1) {
            unsafeStatementUseCases.create(
                CreateStatementUseCase.CreateCommand(
                    contributorId = command.contributorId,
                    subjectId = ThingId("Row_1"),
                    predicateId = Predicates.csvwTitles,
                    objectId = rowTitleLiteralId
                )
            )
        }
    }

    @Test
    fun `Given a table update command, when updating table rows and row title has changed, it updates the row title literal`() {
        val command = updateTableCommand().copy(
            rows = listOf(
                RowCommand(
                    label = "header",
                    data = listOf("Column_1_Title", "Column_2_Title", "Column_3_Title")
                ),
                RowCommand(
                    label = "New row title",
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
            )
        )
        val state = UpdateTableState().copy(
            existingRows = existingRows,
            statementsToDelete = setOf(StatementId("SalreadyExists"))
        )
        val expectedRowIds = listOf(ThingId("Row_1"))

        every { unsafeLiteralUseCases.update(any()) } just runs

        val result = tableRowsUpdater(command, state)

        result.asClue {
            it.table shouldBe state.table
            it.statements shouldBe state.statements
            it.validationCache shouldBe state.validationCache
            it.tempIdToThingId shouldBe state.tempIdToThingId
            it.headerIndices shouldBe state.headerIndices
            it.columns shouldBe state.columns
            it.rows shouldBe expectedRowIds
            it.existingColumns shouldBe state.existingColumns
            it.existingRows shouldBe state.existingRows
            it.thingsToDelete shouldBe state.thingsToDelete
            it.statementsToDelete shouldBe state.statementsToDelete
        }

        verify(exactly = 1) {
            unsafeLiteralUseCases.update(
                UpdateLiteralUseCase.UpdateCommand(
                    id = ThingId("Row_1_Title"),
                    contributorId = command.contributorId,
                    label = "New row title",
                )
            )
        }
    }

    @Test
    fun `Given a table update command, when updating table rows, it creates missing table rows`() {
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
                    label = "New row2 label",
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
            )
        )
        val state = UpdateTableState().copy(
            existingRows = existingRows,
            statementsToDelete = setOf(StatementId("SalreadyExists"))
        )
        val expectedRowIds = listOf(ThingId("Row_1"), ThingId("Row_2"))

        every {
            abstractTableRowCreator.create(
                contributorId = command.contributorId,
                tableId = command.tableId,
                index = 1,
                label = "New row2 label"
            )
        } returns ThingId("Row_2")

        val result = tableRowsUpdater(command, state)

        result.asClue {
            it.table shouldBe state.table
            it.statements shouldBe state.statements
            it.validationCache shouldBe state.validationCache
            it.tempIdToThingId shouldBe state.tempIdToThingId
            it.headerIndices shouldBe state.headerIndices
            it.columns shouldBe state.columns
            it.rows shouldBe expectedRowIds
            it.existingColumns shouldBe state.existingColumns
            it.existingRows shouldBe state.existingRows
            it.thingsToDelete shouldBe state.thingsToDelete
            it.statementsToDelete shouldBe state.statementsToDelete
        }

        verify(exactly = 1) {
            abstractTableRowCreator.create(
                contributorId = command.contributorId,
                tableId = command.tableId,
                index = 1,
                label = "New row2 label"
            )
        }
    }

    @Test
    fun `Given a table update command, when updating table rows and rows get deleted, it returns success`() {
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
            createRowGraph(
                ThingId("Row_2"),
                listOf(
                    createResource(ThingId("R123")),
                    createResource(ThingId("R456")),
                    createResource(ThingId("R789")),
                )
            )
        )
        val state = UpdateTableState().copy(
            existingRows = existingRows,
            statementsToDelete = setOf(StatementId("SalreadyExists"))
        )
        val expectedRowIds = listOf(ThingId("Row_1"))
        val expectedThingsToDelete = state.thingsToDelete + existingRows.last().rowId
        val expectedStatementsToDelete = state.statementsToDelete + existingRows.last().statementIds

        val result = tableRowsUpdater(command, state)

        result.asClue {
            it.table shouldBe state.table
            it.statements shouldBe state.statements
            it.validationCache shouldBe state.validationCache
            it.tempIdToThingId shouldBe state.tempIdToThingId
            it.headerIndices shouldBe state.headerIndices
            it.columns shouldBe state.columns
            it.rows shouldBe expectedRowIds
            it.existingColumns shouldBe state.existingColumns
            it.existingRows shouldBe state.existingRows
            it.thingsToDelete shouldBe expectedThingsToDelete
            it.statementsToDelete shouldBe expectedStatementsToDelete
        }
    }

    @Test
    fun `Given a table update command, when updating table rows but rows are null, it does nothing`() {
        val command = updateTableCommand().copy(rows = null)
        val state = UpdateTableState()

        tableRowsUpdater(command, state)
    }
}
