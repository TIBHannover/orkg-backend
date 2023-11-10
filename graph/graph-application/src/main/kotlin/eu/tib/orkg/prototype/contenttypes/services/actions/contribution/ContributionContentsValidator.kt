package eu.tib.orkg.prototype.contenttypes.services.actions.contribution

import eu.tib.orkg.prototype.contenttypes.services.actions.BakedStatement
import eu.tib.orkg.prototype.contenttypes.services.actions.ContributionState
import eu.tib.orkg.prototype.contenttypes.services.actions.ContributionValidator
import eu.tib.orkg.prototype.contenttypes.services.actions.CreateContributionCommand
import eu.tib.orkg.prototype.statements.spi.ThingRepository

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
