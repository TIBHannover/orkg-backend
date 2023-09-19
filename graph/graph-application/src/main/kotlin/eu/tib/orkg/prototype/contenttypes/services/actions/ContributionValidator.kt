package eu.tib.orkg.prototype.contenttypes.services.actions

import eu.tib.orkg.prototype.contenttypes.api.CreatePaperUseCase
import eu.tib.orkg.prototype.contenttypes.application.EmptyContribution
import eu.tib.orkg.prototype.contenttypes.application.InvalidStatementSubject
import eu.tib.orkg.prototype.contenttypes.application.ThingIsNotAClass
import eu.tib.orkg.prototype.contenttypes.application.ThingIsNotAPredicate
import eu.tib.orkg.prototype.shared.Either
import eu.tib.orkg.prototype.statements.domain.model.Class
import eu.tib.orkg.prototype.statements.domain.model.Literal
import eu.tib.orkg.prototype.statements.domain.model.Predicate
import eu.tib.orkg.prototype.statements.domain.model.Thing
import eu.tib.orkg.prototype.statements.spi.ThingRepository

class ContributionValidator(
    override val thingRepository: ThingRepository
) : PaperAction, ContributionAction, ThingIdValidator {
    override operator fun invoke(command: CreatePaperCommand, state: PaperState): PaperState {
        val bakedStatements: MutableSet<BakedStatement> = mutableSetOf()
        val validatedIds = state.validatedIds.toMutableMap()
        command.contents?.contributions?.forEachIndexed { index, contribution ->
            if (contribution.statements.isEmpty()) {
                throw EmptyContribution(index)
            }
            contribution.classes.forEach {
                validateId(it.value, state.tempIds, validatedIds).onRight { thing ->
                    if (thing !is Class) {
                        throw ThingIsNotAClass(thing.id)
                    }
                }
            }
            bakeStatements(
                subject = "^$index",
                definitions = contribution.statements,
                tempIds = state.tempIds,
                contents = command.contents,
                validatedIds = validatedIds,
                destination = bakedStatements
            )
        }
        return state.copy(bakedStatements = bakedStatements, validatedIds = validatedIds)
    }

    override operator fun invoke(command: CreateContributionCommand, state: ContributionState): ContributionState {
        val bakedStatements: MutableSet<BakedStatement> = mutableSetOf()
        val validatedIds = state.validatedIds.toMutableMap()
        val contribution = command.contributions.single()
        if (contribution.statements.isEmpty()) {
            throw EmptyContribution()
        }
        contribution.classes.forEach {
            validateId(it.value, state.tempIds, validatedIds).onRight { thing ->
                if (thing !is Class) {
                    throw ThingIsNotAClass(thing.id)
                }
            }
        }
        bakeStatements(
            subject = "^0",
            definitions = contribution.statements,
            tempIds = state.tempIds,
            contents = command,
            validatedIds = validatedIds,
            destination = bakedStatements
        )
        return state.copy(bakedStatements = bakedStatements, validatedIds = validatedIds)
    }

    internal fun bakeStatements(
        subject: String,
        definitions: Map<String, List<CreatePaperUseCase.CreateCommand.StatementObjectDefinition>>,
        tempIds: Set<String>,
        contents: CreatePaperUseCase.CreateCommand.PaperContents,
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
                    bakeStatements(validatedObject.id, `object`.statements, tempIds, contents, validatedIds, destination)
                }
            }
        }
    }

    private val Either<String, Thing>.id: String
        get() = fold({ it }, { it.id.value })
}
