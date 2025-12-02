package org.orkg.contenttypes.domain.actions.tables

import io.kotest.assertions.asClue
import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.junit.jupiter.api.Test
import org.orkg.common.ThingId
import org.orkg.common.testing.fixtures.MockkBaseTest
import org.orkg.contenttypes.domain.actions.UpdateTableState
import org.orkg.contenttypes.domain.testing.fixtures.createColumnGraph
import org.orkg.contenttypes.input.CreateRowCommand
import org.orkg.contenttypes.input.testing.fixtures.from
import org.orkg.contenttypes.input.testing.fixtures.updateTableCommand
import org.orkg.graph.domain.Predicates
import org.orkg.graph.domain.StatementId
import org.orkg.graph.input.CreateStatementUseCase
import org.orkg.graph.input.UnsafeStatementUseCases

internal class TableColumnsUpdaterUnitTest : MockkBaseTest {
    private val unsafeStatementUseCases: UnsafeStatementUseCases = mockk()
    private val abstractTableColumnCreator: AbstractTableColumnCreator = mockk()

    private val tableColumnsUpdater = TableColumnsUpdater(unsafeStatementUseCases, abstractTableColumnCreator)

    @Test
    fun `Given a table update command, when updating table columns and columns are identical, it does nothing`() {
        val command = updateTableCommand().copy(
            rows = listOf(
                CreateRowCommand(
                    label = "header",
                    data = listOf("Column_1_Title", "Column_2_Title", "Column_3_Title")
                )
            )
        )
        val existingColumnIds = listOf(
            ThingId("Column_1"),
            ThingId("Column_2"),
            ThingId("Column_3")
        )
        val state = UpdateTableState().copy(
            existingColumns = existingColumnIds.map(::createColumnGraph),
            thingsToDelete = setOf(ThingId("ToBeDeleted")),
            statementsToDelete = setOf(StatementId("SalreadyExists"))
        )

        val result = tableColumnsUpdater(command, state)

        result.asClue {
            it.table shouldBe state.table
            it.statements shouldBe state.statements
            it.validationCache shouldBe state.validationCache
            it.tempIdToThingId shouldBe state.tempIdToThingId
            it.headerIndices shouldBe state.headerIndices
            it.columns shouldBe existingColumnIds
            it.rows shouldBe state.rows
            it.existingColumns shouldBe state.existingColumns
            it.existingRows shouldBe state.existingRows
            it.thingsToDelete shouldBe state.thingsToDelete
            it.statementsToDelete shouldBe state.statementsToDelete
        }
    }

    @Test
    fun `Given a table update command, when updating table columns, it only updates the title of already existing columns`() {
        val command = updateTableCommand()
        val existingColumnIds = listOf(
            ThingId("Column_1"),
            ThingId("Column_2"),
            ThingId("Column_3")
        )
        val state = UpdateTableState().copy(
            existingColumns = existingColumnIds.map(::createColumnGraph),
            validationCache = mapOf(
                "#temp1" from command,
                "#temp2" from command,
                "#temp3" from command
            ),
            tempIdToThingId = mapOf(
                "#temp1" to ThingId("L1"),
                "#temp2" to ThingId("L2"),
                "#temp3" to ThingId("L3")
            ),
            thingsToDelete = setOf(ThingId("ToBeDeleted")),
            statementsToDelete = setOf(StatementId("SalreadyExists"))
        )
        val expecetedStatementsToDelete = state.statementsToDelete + setOf(
            StatementId("S_Column_1--CSVW_Titles--Column_1_Title"),
            StatementId("S_Column_2--CSVW_Titles--Column_2_Title"),
            StatementId("S_Column_3--CSVW_Titles--Column_3_Title")
        )
        val newColumnTitleStatements = listOf(
            StatementId("S1"),
            StatementId("S2"),
            StatementId("S3")
        )

        every { unsafeStatementUseCases.create(any()) } returnsMany newColumnTitleStatements

        val result = tableColumnsUpdater(command, state)

        result.asClue {
            it.table shouldBe state.table
            it.statements shouldBe state.statements
            it.validationCache shouldBe state.validationCache
            it.tempIdToThingId shouldBe state.tempIdToThingId
            it.headerIndices shouldBe state.headerIndices
            it.columns shouldBe existingColumnIds
            it.rows shouldBe state.rows
            it.existingColumns shouldBe state.existingColumns
            it.existingRows shouldBe state.existingRows
            it.thingsToDelete shouldBe state.thingsToDelete
            it.statementsToDelete shouldBe expecetedStatementsToDelete
        }

        verify(exactly = 1) {
            unsafeStatementUseCases.create(
                CreateStatementUseCase.CreateCommand(
                    contributorId = command.contributorId,
                    subjectId = existingColumnIds[0],
                    predicateId = Predicates.csvwTitles,
                    objectId = ThingId("L1")
                )
            )
        }
        verify(exactly = 1) {
            unsafeStatementUseCases.create(
                CreateStatementUseCase.CreateCommand(
                    contributorId = command.contributorId,
                    subjectId = existingColumnIds[1],
                    predicateId = Predicates.csvwTitles,
                    objectId = ThingId("L2")
                )
            )
        }
        verify(exactly = 1) {
            unsafeStatementUseCases.create(
                CreateStatementUseCase.CreateCommand(
                    contributorId = command.contributorId,
                    subjectId = existingColumnIds[2],
                    predicateId = Predicates.csvwTitles,
                    objectId = ThingId("L3")
                )
            )
        }
    }

