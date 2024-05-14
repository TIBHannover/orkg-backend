package org.orkg.contenttypes.domain.actions.rosettastone.statements

import org.orkg.common.Either
import org.orkg.common.ThingId
import org.orkg.contenttypes.domain.RosettaStoneTemplate
import org.orkg.contenttypes.domain.actions.Action
import org.orkg.contenttypes.domain.actions.CreateRosettaStoneStatementCommand
import org.orkg.graph.domain.Thing

interface CreateRosettaStoneStatementAction : Action<CreateRosettaStoneStatementCommand, CreateRosettaStoneStatementAction.State> {
    data class State(
        val rosettaStoneTemplate: RosettaStoneTemplate? = null,
        val rosettaStoneStatementId: ThingId? = null,
        val tempIds: Set<String> = emptySet(),
        val validatedIds: Map<String, Either<String, Thing>> = emptyMap(),
        val tempId2Thing: Map<String, ThingId> = emptyMap()
    )
}
