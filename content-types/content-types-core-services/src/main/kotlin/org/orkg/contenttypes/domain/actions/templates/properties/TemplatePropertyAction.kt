package org.orkg.contenttypes.domain.actions.templates.properties

import org.orkg.common.ThingId
import org.orkg.contenttypes.domain.actions.Action
import org.orkg.contenttypes.domain.actions.CreateTemplatePropertyCommand
import org.orkg.contenttypes.domain.actions.templates.properties.TemplatePropertyAction.State

interface TemplatePropertyAction : Action<CreateTemplatePropertyCommand, State> {
    data class State(
        val templatePropertyId: ThingId? = null,
        val propertyCount: Int? = null
    )
}
