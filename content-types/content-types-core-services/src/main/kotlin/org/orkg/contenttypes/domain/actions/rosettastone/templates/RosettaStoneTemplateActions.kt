package org.orkg.contenttypes.domain.actions.rosettastone.templates

import org.orkg.common.ThingId
import org.orkg.contenttypes.domain.RosettaStoneTemplate
import org.orkg.contenttypes.domain.actions.Action
import org.orkg.contenttypes.domain.actions.CreateRosettaStoneTemplateCommand
import org.orkg.contenttypes.domain.actions.UpdateRosettaStoneTemplateCommand
import org.orkg.graph.domain.GeneralStatement

interface CreateRosettaStoneTemplateAction : Action<CreateRosettaStoneTemplateCommand, CreateRosettaStoneTemplateAction.State> {
    data class State(
        val rosettaStoneTemplateId: ThingId? = null
    )
}

interface UpdateRosettaStoneTemplateAction : Action<UpdateRosettaStoneTemplateCommand, UpdateRosettaStoneTemplateAction.State> {
    data class State(
        val rosettaStoneTemplate: RosettaStoneTemplate? = null,
        val isUsedInRosettaStoneStatement: Boolean = false,
        val statements: Map<ThingId, List<GeneralStatement>> = emptyMap()
    )
}
