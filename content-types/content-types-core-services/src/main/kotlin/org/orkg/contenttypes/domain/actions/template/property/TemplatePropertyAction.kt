package org.orkg.contenttypes.domain.actions.template.property

import org.orkg.common.ThingId
import org.orkg.contenttypes.domain.actions.Action
import org.orkg.contenttypes.domain.actions.CreateTemplatePropertyCommand
import org.orkg.contenttypes.domain.actions.template.property.TemplatePropertyAction.State

interface TemplatePropertyAction : Action<CreateTemplatePropertyCommand, State> {
    data class State(
        val templatePropertyId: ThingId? = null,
        val propertyCount: Int? = null
    )
}
