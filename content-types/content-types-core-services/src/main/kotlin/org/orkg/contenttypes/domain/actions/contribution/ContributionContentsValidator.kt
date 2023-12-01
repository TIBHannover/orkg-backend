package org.orkg.contenttypes.domain.actions.contribution

import org.orkg.contenttypes.domain.actions.BakedStatement
import org.orkg.contenttypes.domain.actions.ContributionState
import org.orkg.contenttypes.domain.actions.ContributionValidator
import org.orkg.contenttypes.domain.actions.CreateContributionCommand
import org.orkg.graph.output.ThingRepository

class ContributionContentsValidator(
    thingRepository: ThingRepository
) : ContributionValidator(thingRepository), ContributionAction {
    override operator fun invoke(command: CreateContributionCommand, state: ContributionState): ContributionState {
        val bakedStatements: MutableSet<BakedStatement> = mutableSetOf()
        val validatedIds = state.validatedIds.toMutableMap()
        validate(bakedStatements, validatedIds, state.tempIds, command)
        return state.copy(bakedStatements = bakedStatements, validatedIds = validatedIds)
    }
}