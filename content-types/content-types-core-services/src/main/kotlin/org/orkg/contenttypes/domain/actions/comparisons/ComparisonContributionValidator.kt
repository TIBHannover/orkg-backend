package org.orkg.contenttypes.domain.actions.comparisons

import org.orkg.common.ThingId
import org.orkg.contenttypes.domain.ContributionNotFound
import org.orkg.contenttypes.domain.RequiresAtLeastTwoContributions
import org.orkg.contenttypes.domain.actions.Action
import org.orkg.graph.domain.Classes
import org.orkg.graph.output.ResourceRepository

class ComparisonContributionValidator<T, S>(
    val resourceRepository: ResourceRepository,
    val valueSelector: (T) -> List<ThingId>?
) : Action<T, S> {
    override operator fun invoke(command: T, state: S): S {
        valueSelector(command)?.let { contributions ->
            if (contributions.distinct().size < 2) {
                throw RequiresAtLeastTwoContributions()
            }
            contributions.distinct().forEach { contributionId ->
                resourceRepository.findById(contributionId)
                    .filter { Classes.contribution in it.classes }
                    .orElseThrow { ContributionNotFound(contributionId) }
            }
        }
        return state
    }
}
