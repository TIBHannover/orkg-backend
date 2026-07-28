package org.orkg.contenttypes.domain.actions.templates.instances

import org.orkg.contenttypes.domain.actions.CreateTemplateInstanceCommand
import org.orkg.contenttypes.domain.actions.ThingsCommandValidator
import org.orkg.contenttypes.domain.actions.templates.instances.CreateTemplateInstanceAction.State
import org.orkg.graph.output.ThingRepository

class TemplateInstanceThingsCommandCreateValidator(
    private val thingsCommandValidator: ThingsCommandValidator,
) : CreateTemplateInstanceAction {
    constructor(
        thingRepository: ThingRepository,
    ) : this(
        ThingsCommandValidator(thingRepository),
    )

    override fun invoke(command: CreateTemplateInstanceCommand, state: State): State =
        state.copy(
            validationCache = thingsCommandValidator.validate(
                thingsCommand = command,
                validationCache = state.validationCache,
            ),
        )
}
