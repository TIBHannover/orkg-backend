package org.orkg.contenttypes.domain.actions.tables

import org.orkg.common.Either
import org.orkg.common.ThingId
import org.orkg.contenttypes.domain.actions.Action
import org.orkg.contenttypes.domain.actions.CreateTableCommand
import org.orkg.graph.domain.Thing

interface CreateTableAction : Action<CreateTableCommand, CreateTableAction.State> {
    data class State(
        val tempIds: Set<String> = emptySet(),
        val validatedIds: Map<String, Either<String, Thing>> = emptyMap(),
        val tableId: ThingId? = null,
        val tempIdToThing: Map<String, ThingId> = emptyMap(),
        val columns: List<ThingId> = emptyList(),
        val rows: List<ThingId> = emptyList(),
    )
}
