package eu.tib.orkg.prototype.contenttypes.services.actions.contribution

import eu.tib.orkg.prototype.contenttypes.services.actions.ContributionState
import eu.tib.orkg.prototype.contenttypes.services.actions.CreateContributionCommand
import eu.tib.orkg.prototype.contenttypes.services.actions.ThingDefinitionValidator
import eu.tib.orkg.prototype.statements.spi.ThingRepository

class ContributionThingDefinitionValidator(
    thingRepository: ThingRepository
) : ThingDefinitionValidator(thingRepository), ContributionAction {
    override operator fun invoke(command: CreateContributionCommand, state: ContributionState): ContributionState {
        val validatedIds = state.validatedIds.toMutableMap()
        validateIdsInDefinitions(command, state.tempIds, validatedIds)
        return state.copy(validatedIds = validatedIds)
    }
}
