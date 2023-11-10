package eu.tib.orkg.prototype.contenttypes.services.actions.paper

import eu.tib.orkg.prototype.contenttypes.services.actions.CreatePaperCommand
import eu.tib.orkg.prototype.contenttypes.services.actions.PaperState
import eu.tib.orkg.prototype.contenttypes.services.actions.ThingDefinitionValidator
import eu.tib.orkg.prototype.statements.spi.ThingRepository

class PaperThingDefinitionValidator(
    thingRepository: ThingRepository
) : ThingDefinitionValidator(thingRepository), PaperAction {
    override operator fun invoke(command: CreatePaperCommand, state: PaperState): PaperState {
        val validatedIds = state.validatedIds.toMutableMap()
        if (command.contents != null) {
            validateIdsInDefinitions(command.contents, state.tempIds, validatedIds)
        }
        return state.copy(validatedIds = validatedIds)
    }
}
