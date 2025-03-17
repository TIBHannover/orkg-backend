package org.orkg.contenttypes.domain.actions.papers

import org.orkg.contenttypes.domain.actions.AbstractAuthorListUpdater
import org.orkg.contenttypes.domain.actions.UpdatePaperCommand
import org.orkg.contenttypes.domain.actions.UpdatePaperState
import org.orkg.graph.input.ListUseCases
import org.orkg.graph.input.UnsafeLiteralUseCases
import org.orkg.graph.input.UnsafeResourceUseCases
import org.orkg.graph.input.UnsafeStatementUseCases
import org.orkg.graph.output.ListRepository

class PaperAuthorListUpdater(
    private val authorUpdater: AbstractAuthorListUpdater,
) : UpdatePaperAction {
    constructor(
        unsafeResourceUseCases: UnsafeResourceUseCases,
        unsafeStatementUseCases: UnsafeStatementUseCases,
        unsafeLiteralUseCases: UnsafeLiteralUseCases,
        listService: ListUseCases,
        listRepository: ListRepository,
    ) : this(
        AbstractAuthorListUpdater(
            unsafeResourceUseCases,
            unsafeStatementUseCases,
            unsafeLiteralUseCases,
            listService,
            listRepository
        )
    )

    override operator fun invoke(command: UpdatePaperCommand, state: UpdatePaperState): UpdatePaperState {
        if (command.authors != null && command.authors != state.paper!!.authors) {
            authorUpdater.update(state.statements, command.contributorId, state.authors, command.paperId)
        }
        return state
    }
}
