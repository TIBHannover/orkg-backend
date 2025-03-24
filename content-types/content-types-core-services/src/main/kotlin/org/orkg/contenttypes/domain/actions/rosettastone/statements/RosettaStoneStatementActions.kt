package org.orkg.contenttypes.domain.actions.rosettastone.statements

import org.orkg.common.Either
import org.orkg.common.ThingId
import org.orkg.contenttypes.domain.RosettaStoneStatement
import org.orkg.contenttypes.domain.RosettaStoneTemplate
import org.orkg.contenttypes.domain.actions.Action
import org.orkg.contenttypes.domain.actions.CreateRosettaStoneStatementCommand
import org.orkg.contenttypes.domain.actions.UpdateRosettaStoneStatementCommand
import org.orkg.contenttypes.input.CreateThingCommandPart
import org.orkg.graph.domain.Thing

interface CreateRosettaStoneStatementAction : Action<CreateRosettaStoneStatementCommand, CreateRosettaStoneStatementAction.State> {
    data class State(
        val rosettaStoneTemplate: RosettaStoneTemplate? = null,
        val rosettaStoneStatementId: ThingId? = null,
        val validationCache: Map<String, Either<CreateThingCommandPart, Thing>> = emptyMap(),
        val tempIdToThing: Map<String, ThingId> = emptyMap(),
    )
}

interface UpdateRosettaStoneStatementAction : Action<UpdateRosettaStoneStatementCommand, UpdateRosettaStoneStatementAction.State> {
    data class State(
        val rosettaStoneStatement: RosettaStoneStatement? = null,
        val rosettaStoneTemplate: RosettaStoneTemplate? = null,
        val rosettaStoneStatementId: ThingId? = null,
        val validationCache: Map<String, Either<CreateThingCommandPart, Thing>> = emptyMap(),
        val tempIdToThing: Map<String, ThingId> = emptyMap(),
    )
}
