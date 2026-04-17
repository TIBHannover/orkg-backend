package org.orkg.contenttypes.domain.actions.templates.instances

import org.orkg.common.Either
import org.orkg.common.ThingId
import org.orkg.contenttypes.domain.Template
import org.orkg.contenttypes.domain.TemplateInstance
import org.orkg.contenttypes.domain.actions.Action
import org.orkg.contenttypes.domain.actions.BakedStatement
import org.orkg.contenttypes.domain.actions.CreateTemplateInstanceCommand
import org.orkg.contenttypes.domain.actions.UpdateTemplateInstanceCommand
import org.orkg.contenttypes.input.CreateLiteralCommandPart
import org.orkg.contenttypes.input.CreateThingCommandPart
import org.orkg.graph.domain.Thing

interface CreateTemplateInstanceAction : Action<CreateTemplateInstanceCommand, CreateTemplateInstanceAction.State> {
    data class State(
        val templateInstanceId: ThingId? = null,
        val template: Template? = null,
        val validationCache: Map<String, Either<CreateThingCommandPart, Thing>> = emptyMap(),
        val statementsToAdd: Set<Pair<String, String>> = emptySet(),
        val literals: Map<String, CreateLiteralCommandPart> = emptyMap(),
    )
}

interface UpdateTemplateInstanceAction : Action<UpdateTemplateInstanceCommand, UpdateTemplateInstanceAction.State> {
    data class State(
        val template: Template? = null,
        val templateInstance: TemplateInstance? = null,
        val validationCache: Map<String, Either<CreateThingCommandPart, Thing>> = emptyMap(),
        val statementsToAdd: Set<BakedStatement> = emptySet(),
        val statementsToRemove: Set<BakedStatement> = emptySet(),
        val literals: Map<String, CreateLiteralCommandPart> = emptyMap(),
    )
}
