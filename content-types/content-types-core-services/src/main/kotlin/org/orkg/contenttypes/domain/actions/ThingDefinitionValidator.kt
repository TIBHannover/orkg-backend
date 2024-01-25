package org.orkg.contenttypes.domain.actions

import org.orkg.common.Either
import org.orkg.contenttypes.domain.ThingIsNotAClass
import org.orkg.contenttypes.input.ThingDefinitions
import org.orkg.graph.domain.Class
import org.orkg.graph.domain.ReservedClass
import org.orkg.graph.domain.Thing
import org.orkg.graph.domain.reservedClassIds
import org.orkg.graph.output.ThingRepository

abstract class ThingDefinitionValidator(
    override val thingRepository: ThingRepository
) : ThingIdValidator {
    internal fun validateIdsInDefinitions(
        thingDefinitions: ThingDefinitions,
        tempIds: Set<String>,
        validatedIds: MutableMap<String, Either<String, Thing>>
    ) {
        thingDefinitions.lists.values
            .flatMap { it.elements }
            .forEach { validateId(it, tempIds, validatedIds) }
        thingDefinitions.resources.values
            .flatMap { it.classes }
            .toSet()
            .forEach {
                if (it in reservedClassIds) {
                    throw ReservedClass(it)
                }
                validateId(it.value, tempIds, validatedIds).onRight { thing ->
                    if (thing !is Class) {
                        throw ThingIsNotAClass(thing.id)
                    }
                }
            }
    }
}
