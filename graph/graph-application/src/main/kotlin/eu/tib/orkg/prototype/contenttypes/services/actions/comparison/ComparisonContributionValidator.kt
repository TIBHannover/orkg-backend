package eu.tib.orkg.prototype.contenttypes.services.actions.comparison

import eu.tib.orkg.prototype.contenttypes.application.ContributionNotFound
import eu.tib.orkg.prototype.contenttypes.application.RequiresAtLeastTwoContributions
import eu.tib.orkg.prototype.contenttypes.services.actions.CreateComparisonCommand
import eu.tib.orkg.prototype.contenttypes.services.actions.comparison.ComparisonAction.State
import eu.tib.orkg.prototype.statements.api.Classes
import eu.tib.orkg.prototype.statements.spi.ResourceRepository

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
