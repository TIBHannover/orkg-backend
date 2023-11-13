package eu.tib.orkg.prototype.contenttypes.services.actions.comparison

import eu.tib.orkg.prototype.contenttypes.services.actions.AuthorValidator
import eu.tib.orkg.prototype.contenttypes.services.actions.CreateComparisonCommand
import eu.tib.orkg.prototype.contenttypes.services.actions.comparison.ComparisonAction.State
import eu.tib.orkg.prototype.statements.spi.ResourceRepository
import eu.tib.orkg.prototype.statements.spi.StatementRepository

class ComparisonAuthorValidator(
    resourceRepository: ResourceRepository,
    statementRepository: StatementRepository
) : AuthorValidator(resourceRepository, statementRepository), ComparisonAction {
    override operator fun invoke(command: CreateComparisonCommand, state: State): State =
        state.copy(authors = validate(command.authors))
}
