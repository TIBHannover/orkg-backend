package org.orkg.contenttypes.domain.actions.contribution

import org.orkg.common.Either
import org.orkg.common.ThingId
import org.orkg.contenttypes.domain.actions.Action
import org.orkg.contenttypes.domain.actions.BakedStatement
import org.orkg.contenttypes.domain.actions.CreateContributionCommand
import org.orkg.contenttypes.domain.actions.contribution.ContributionAction.State
import org.orkg.graph.domain.Thing

interface ContributionAction : Action<CreateContributionCommand, State> {
    data class State(
        val tempIds: Set<String> = emptySet(),
        val validatedIds: Map<String, Either<String, Thing>> = emptyMap(),
        val bakedStatements: Set<BakedStatement> = emptySet(),
        val contributionId: ThingId? = null
    )
}
