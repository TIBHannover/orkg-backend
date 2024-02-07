package org.orkg.contenttypes.domain.actions.contribution

import org.orkg.contenttypes.domain.actions.ContributionState
import org.orkg.contenttypes.domain.actions.CreateContributionCommand
import org.orkg.contenttypes.domain.actions.ThingDefinitionValidator
import org.orkg.graph.output.ThingRepository

class ContributionThingDefinitionValidator(
    thingRepository: ThingRepository
) : ThingDefinitionValidator(thingRepository), ContributionAction {
    override operator fun invoke(command: CreateContributionCommand, state: ContributionState): ContributionState {
        val validatedIds = state.validatedIds.toMutableMap()
        validateThingDefinitions(command, state.tempIds, validatedIds)
        return state.copy(validatedIds = validatedIds)
    }
}
