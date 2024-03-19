package org.orkg.contenttypes.domain.actions.templates.properties

import org.orkg.common.ThingId
import org.orkg.contenttypes.domain.Template
import org.orkg.contenttypes.domain.TemplateProperty
import org.orkg.contenttypes.domain.actions.Action
import org.orkg.contenttypes.domain.actions.CreateTemplatePropertyCommand
import org.orkg.contenttypes.domain.actions.UpdateTemplatePropertyCommand

interface CreateTemplatePropertyAction : Action<CreateTemplatePropertyCommand, CreateTemplatePropertyAction.State> {
    data class State(
        val templatePropertyId: ThingId? = null,
        val propertyCount: Int? = null
    )
}

interface UpdateTemplatePropertyAction : Action<UpdateTemplatePropertyCommand, UpdateTemplatePropertyAction.State> {
    data class State(
        val template: Template? = null,
        val templateProperty: TemplateProperty? = null
    )
}
