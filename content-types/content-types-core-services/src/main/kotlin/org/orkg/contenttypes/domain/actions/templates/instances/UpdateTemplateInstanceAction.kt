package org.orkg.contenttypes.domain.actions.templates.instances

import org.orkg.common.Either
import org.orkg.contenttypes.domain.Template
import org.orkg.contenttypes.domain.TemplateInstance
import org.orkg.contenttypes.domain.actions.Action
import org.orkg.contenttypes.domain.actions.BakedStatement
import org.orkg.contenttypes.domain.actions.UpdateTemplateInstanceCommand
import org.orkg.contenttypes.domain.actions.templates.instances.UpdateTemplateInstanceAction.State
import org.orkg.contenttypes.input.CreateLiteralCommandPart
import org.orkg.contenttypes.input.CreateThingCommandPart
import org.orkg.graph.domain.Thing

interface UpdateTemplateInstanceAction : Action<UpdateTemplateInstanceCommand, State> {
    data class State(
        val template: Template? = null,
        val templateInstance: TemplateInstance? = null,
        val validationCache: Map<String, Either<CreateThingCommandPart, Thing>> = emptyMap(),
        val statementsToAdd: Set<BakedStatement> = emptySet(),
        val statementsToRemove: Set<BakedStatement> = emptySet(),
        val literals: Map<String, CreateLiteralCommandPart> = emptyMap(),
    )
}
