package org.orkg.contenttypes.domain.actions.paper

import org.orkg.contenttypes.domain.actions.AuthorUpdater
import org.orkg.contenttypes.domain.actions.UpdatePaperCommand
import org.orkg.contenttypes.domain.actions.UpdatePaperState
import org.orkg.graph.input.ListUseCases
import org.orkg.graph.input.LiteralUseCases
import org.orkg.graph.input.ResourceUseCases
import org.orkg.graph.input.StatementUseCases

class PaperAuthorUpdater(
    resourceService: ResourceUseCases,
    statementService: StatementUseCases,
    literalService: LiteralUseCases,
    listService: ListUseCases
) : AuthorUpdater(resourceService, statementService, literalService, listService), UpdatePaperAction {
    override operator fun invoke(command: UpdatePaperCommand, state: UpdatePaperState): UpdatePaperState =
        state.apply { update(command.contributorId, state.authors, command.paperId) }
}
