package org.orkg.contenttypes.domain.actions.tables.rows

import org.orkg.common.ThingId
import org.orkg.contenttypes.domain.Table
import org.orkg.contenttypes.domain.actions.Action
import org.orkg.contenttypes.domain.actions.CreateTableRowCommand
import org.orkg.contenttypes.domain.actions.DeleteTableRowCommand
import org.orkg.contenttypes.domain.actions.UpdateTableRowCommand
import org.orkg.graph.domain.GeneralStatement

interface CreateTableRowAction : Action<CreateTableRowCommand, CreateTableRowAction.State> {
    data class State(
        val table: Table? = null,
        val statements: Map<ThingId, List<GeneralStatement>> = emptyMap(),
        val rowId: ThingId? = null,
    )
}

interface UpdateTableRowAction : Action<UpdateTableRowCommand, UpdateTableRowAction.State> {
    data class State(
        val table: Table? = null,
        val statements: Map<ThingId, List<GeneralStatement>> = emptyMap(),
    )
}

interface DeleteTableRowAction : Action<DeleteTableRowCommand, DeleteTableRowAction.State> {
    data class State(
        val table: Table? = null,
        val statements: Map<ThingId, List<GeneralStatement>> = emptyMap(),
    )
}
