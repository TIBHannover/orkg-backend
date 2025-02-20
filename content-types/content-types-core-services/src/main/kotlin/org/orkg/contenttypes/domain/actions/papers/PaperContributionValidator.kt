package org.orkg.contenttypes.domain.actions.papers

import org.orkg.contenttypes.domain.actions.BakedStatement
import org.orkg.contenttypes.domain.actions.ContributionValidator
import org.orkg.contenttypes.domain.actions.CreatePaperCommand
import org.orkg.contenttypes.domain.actions.CreatePaperState
import org.orkg.graph.output.ThingRepository

class PaperContributionValidator(thingRepository: ThingRepository) :
    ContributionValidator(thingRepository),
    CreatePaperAction {
    override operator fun invoke(command: CreatePaperCommand, state: CreatePaperState): CreatePaperState {
        val bakedStatements: MutableSet<BakedStatement> = mutableSetOf()
        val validatedIds = state.validatedIds.toMutableMap()
        command.contents?.let {
            validate(bakedStatements, validatedIds, state.tempIds, it, it.contributions)
        }
        return state.copy(bakedStatements = bakedStatements, validatedIds = validatedIds)
    }
}
