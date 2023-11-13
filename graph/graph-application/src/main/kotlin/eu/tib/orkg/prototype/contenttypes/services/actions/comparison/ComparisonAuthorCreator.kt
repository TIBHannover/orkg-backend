package eu.tib.orkg.prototype.contenttypes.services.actions.comparison

import eu.tib.orkg.prototype.contenttypes.services.actions.AuthorCreator
import eu.tib.orkg.prototype.contenttypes.services.actions.CreateComparisonCommand
import eu.tib.orkg.prototype.contenttypes.services.actions.comparison.ComparisonAction.*
import eu.tib.orkg.prototype.statements.api.ListUseCases
import eu.tib.orkg.prototype.statements.api.LiteralUseCases
import eu.tib.orkg.prototype.statements.api.ResourceUseCases
import eu.tib.orkg.prototype.statements.api.StatementUseCases

class ComparisonAuthorCreator(
    resourceService: ResourceUseCases,
    statementService: StatementUseCases,
    literalService: LiteralUseCases,
    listService: ListUseCases
) : AuthorCreator(resourceService, statementService, literalService, listService), ComparisonAction {
    override operator fun invoke(command: CreateComparisonCommand, state: State): State {
        create(command.contributorId, command.authors, state.comparisonId!!)
        return state
    }
}
