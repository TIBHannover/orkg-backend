package org.orkg.contenttypes.domain.actions

import dev.forkhandles.values.ofOrNull
import org.orkg.common.Either
import org.orkg.contenttypes.domain.ThingIsNotAClass
import org.orkg.contenttypes.input.ThingDefinitions
import org.orkg.graph.domain.Class
import org.orkg.graph.domain.InvalidLabel
import org.orkg.graph.domain.InvalidLiteralLabel
import org.orkg.graph.domain.Label
import org.orkg.graph.domain.MAX_LABEL_LENGTH
import org.orkg.graph.domain.ReservedClass
import org.orkg.graph.domain.Thing
import org.orkg.graph.domain.reservedClassIds
import org.orkg.graph.output.ThingRepository

abstract class ThingDefinitionValidator(
    override val thingRepository: ThingRepository
) : ThingIdValidator {
    internal fun validateThingDefinitions(
        thingDefinitions: ThingDefinitions,
        tempIds: Set<String>,
        validatedIds: MutableMap<String, Either<String, Thing>>
    ) {
        validateIds(thingDefinitions, tempIds, validatedIds)
        validateLabels(thingDefinitions)
    }

    private fun validateIds(
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

    private fun validateLabels(thingDefinitions: ThingDefinitions) {
        thingDefinitions.resources.values.forEach {
            Label.ofOrNull(it.label) ?: throw InvalidLabel()
        }
        thingDefinitions.literals.values.forEach {
            if (it.label.length > MAX_LABEL_LENGTH) {
                throw InvalidLiteralLabel()
            }
        }
        thingDefinitions.predicates.values.forEach {
            Label.ofOrNull(it.label) ?: throw InvalidLabel()
        }
        thingDefinitions.classes.values.forEach {
            Label.ofOrNull(it.label) ?: throw InvalidLabel()
        }
        thingDefinitions.lists.values.forEach {
            Label.ofOrNull(it.label) ?: throw InvalidLabel()
        }
    }
}
