package org.orkg.contenttypes.domain.actions.contributions

import org.orkg.common.Either
import org.orkg.common.ThingId
import org.orkg.contenttypes.domain.actions.Action
import org.orkg.contenttypes.domain.actions.BakedStatement
import org.orkg.contenttypes.domain.actions.CreateContributionCommand
import org.orkg.contenttypes.domain.actions.contributions.ContributionAction.State
import org.orkg.contenttypes.input.CreateThingCommandPart
import org.orkg.graph.domain.Thing

interface ContributionAction : Action<CreateContributionCommand, State> {
    data class State(
        val validationCache: Map<String, Either<CreateThingCommandPart, Thing>> = emptyMap(),
        val bakedStatements: Set<BakedStatement> = emptySet(),
        val contributionId: ThingId? = null,
    )
}
