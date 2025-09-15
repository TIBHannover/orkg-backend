package org.orkg.contenttypes.domain.actions.tables

import org.orkg.common.Either
import org.orkg.common.ThingId
import org.orkg.contenttypes.domain.Table
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
    }
}
