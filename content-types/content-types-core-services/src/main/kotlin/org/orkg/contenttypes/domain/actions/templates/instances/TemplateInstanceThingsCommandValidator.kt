package org.orkg.contenttypes.domain.actions.templates.instances

import org.orkg.contenttypes.domain.actions.ThingsCommandValidator
import org.orkg.contenttypes.domain.actions.UpdateTemplateInstanceCommand
import org.orkg.contenttypes.domain.actions.UpdateTemplateInstanceState
import org.orkg.graph.output.ClassRepository
import org.orkg.graph.output.ThingRepository

class TemplateInstanceThingsCommandValidator(
    private val thingsCommandValidator: ThingsCommandValidator,
) : UpdateTemplateInstanceAction {
    constructor(thingRepository: ThingRepository, classRepository: ClassRepository) : this(
        ThingsCommandValidator(
            thingRepository,
            classRepository
        )
    )

    override operator fun invoke(
        command: UpdateTemplateInstanceCommand,
        state: UpdateTemplateInstanceState,
    ): UpdateTemplateInstanceState =
        state.copy(
            validationCache = thingsCommandValidator.validate(
                thingsCommand = command,
                validationCache = state.validationCache
            )
        )
}
