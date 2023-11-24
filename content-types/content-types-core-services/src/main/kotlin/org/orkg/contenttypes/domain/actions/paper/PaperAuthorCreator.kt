package org.orkg.contenttypes.domain.actions.paper

import org.orkg.contenttypes.domain.actions.AuthorCreator
import org.orkg.contenttypes.domain.actions.CreatePaperCommand
import org.orkg.contenttypes.domain.actions.paper.PaperAction.State
import org.orkg.graph.input.ListUseCases
import org.orkg.graph.input.LiteralUseCases
import org.orkg.graph.input.ResourceUseCases
import org.orkg.graph.input.StatementUseCases

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
