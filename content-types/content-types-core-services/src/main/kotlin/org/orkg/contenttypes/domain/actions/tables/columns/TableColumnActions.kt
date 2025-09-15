package org.orkg.contenttypes.domain.actions.tables.columns

import org.orkg.common.ThingId
import org.orkg.contenttypes.domain.Table
import org.orkg.contenttypes.domain.actions.Action
import org.orkg.contenttypes.domain.actions.CreateTableColumnCommand
import org.orkg.contenttypes.domain.actions.DeleteTableColumnCommand
import org.orkg.contenttypes.domain.actions.UpdateTableColumnCommand
import org.orkg.graph.domain.GeneralStatement

interface CreateTableColumnAction : Action<CreateTableColumnCommand, CreateTableColumnAction.State> {
    data class State(
        val table: Table? = null,
        val statements: Map<ThingId, List<GeneralStatement>> = emptyMap(),
        val columnId: ThingId? = null,
    )
}

interface UpdateTableColumnAction : Action<UpdateTableColumnCommand, UpdateTableColumnAction.State> {
    data class State(
        val table: Table? = null,
        val statements: Map<ThingId, List<GeneralStatement>> = emptyMap(),
    )
}

interface DeleteTableColumnAction : Action<DeleteTableColumnCommand, DeleteTableColumnAction.State> {
    data class State(
        val table: Table? = null,
        val statements: Map<ThingId, List<GeneralStatement>> = emptyMap(),
    )
}