    @Test
    fun `Given a table update command, when updating table columns, it creates missing table columns`() {
        val command = updateTableCommand()
        val existingColumnIds = listOf(
            ThingId("Column_1"),
            ThingId("Column_2")
        )
        val existingColumns = existingColumnIds.map(::createColumnGraph)
        val state = UpdateTableState().copy(
            existingColumns = existingColumns,
            validationCache = mapOf(
                "#temp1" from command,
                "#temp2" from command,
                "#temp3" from command
            ),
            tempIdToThingId = mapOf(
                "#temp1" to ThingId("L1"),
                "#temp2" to ThingId("L2"),
                "#temp3" to ThingId("L3")
            ),
            thingsToDelete = setOf(ThingId("ToBeDeleted")),
            statementsToDelete = setOf(StatementId("SalreadyExists"))
        )
        val newColumnId = ThingId("Column3")
        val columnIds = existingColumnIds + newColumnId
        val expecetedStatementsToDelete = state.statementsToDelete + setOf(
            StatementId("S_Column_1--CSVW_Titles--Column_1_Title"),
            StatementId("S_Column_2--CSVW_Titles--Column_2_Title"),
        )
        val newColumnTitleStatements = listOf(
            StatementId("S1"),
            StatementId("S2"),
        )

        every { unsafeStatementUseCases.create(any()) } returnsMany newColumnTitleStatements
        every {
            abstractTableColumnCreator.create(
                contributorId = command.contributorId,
                tableId = command.tableId,
                index = 2,
                titleLiteralId = ThingId("L3")
            )
        } returns newColumnId

        val result = tableColumnsUpdater(command, state)

        result.asClue {
            it.table shouldBe state.table
            it.statements shouldBe state.statements
            it.validationCache shouldBe state.validationCache
            it.tempIdToThingId shouldBe state.tempIdToThingId
            it.headerIndices shouldBe state.headerIndices
            it.columns shouldBe columnIds
            it.rows shouldBe state.rows
            it.existingColumns shouldBe state.existingColumns
            it.existingRows shouldBe state.existingRows
            it.thingsToDelete shouldBe state.thingsToDelete
            it.statementsToDelete shouldBe expecetedStatementsToDelete
        }

        verify(exactly = 1) {
            unsafeStatementUseCases.create(
                CreateStatementUseCase.CreateCommand(
                    contributorId = command.contributorId,
                    subjectId = existingColumnIds[0],
                    predicateId = Predicates.csvwTitles,
                    objectId = ThingId("L1")
                )
            )
        }
        verify(exactly = 1) {
            unsafeStatementUseCases.create(
                CreateStatementUseCase.CreateCommand(
                    contributorId = command.contributorId,
                    subjectId = existingColumnIds[1],
                    predicateId = Predicates.csvwTitles,
                    objectId = ThingId("L2")
                )
            )
        }
        verify(exactly = 1) {
            abstractTableColumnCreator.create(
                contributorId = command.contributorId,
                tableId = command.tableId,
                index = 2,
                titleLiteralId = ThingId("L3")
            )
        }
    }

    @Test
    fun `Given a table update command, when updating table columns, it deletes exceeding table columns`() {
        val command = updateTableCommand().copy(
            rows = listOf(
                CreateRowCommand(
                    label = "header",
                    data = listOf("#temp1")
                )
            )
        )
        val existingColumnIds = listOf(
            ThingId("Column_1"),
            ThingId("Column_2")
        )
        val existingColumns = existingColumnIds.map(::createColumnGraph)
        val state = UpdateTableState().copy(
            existingColumns = existingColumns,
            validationCache = mapOf(
                "#temp1" from command
            ),
            tempIdToThingId = mapOf(
                "#temp1" to ThingId("L1")
            ),
            thingsToDelete = setOf(ThingId("ToBeDeleted")),
            statementsToDelete = setOf(StatementId("SalreadyExists"))
        )
        val columnIds = existingColumnIds.take(1)
        val expectedThingsToDelete = state.thingsToDelete + ThingId("Column_2")
        val expecetedStatementsToDelete = state.statementsToDelete + existingColumns.last().statementIds + setOf(
            StatementId("S_Column_1--CSVW_Titles--Column_1_Title")
        )

        every { unsafeStatementUseCases.create(any()) } returns StatementId("S1")

        val result = tableColumnsUpdater(command, state)

        result.asClue {
            it.table shouldBe state.table
            it.statements shouldBe state.statements
            it.validationCache shouldBe state.validationCache
            it.tempIdToThingId shouldBe state.tempIdToThingId
            it.headerIndices shouldBe state.headerIndices
            it.columns shouldBe columnIds
            it.rows shouldBe state.rows
            it.existingColumns shouldBe state.existingColumns
            it.existingRows shouldBe state.existingRows
            it.thingsToDelete shouldBe expectedThingsToDelete
            it.statementsToDelete shouldBe expecetedStatementsToDelete
        }

        verify(exactly = 1) {
            unsafeStatementUseCases.create(
                CreateStatementUseCase.CreateCommand(
                    contributorId = command.contributorId,
                    subjectId = existingColumnIds[0],
                    predicateId = Predicates.csvwTitles,
                    objectId = ThingId("L1")
                )
            )
        }
    }

    @Test
    fun `Given a table update command, when updating table columns but rows are null, it does nothing`() {
        val command = updateTableCommand().copy(rows = null)
        val state = UpdateTableState()

        tableColumnsUpdater(command, state)
    }
}
