package org.orkg.contenttypes.domain.actions.tables

import org.orkg.common.Either
import org.orkg.common.ThingId
import org.orkg.contenttypes.domain.Table
import org.orkg.contenttypes.domain.Table.Row
import org.orkg.contenttypes.domain.actions.Action
import org.orkg.contenttypes.domain.actions.CreateTableCommand
import org.orkg.contenttypes.domain.actions.UpdateTableCommand
import org.orkg.contenttypes.input.CreateThingCommandPart
import org.orkg.graph.domain.GeneralStatement
import org.orkg.graph.domain.StatementId
import org.orkg.graph.domain.Thing

interface CreateTableAction : Action<CreateTableCommand, CreateTableAction.State> {
    data class State(
        val validationCache: Map<String, Either<CreateThingCommandPart, Thing>> = emptyMap(),
        val tableId: ThingId? = null,
        val tempIdToThingId: Map<String, ThingId> = emptyMap(),
        val columns: List<ThingId> = emptyList(),
        val rows: List<ThingId> = emptyList(),
    ) {
        fun resolve(id: String): ThingId? =
            validationCache[id]?.fold({ tempIdToThingId[id] }, { it.id })
    }
}

interface UpdateTableAction : Action<UpdateTableCommand, UpdateTableAction.State> {
    data class State(
        val table: Table? = null,
        val statements: Map<ThingId, List<GeneralStatement>> = emptyMap(),
        val validationCache: Map<String, Either<CreateThingCommandPart, Thing>> = emptyMap(),
        val tempIdToThingId: Map<String, ThingId> = emptyMap(),
        val headerIndices: List<Int> = emptyList(),
        val columns: List<ThingId> = emptyList(),
        val rows: List<ThingId> = emptyList(),
        val existingColumns: List<ColumnGraph> = emptyList(),
        val existingRows: List<RowGraph> = emptyList(),
        val thingsToDelete: Set<ThingId> = emptySet(),
        val statementsToDelete: Set<StatementId> = emptySet(),
    ) {
        fun resolve(id: String): ThingId? =
            validationCache[id]?.fold({ tempIdToThingId[id] }, { it.id })

        /**
         * The row subgraph is defined as the row resource with all incoming and outgoing statements.
         * The statement that links to each cell resource is not included in statementIds.
         */
        data class RowGraph(
            val tableHasRowStatement: GeneralStatement,
            val labelStatement: GeneralStatement?,
            val indexStatement: GeneralStatement?,
            val cells: List<CellGraph?>,
        ) {
            val rowId: ThingId = tableHasRowStatement.`object`.id
            val label: String? = labelStatement?.`object`?.label
            val statementIds: Set<StatementId> = setOfNotNull(
                tableHasRowStatement.id,
                labelStatement?.id,
                indexStatement?.id,
            )

            fun toRow(): Row =
                Row(label, cells.map { it?.value })
        }

        /**
         * The column subgraph is defined as the column resource with all incoming and outgoing statements,
         * expect for incoming statements from cell resources.
         */
        data class ColumnGraph(
            val tableHasColumnStatement: GeneralStatement,
            val labelStatement: GeneralStatement?,
            val indexStatement: GeneralStatement?,
        ) {
            val columnId: ThingId = tableHasColumnStatement.`object`.id
            val label: String? = labelStatement?.`object`?.label
            val statementIds: Set<StatementId> = setOfNotNull(
                tableHasColumnStatement.id,
                labelStatement?.id,
                indexStatement?.id,
            )
        }

        /**
         * The cell subgraph is defined as the cell resource with all incoming and outgoing statements.
         * The value statement also has to be included in statementIds.
         */
        data class CellGraph(
            val rowHasCellStatement: GeneralStatement,
            val valueStatement: GeneralStatement?,
            val columnStatement: GeneralStatement?,
        ) {
            val cellId: ThingId = rowHasCellStatement.`object`.id
            val value: Thing? = valueStatement?.`object`
            val statementIds = setOfNotNull(
                rowHasCellStatement.id,
                columnStatement?.id,
                valueStatement?.id
            )
        }
    }
}
