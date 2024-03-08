package org.orkg.contenttypes.domain.actions.comparisons

import org.orkg.contenttypes.domain.ContributionNotFound
import org.orkg.contenttypes.domain.RequiresAtLeastTwoContributions
import org.orkg.contenttypes.domain.actions.CreateComparisonCommand
import org.orkg.contenttypes.domain.actions.comparisons.ComparisonAction.State
import org.orkg.graph.domain.Classes
import org.orkg.graph.output.ResourceRepository

class ComparisonContributionValidator(
    val resourceRepository: ResourceRepository
) : ComparisonAction {
    override operator fun invoke(command: CreateComparisonCommand, state: State): State {
        if (command.contributions.distinct().size < 2) {
            throw RequiresAtLeastTwoContributions()
        }
        command.contributions.distinct().forEach { contributionId ->
            resourceRepository.findById(contributionId)
                .filter { Classes.contribution in it.classes }
                .orElseThrow { ContributionNotFound(contributionId) }
        }
        return state
    }
}
