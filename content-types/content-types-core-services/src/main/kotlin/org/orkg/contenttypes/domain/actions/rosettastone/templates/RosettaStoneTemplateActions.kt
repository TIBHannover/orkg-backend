package org.orkg.contenttypes.domain.actions.rosettastone.templates

import org.orkg.common.ThingId
import org.orkg.contenttypes.domain.actions.Action
import org.orkg.contenttypes.domain.actions.CreateRosettaStoneTemplateCommand

interface CreateRosettaStoneTemplateAction : Action<CreateRosettaStoneTemplateCommand, CreateRosettaStoneTemplateAction.State> {
    data class State(
        val rosettaStoneTemplateId: ThingId? = null
    )
}
