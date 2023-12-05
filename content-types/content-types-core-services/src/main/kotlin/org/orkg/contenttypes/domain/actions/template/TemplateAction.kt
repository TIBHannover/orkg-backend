package org.orkg.contenttypes.domain.actions.template

import org.orkg.common.ThingId
import org.orkg.contenttypes.domain.actions.Action
import org.orkg.contenttypes.domain.actions.CreateTemplateCommand
import org.orkg.contenttypes.domain.actions.template.TemplateAction.State

interface TemplateAction : Action<CreateTemplateCommand, State> {
    data class State(
        val templateId: ThingId? = null
    )
}
