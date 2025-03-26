package org.orkg.contenttypes.domain.actions.tables

import org.orkg.common.Either
import org.orkg.common.ThingId
import org.orkg.contenttypes.domain.actions.Action
import org.orkg.contenttypes.domain.actions.CreateTableCommand
import org.orkg.contenttypes.input.CreateThingCommandPart
import org.orkg.graph.domain.Thing

interface CreateTableAction : Action<CreateTableCommand, CreateTableAction.State> {
    data class State(
        val validationCache: Map<String, Either<CreateThingCommandPart, Thing>> = emptyMap(),
        val tableId: ThingId? = null,
        val tempIdToThingId: Map<String, ThingId> = emptyMap(),
        val columns: List<ThingId> = emptyList(),
        val rows: List<ThingId> = emptyList(),
    )
}
