package eu.tib.orkg.prototype.contenttypes.services.actions.paper

import eu.tib.orkg.prototype.contenttypes.services.actions.AuthorValidator
import eu.tib.orkg.prototype.contenttypes.services.actions.CreatePaperCommand
import eu.tib.orkg.prototype.contenttypes.services.actions.paper.PaperAction.State
import eu.tib.orkg.prototype.statements.spi.ResourceRepository
import eu.tib.orkg.prototype.statements.spi.StatementRepository

class PaperAuthorValidator(
    resourceRepository: ResourceRepository,
    statementRepository: StatementRepository
) : AuthorValidator(resourceRepository, statementRepository), PaperAction {
    override operator fun invoke(command: CreatePaperCommand, state: State): State =
        state.copy(authors = validate(command.authors))
}
