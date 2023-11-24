package org.orkg.contenttypes.domain.actions

import org.orkg.common.Either
import org.orkg.contenttypes.domain.ThingIsNotAClass
import org.orkg.contenttypes.input.CreatePaperUseCase
import org.orkg.graph.domain.Class
import org.orkg.graph.domain.Thing
import org.orkg.graph.output.ThingRepository

abstract class ThingDefinitionValidator(
    override val thingRepository: ThingRepository
) : ThingIdValidator {
    internal fun validateIdsInDefinitions(
        contents: CreatePaperUseCase.CreateCommand.PaperContents,
        tempIds: Set<String>,
        validatedIds: MutableMap<String, Either<String, Thing>>
    ) {
        contents.lists.values
            .flatMap { it.elements }
            .forEach { validateId(it, tempIds, validatedIds) }
        contents.resources.values
            .flatMap { it.classes }
            .toSet()
            .forEach {
                validateId(it.value, tempIds, validatedIds).onRight { thing ->
                    if (thing !is Class) {
                        throw ThingIsNotAClass(thing.id)
                    }
                }
            }
    }
}
