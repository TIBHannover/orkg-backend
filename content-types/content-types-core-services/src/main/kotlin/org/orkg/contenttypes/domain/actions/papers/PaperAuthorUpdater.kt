package org.orkg.contenttypes.domain.actions.papers

import org.orkg.contenttypes.domain.actions.AuthorUpdater
import org.orkg.contenttypes.domain.actions.UpdatePaperCommand
import org.orkg.contenttypes.domain.actions.UpdatePaperState
import org.orkg.graph.input.ListUseCases
import org.orkg.graph.input.LiteralUseCases
import org.orkg.graph.input.ResourceUseCases
import org.orkg.graph.input.StatementUseCases

class PaperAuthorUpdater(
    private val authorUpdater: AuthorUpdater
) : UpdatePaperAction {
    constructor(
        resourceService: ResourceUseCases,
        statementService: StatementUseCases,
        literalService: LiteralUseCases,
        listService: ListUseCases
    ) : this(AuthorUpdater(resourceService, statementService, literalService, listService))

    override operator fun invoke(command: UpdatePaperCommand, state: UpdatePaperState): UpdatePaperState {
        if (command.authors != null && command.authors != state.paper!!.authors) {
            authorUpdater.update(command.contributorId, state.authors, command.paperId)
        }
        return state
    }
}
