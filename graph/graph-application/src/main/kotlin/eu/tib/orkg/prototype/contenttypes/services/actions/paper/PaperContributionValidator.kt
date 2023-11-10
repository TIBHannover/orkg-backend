package eu.tib.orkg.prototype.contenttypes.services.actions.paper

import eu.tib.orkg.prototype.contenttypes.services.actions.BakedStatement
import eu.tib.orkg.prototype.contenttypes.services.actions.ContributionValidator
import eu.tib.orkg.prototype.contenttypes.services.actions.CreatePaperCommand
import eu.tib.orkg.prototype.contenttypes.services.actions.PaperState
import eu.tib.orkg.prototype.statements.spi.ThingRepository

class PaperContributionValidator(thingRepository: ThingRepository) : ContributionValidator(thingRepository), PaperAction {
    override operator fun invoke(command: CreatePaperCommand, state: PaperState): PaperState {
        val bakedStatements: MutableSet<BakedStatement> = mutableSetOf()
        val validatedIds = state.validatedIds.toMutableMap()
        validate(bakedStatements, validatedIds, state.tempIds, command.contents)
        return state.copy(bakedStatements = bakedStatements, validatedIds = validatedIds)
    }
}
