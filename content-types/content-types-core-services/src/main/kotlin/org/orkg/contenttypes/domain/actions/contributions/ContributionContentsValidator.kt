package org.orkg.contenttypes.domain.actions.contributions

import org.orkg.contenttypes.domain.actions.ContributionValidator
import org.orkg.contenttypes.domain.actions.CreateContributionCommand
import org.orkg.contenttypes.domain.actions.contributions.ContributionAction.State
import org.orkg.graph.output.ThingRepository

class ContributionContentsValidator(
    private val contributionValidator: ContributionValidator,
) : ContributionAction {
    constructor(thingRepository: ThingRepository) : this(ContributionValidator(thingRepository))

    override fun invoke(command: CreateContributionCommand, state: State): State {
        val result = contributionValidator.validate(
            validatedIdsIn = state.validatedIds,
            tempIds = state.tempIds,
            thingsCommand = command,
            contributionCommands = listOf(command.contribution)
        )
        return state.copy(bakedStatements = result.bakedStatements, validatedIds = result.validatedIds)
    }
}
