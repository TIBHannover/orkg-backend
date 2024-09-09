package org.orkg.contenttypes.domain.actions

import dev.forkhandles.values.ofOrNull
import org.orkg.common.Either
import org.orkg.common.toIRIOrNull
import org.orkg.contenttypes.domain.ThingIsNotAClass
import org.orkg.contenttypes.input.ThingDefinitions
import org.orkg.graph.domain.Class
import org.orkg.graph.domain.InvalidLabel
import org.orkg.graph.domain.InvalidLiteralDatatype
import org.orkg.graph.domain.InvalidLiteralLabel
import org.orkg.graph.domain.Label
import org.orkg.graph.domain.Literals
import org.orkg.graph.domain.MAX_LABEL_LENGTH
import org.orkg.graph.domain.ReservedClass
import org.orkg.graph.domain.Thing
import org.orkg.graph.domain.URIAlreadyInUse
import org.orkg.graph.domain.URINotAbsolute
import org.orkg.graph.domain.reservedClassIds
import org.orkg.graph.output.ClassRepository
import org.orkg.graph.output.ThingRepository

open class ThingDefinitionValidator(
    override val thingRepository: ThingRepository,
    private val classRepository: ClassRepository
) : ThingIdValidator {
    internal fun validateThingDefinitionsInPlace(
        thingDefinitions: ThingDefinitions,
        tempIds: Set<String>,
        validatedIds: MutableMap<String, Either<String, Thing>>
    ) {
        validateIds(thingDefinitions, tempIds, validatedIds)
        validateLabels(thingDefinitions)
        validateClassURIs(thingDefinitions)
    }

    internal fun validateThingDefinitions(
        thingDefinitions: ThingDefinitions,
        tempIds: Set<String>,
        validatedIds: Map<String, Either<String, Thing>>
    ): Map<String, Either<String, Thing>> {
        val result = validatedIds.toMutableMap()
        validateThingDefinitionsInPlace(thingDefinitions, tempIds, result)
        return result
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
            val xsd = Literals.XSD.fromString(it.dataType)
            if (xsd != null && !xsd.canParse(it.label)) {
                throw InvalidLiteralLabel(it.label, it.dataType)
            } else if (it.dataType.toIRIOrNull()?.isAbsolute != true) {
                throw InvalidLiteralDatatype()
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

    private fun validateClassURIs(thingDefinitions: ThingDefinitions) {
        thingDefinitions.classes.values.forEach {
            if (it.uri != null) {
                if (!it.uri!!.isAbsolute) {
                    throw URINotAbsolute(it.uri!!)
                }
                classRepository.findByUri(it.uri.toString()).ifPresent { found ->
                    throw URIAlreadyInUse(found.uri!!, found.id)
                }
            }
        }
    }
}
