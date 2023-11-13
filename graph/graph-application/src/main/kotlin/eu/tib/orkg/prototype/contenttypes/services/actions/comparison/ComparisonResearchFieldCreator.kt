package eu.tib.orkg.prototype.contenttypes.services.actions.comparison

import eu.tib.orkg.prototype.contenttypes.services.actions.CreateComparisonCommand
import eu.tib.orkg.prototype.contenttypes.services.actions.ResearchFieldCreator
import eu.tib.orkg.prototype.contenttypes.services.actions.comparison.ComparisonAction.*
import eu.tib.orkg.prototype.statements.api.StatementUseCases

class ComparisonResearchFieldCreator(
    statementService: StatementUseCases
) : ResearchFieldCreator(statementService), ComparisonAction {
    override operator fun invoke(command: CreateComparisonCommand, state: State): State {
        create(command.contributorId, command.researchFields, state.comparisonId!!)
        return state
    }
}
