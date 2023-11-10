package eu.tib.orkg.prototype.contenttypes.services.actions.contribution

import eu.tib.orkg.prototype.contenttypes.services.actions.Action
import eu.tib.orkg.prototype.contenttypes.services.actions.BakedStatement
import eu.tib.orkg.prototype.contenttypes.services.actions.CreateContributionCommand
import eu.tib.orkg.prototype.contenttypes.services.actions.contribution.ContributionAction.*
import eu.tib.orkg.prototype.shared.Either
import eu.tib.orkg.prototype.statements.domain.model.Thing
import eu.tib.orkg.prototype.statements.domain.model.ThingId

interface ContributionAction : Action<CreateContributionCommand, State> {
    data class State(
        val tempIds: Set<String> = emptySet(),
        val validatedIds: Map<String, Either<String, Thing>> = emptyMap(),
        val bakedStatements: Set<BakedStatement> = emptySet(),
        val contributionId: ThingId? = null
    )
}
