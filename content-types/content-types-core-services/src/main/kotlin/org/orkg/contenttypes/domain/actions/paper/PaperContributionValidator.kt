package org.orkg.contenttypes.domain.actions.paper

import org.orkg.contenttypes.domain.actions.BakedStatement
import org.orkg.contenttypes.domain.actions.ContributionValidator
import org.orkg.contenttypes.domain.actions.CreatePaperCommand
import org.orkg.contenttypes.domain.actions.CreatePaperState
import org.orkg.graph.output.ThingRepository

class PaperContributionValidator(thingRepository: ThingRepository) : ContributionValidator(thingRepository), CreatePaperAction {
    override operator fun invoke(command: CreatePaperCommand, state: CreatePaperState): CreatePaperState {
        val bakedStatements: MutableSet<BakedStatement> = mutableSetOf()
        val validatedIds = state.validatedIds.toMutableMap()
        validate(bakedStatements, validatedIds, state.tempIds, command.contents)
        return state.copy(bakedStatements = bakedStatements, validatedIds = validatedIds)
    }
}
