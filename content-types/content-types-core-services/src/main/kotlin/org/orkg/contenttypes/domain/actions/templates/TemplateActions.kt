package org.orkg.contenttypes.domain.actions.templates

import org.orkg.common.ThingId
import org.orkg.contenttypes.domain.Template
import org.orkg.contenttypes.domain.actions.Action
import org.orkg.contenttypes.domain.actions.CreateTemplateCommand
import org.orkg.contenttypes.domain.actions.UpdateTemplateCommand
import org.orkg.graph.domain.GeneralStatement

interface CreateTemplateAction : Action<CreateTemplateCommand, CreateTemplateAction.State> {
    data class State(
        val templateId: ThingId? = null,
    )
}

interface UpdateTemplateAction : Action<UpdateTemplateCommand, UpdateTemplateAction.State> {
    data class State(
        val template: Template? = null,
        val statements: Map<ThingId, List<GeneralStatement>> = emptyMap(),
    )
}
