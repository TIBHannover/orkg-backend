package eu.tib.orkg.prototype.contenttypes.services.actions

import eu.tib.orkg.prototype.contenttypes.api.CreatePaperUseCase
import eu.tib.orkg.prototype.contenttypes.application.ThingIsNotAClass
import eu.tib.orkg.prototype.shared.Either
import eu.tib.orkg.prototype.statements.domain.model.Class
import eu.tib.orkg.prototype.statements.domain.model.Thing
import eu.tib.orkg.prototype.statements.spi.ThingRepository

class ThingDefinitionValidator(
    override val thingRepository: ThingRepository
) : PaperAction, ContributionAction, ThingIdValidator {
    override operator fun invoke(command: CreatePaperCommand, state: PaperState): PaperState {
        val validatedIds = state.validatedIds.toMutableMap()
        if (command.contents != null) {
            validateIdsInDefinitions(command.contents, state.tempIds, validatedIds)
        }
        return state.copy(validatedIds = validatedIds)
    }

    override operator fun invoke(command: CreateContributionCommand, state: ContributionState): ContributionState {
        val validatedIds = state.validatedIds.toMutableMap()
        validateIdsInDefinitions(command, state.tempIds, validatedIds)
        return state.copy(validatedIds = validatedIds)
    }

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
