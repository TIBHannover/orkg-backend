package eu.tib.orkg.prototype.contenttypes.services.actions.comparison

import eu.tib.orkg.prototype.contenttypes.services.actions.CreateComparisonCommand
import eu.tib.orkg.prototype.contenttypes.services.actions.comparison.ComparisonAction.*
import eu.tib.orkg.prototype.statements.api.Predicates
import eu.tib.orkg.prototype.statements.api.StatementUseCases

class ComparisonContributionCreator(
    val statementService: StatementUseCases
) : ComparisonAction {
    override operator fun invoke(command: CreateComparisonCommand, state: State): State {
        command.contributions.forEach { contributionId ->
            statementService.add(
                userId = command.contributorId,
                subject = state.comparisonId!!,
                predicate = Predicates.comparesContribution,
                `object` = contributionId
            )
        }
        return state
    }
}
