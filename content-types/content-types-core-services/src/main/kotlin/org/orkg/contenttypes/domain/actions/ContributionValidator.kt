package org.orkg.contenttypes.domain.actions

import org.orkg.common.Either
import org.orkg.contenttypes.domain.EmptyContribution
import org.orkg.contenttypes.domain.InvalidStatementSubject
import org.orkg.contenttypes.domain.ThingIsNotAClass
import org.orkg.contenttypes.domain.ThingIsNotAPredicate
import org.orkg.contenttypes.input.CreatePaperUseCase.CreateCommand.PaperContents
import org.orkg.contenttypes.input.CreatePaperUseCase.CreateCommand.StatementObjectDefinition
import org.orkg.graph.domain.Class
import org.orkg.graph.domain.Literal
import org.orkg.graph.domain.Predicate
import org.orkg.graph.domain.Thing
import org.orkg.graph.output.ThingRepository

abstract class ContributionValidator(
    override val thingRepository: ThingRepository
) : ThingIdValidator {
    internal fun validate(
        bakedStatements: MutableSet<BakedStatement>,
        validatedIds: MutableMap<String, Either<String, Thing>>,
        tempIds: Set<String>,
        contents: PaperContents?
    ) {
        contents?.contributions?.forEachIndexed { index, contribution ->
            if (contribution.statements.isEmpty()) {
                if (contents.contributions.size == 1) {
                    throw EmptyContribution()
                } else {
                    throw EmptyContribution(index)
                }
            }
            contribution.classes.forEach {
                validateId(it.value, tempIds, validatedIds).onRight { thing ->
                    if (thing !is Class) {
                        throw ThingIsNotAClass(thing.id)
                    }
                }
            }
            bakeStatements(
                subject = "^$index",
                definitions = contribution.statements,
                tempIds = tempIds,
                contents = contents,
                validatedIds = validatedIds,
                destination = bakedStatements
            )
        }
    }

    internal fun bakeStatements(
        subject: String,
        definitions: Map<String, List<StatementObjectDefinition>>,
        tempIds: Set<String>,
        contents: PaperContents,
        validatedIds: MutableMap<String, Either<String, Thing>>,
        destination: MutableSet<BakedStatement>
    ) {
        definitions.forEach {
            val validatedPredicate = validateId(it.key, tempIds, validatedIds)
            validatedPredicate.onLeft { tempId ->
                if (tempId !in contents.predicates.keys) {
                    throw ThingIsNotAPredicate(tempId)
                }
            }
            validatedPredicate.onRight { thing ->
                if (thing !is Predicate) {
                    throw ThingIsNotAPredicate(thing.id.value)
                }
            }
            it.value.forEach { `object` ->
                val validatedObject = validateId(`object`.id, tempIds, validatedIds)
                // TODO: Do we disallow linking to existing literals?
                // TODO: Do we ignore duplicate statement definitions or do we want throw an error?
                destination += BakedStatement(subject, validatedPredicate.id, validatedObject.id)
                if (`object`.statements != null) {
                    validatedObject.onLeft { tempId ->
                        if (tempId in contents.literals.keys) {
                            throw InvalidStatementSubject(validatedObject.id)
                        }
                    }
                    validatedObject.onRight { thing ->
                        if (thing is Literal) {
                            throw InvalidStatementSubject(validatedObject.id)
                        }
                    }
                    bakeStatements(validatedObject.id, `object`.statements!!, tempIds, contents, validatedIds, destination)
                }
            }
        }
    }

    private val Either<String, Thing>.id: String
        get() = fold({ it }, { it.id.value })
}