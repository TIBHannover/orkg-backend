package org.orkg.contenttypes.domain.actions

import dev.forkhandles.values.ofOrNull
import org.orkg.common.Either
import org.orkg.contenttypes.domain.EmptyContribution
import org.orkg.contenttypes.domain.InvalidStatementSubject
import org.orkg.contenttypes.domain.ThingIsNotAClass
import org.orkg.contenttypes.domain.ThingIsNotAPredicate
import org.orkg.contenttypes.input.CreateContributionCommandPart
import org.orkg.contenttypes.input.CreateContributionCommandPart.StatementObject
import org.orkg.contenttypes.input.CreateLiteralCommandPart
import org.orkg.contenttypes.input.CreatePredicateCommandPart
import org.orkg.contenttypes.input.CreateThingCommandPart
import org.orkg.graph.domain.Class
import org.orkg.graph.domain.InvalidLabel
import org.orkg.graph.domain.Label
import org.orkg.graph.domain.Literal
import org.orkg.graph.domain.Predicate
import org.orkg.graph.domain.Thing
import org.orkg.graph.output.ThingRepository

class ContributionValidator(
    private val thingIdValidator: ThingIdValidator,
) {
    constructor(thingRepository: ThingRepository) : this(ThingIdValidator(thingRepository))

    internal fun validate(
        validationCacheIn: Map<String, Either<CreateThingCommandPart, Thing>>,
        thingCommands: Map<String, CreateThingCommandPart>,
        contributionCommands: List<CreateContributionCommandPart>,
    ): Result {
        val validationCache = validationCacheIn.toMutableMap()
        val bakedStatements = mutableSetOf<BakedStatement>()
        contributionCommands.forEachIndexed { index, contribution ->
            Label.ofOrNull(contribution.label) ?: throw InvalidLabel()
            if (contribution.statements.isEmpty()) {
                if (contributionCommands.size == 1) {
                    throw EmptyContribution()
                } else {
                    throw EmptyContribution(index)
                }
            }
            contribution.classes.forEach {
                thingIdValidator.validate(it.value, thingCommands, validationCache).onRight { thing ->
                    if (thing !is Class) {
                        throw ThingIsNotAClass(thing.id)
                    }
                }
            }
            bakeStatements(
                subject = "^$index",
                statementCommands = contribution.statements,
                thingCommands = thingCommands,
                validationCache = validationCache,
                destination = bakedStatements
            )
        }
        return Result(validationCache, bakedStatements)
    }

    internal fun bakeStatements(
        subject: String,
        statementCommands: Map<String, List<StatementObject>>,
        thingCommands: Map<String, CreateThingCommandPart>,
        validationCache: MutableMap<String, Either<CreateThingCommandPart, Thing>>,
        destination: MutableSet<BakedStatement>,
    ) {
        statementCommands.forEach { (predicateId, objects) ->
            val validatedPredicate = thingIdValidator.validate(predicateId, thingCommands, validationCache)
            validatedPredicate.onLeft { tempId ->
                if (tempId !is CreatePredicateCommandPart) {
                    throw ThingIsNotAPredicate(predicateId)
                }
            }
            validatedPredicate.onRight { thing ->
                if (thing !is Predicate) {
                    throw ThingIsNotAPredicate(thing.id.value)
                }
            }
            objects.forEach { `object` ->
                val objectId = `object`.id
                val validatedObject = thingIdValidator.validate(objectId, thingCommands, validationCache)
                // TODO: Do we disallow linking to existing literals?
                // TODO: Do we ignore duplicate statement commands or do we want throw an error?
                destination += BakedStatement(subject, predicateId, objectId)
                if (`object`.statements != null) {
                    validatedObject.onLeft { tempId ->
                        if (tempId is CreateLiteralCommandPart) {
                            throw InvalidStatementSubject(objectId)
                        }
                    }
                    validatedObject.onRight { thing ->
                        if (thing is Literal) {
                            throw InvalidStatementSubject(objectId)
                        }
                    }
                    bakeStatements(
                        subject = objectId,
                        statementCommands = `object`.statements!!,
                        thingCommands = thingCommands,
                        validationCache = validationCache,
                        destination = destination
                    )
                }
            }
        }
    }

    data class Result(
        val validationCache: Map<String, Either<CreateThingCommandPart, Thing>>,
        val bakedStatements: Set<BakedStatement>,
    )
}
