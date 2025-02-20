package org.orkg.contenttypes.domain.actions.comparisons

import org.orkg.common.ThingId
import org.orkg.contenttypes.domain.ContributionNotFound
import org.orkg.contenttypes.domain.actions.Action
import org.orkg.graph.domain.Classes
import org.orkg.graph.output.ResourceRepository

class ComparisonContributionValidator<T, S>(
    private val resourceRepository: ResourceRepository,
    private val valueSelector: (T) -> List<ThingId>?,
) : Action<T, S> {
    override fun invoke(command: T, state: S): S {
        valueSelector(command)?.let { contributions ->
            contributions.distinct().forEach { contributionId ->
                resourceRepository.findById(contributionId)
                    .filter { Classes.contribution in it.classes }
                    .orElseThrow { ContributionNotFound(contributionId) }
            }
        }
        return state
    }
}
