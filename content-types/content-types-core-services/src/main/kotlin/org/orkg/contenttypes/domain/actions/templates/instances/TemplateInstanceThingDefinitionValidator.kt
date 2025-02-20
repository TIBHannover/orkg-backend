package org.orkg.contenttypes.domain.actions.templates.instances

import org.orkg.contenttypes.domain.actions.ThingDefinitionValidator
import org.orkg.contenttypes.domain.actions.UpdateTemplateInstanceCommand
import org.orkg.contenttypes.domain.actions.UpdateTemplateInstanceState
import org.orkg.graph.output.ClassRepository
import org.orkg.graph.output.ThingRepository

class TemplateInstanceThingDefinitionValidator(
    private val thingDefinitionValidator: ThingDefinitionValidator,
) : UpdateTemplateInstanceAction {
    constructor(thingRepository: ThingRepository, classRepository: ClassRepository) : this(
        ThingDefinitionValidator(
            thingRepository,
            classRepository
        )
    )

    override operator fun invoke(
        command: UpdateTemplateInstanceCommand,
        state: UpdateTemplateInstanceState,
    ): UpdateTemplateInstanceState =
        state.copy(
            validatedIds = thingDefinitionValidator.validateThingDefinitions(
                thingDefinitions = command,
                tempIds = state.tempIds,
                validatedIds = state.validatedIds
            )
        )
}
