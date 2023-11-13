package eu.tib.orkg.prototype.contenttypes.services.actions.comparison

import eu.tib.orkg.prototype.contenttypes.services.actions.CreateComparisonCommand
import eu.tib.orkg.prototype.contenttypes.services.actions.DescriptionCreator
import eu.tib.orkg.prototype.contenttypes.services.actions.comparison.ComparisonAction.State
import eu.tib.orkg.prototype.statements.api.LiteralUseCases
import eu.tib.orkg.prototype.statements.api.StatementUseCases

class ComparisonDescriptionCreator(
    literalService: LiteralUseCases,
    statementService: StatementUseCases
) : DescriptionCreator(literalService, statementService), ComparisonAction {
    override fun invoke(command: CreateComparisonCommand, state: State): State {
        create(command.contributorId, state.comparisonId!!, command.description)
        return state
    }
}
