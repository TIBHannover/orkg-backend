package eu.tib.orkg.prototype.contenttypes.services.actions.paper

import eu.tib.orkg.prototype.contenttypes.services.actions.AuthorCreator
import eu.tib.orkg.prototype.contenttypes.services.actions.CreatePaperCommand
import eu.tib.orkg.prototype.contenttypes.services.actions.paper.PaperAction.*
import eu.tib.orkg.prototype.statements.api.ListUseCases
import eu.tib.orkg.prototype.statements.api.LiteralUseCases
import eu.tib.orkg.prototype.statements.api.ResourceUseCases
import eu.tib.orkg.prototype.statements.api.StatementUseCases

class PaperAuthorCreator(
    resourceService: ResourceUseCases,
    statementService: StatementUseCases,
    literalService: LiteralUseCases,
    listService: ListUseCases
) : AuthorCreator(resourceService, statementService, literalService, listService), PaperAction {
    override operator fun invoke(command: CreatePaperCommand, state: State): State {
        create(command.contributorId, command.authors, state.paperId!!)
        return state
    }
}
