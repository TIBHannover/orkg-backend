package org.orkg.contenttypes.domain.actions

import dev.forkhandles.values.ofOrNull
import org.orkg.common.Either
import org.orkg.common.PageRequests
import org.orkg.common.toIRIOrNull
import org.orkg.contenttypes.domain.DuplicateURIs
import org.orkg.contenttypes.domain.ThingIsNotAClass
import org.orkg.contenttypes.input.CreateThingCommandPart
import org.orkg.contenttypes.input.CreateThingsCommand
import org.orkg.graph.domain.Class
import org.orkg.graph.domain.InvalidLabel
import org.orkg.graph.domain.InvalidLiteralDatatype
import org.orkg.graph.domain.InvalidLiteralLabel
import org.orkg.graph.domain.Label
import org.orkg.graph.domain.Literals
import org.orkg.graph.domain.MAX_LABEL_LENGTH
import org.orkg.graph.domain.ReservedClassId
import org.orkg.graph.domain.Thing
import org.orkg.graph.domain.URIAlreadyInUse
import org.orkg.graph.domain.URINotAbsolute
import org.orkg.graph.domain.reservedClassIds
import org.orkg.graph.output.ThingRepository

class ThingsCommandValidator(
    private val thingIdValidator: ThingIdValidator,
    private val thingRepository: ThingRepository,
) {
    constructor(thingRepository: ThingRepository) : this(
        ThingIdValidator(thingRepository),
        thingRepository,
    )

    internal fun validate(
        thingsCommand: CreateThingsCommand,
        validationCache: Map<String, Either<CreateThingCommandPart, Thing>>,
    ): Map<String, Either<CreateThingCommandPart, Thing>> {
        val result = validationCache.toMutableMap()
        validateIds(thingsCommand, result)
        validateLabels(thingsCommand)
        validateURIs(thingsCommand)
        return result
    }

    private fun validateIds(
        thingsCommand: CreateThingsCommand,
        validationCache: MutableMap<String, Either<CreateThingCommandPart, Thing>>,
    ) {
        val thingCommands = thingsCommand.all()
        thingsCommand.lists.values
            .flatMap { it.elements }
            .forEach { thingIdValidator.validate(it, thingCommands, validationCache) }
        thingsCommand.resources.values
            .flatMap { it.classes }
            .toSet()
            .forEach {
                if (it in reservedClassIds) {
                    throw ReservedClassId(it)
                }
                thingIdValidator.validate(it.value, thingCommands, validationCache).onRight { thing ->
                    if (thing !is Class) {
                        throw ThingIsNotAClass(thing.id)
                    }
                }
            }
    }

    private fun validateLabels(thingsCommand: CreateThingsCommand) {
        thingsCommand.resources.values.forEach {
            Label.ofOrNull(it.label) ?: throw InvalidLabel()
        }
        thingsCommand.literals.values.forEach {
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
        thingsCommand.predicates.values.forEach {
            Label.ofOrNull(it.label) ?: throw InvalidLabel()
        }
        thingsCommand.classes.values.forEach {
            Label.ofOrNull(it.label) ?: throw InvalidLabel()
        }
        thingsCommand.lists.values.forEach {
            Label.ofOrNull(it.label) ?: throw InvalidLabel()
        }
    }

    private fun validateURIs(thingsCommand: CreateThingsCommand) {
        val uris = thingsCommand.uris()
        val duplicates = uris.groupingBy { it }
            .eachCount()
            .filter { it.value > 1 }
        if (duplicates.isNotEmpty()) {
            throw DuplicateURIs(duplicates)
        }
        uris.forEach { uri ->
            if (!uri.isAbsolute) {
                throw URINotAbsolute(uri)
            }
            val things = thingRepository.findAll(PageRequests.SINGLE, uri = uri)
            if (!things.isEmpty) {
                throw URIAlreadyInUse(uri, things.single().id)
            }
        }
    }
}
