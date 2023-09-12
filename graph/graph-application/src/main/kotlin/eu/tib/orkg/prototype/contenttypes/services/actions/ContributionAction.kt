package eu.tib.orkg.prototype.contenttypes.services.actions

import eu.tib.orkg.prototype.shared.Either
import eu.tib.orkg.prototype.statements.domain.model.Thing
import eu.tib.orkg.prototype.statements.domain.model.ThingId

interface ContributionAction {
    operator fun invoke(command: CreateContributionCommand, state: State): State

    data class State(
        val tempIds: Set<String> = emptySet(),
        val validatedIds: Map<String, Either<String, Thing>> = emptyMap(),
        val bakedStatements: Set<BakedStatement> = emptySet(),
        val contributionId: ThingId? = null
    )
}
