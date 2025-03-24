package org.orkg.contenttypes.domain.actions.papers

import org.orkg.contenttypes.domain.actions.ContributionValidator
import org.orkg.contenttypes.domain.actions.CreatePaperCommand
import org.orkg.contenttypes.domain.actions.papers.CreatePaperAction.State
import org.orkg.graph.output.ThingRepository

class PaperContributionValidator(
    private val contributionValidator: ContributionValidator,
) : CreatePaperAction {
    constructor(thingRepository: ThingRepository) : this(ContributionValidator(thingRepository))

    override fun invoke(command: CreatePaperCommand, state: State): State {
        if (command.contents == null) {
            return state
        }
        val result = contributionValidator.validate(
            validationCacheIn = state.validationCache,
            thingCommands = command.contents!!.all(),
            contributionCommands = command.contents!!.contributions
        )
        return state.copy(bakedStatements = result.bakedStatements, validationCache = result.validationCache)
    }
}
