package org.orkg.contenttypes.domain.actions.contributions

import org.orkg.contenttypes.domain.actions.ContributionState
import org.orkg.contenttypes.domain.actions.CreateContributionCommand
import org.orkg.contenttypes.domain.actions.ThingsCommandValidator
import org.orkg.graph.output.ClassRepository
import org.orkg.graph.output.ThingRepository

class ContributionThingsCommandValidator(
    thingRepository: ThingRepository,
    classRepository: ClassRepository,
) : ThingsCommandValidator(thingRepository, classRepository),
    ContributionAction {
    override operator fun invoke(command: CreateContributionCommand, state: ContributionState): ContributionState {
        val validatedIds = validate(
            thingsCommand = command,
            tempIds = state.tempIds,
            validatedIds = state.validatedIds
        )
        return state.copy(validatedIds = validatedIds)
    }
}
