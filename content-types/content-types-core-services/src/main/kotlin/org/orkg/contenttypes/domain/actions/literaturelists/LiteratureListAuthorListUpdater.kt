package org.orkg.contenttypes.domain.actions.literaturelists

import org.orkg.contenttypes.domain.actions.AbstractAuthorListUpdater
import org.orkg.contenttypes.domain.actions.UpdateLiteratureListCommand
import org.orkg.contenttypes.domain.actions.literaturelists.UpdateLiteratureListAction.State
import org.orkg.graph.input.ListUseCases
import org.orkg.graph.input.UnsafeLiteralUseCases
import org.orkg.graph.input.UnsafeResourceUseCases
import org.orkg.graph.input.UnsafeStatementUseCases
import org.orkg.graph.output.ListRepository

class LiteratureListAuthorListUpdater(
    private val authorUpdater: AbstractAuthorListUpdater,
) : UpdateLiteratureListAction {
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

    override fun invoke(command: UpdateLiteratureListCommand, state: State): State {
        if (command.authors != null && command.authors != state.literatureList!!.authors) {
            authorUpdater.update(state.statements, command.contributorId, state.authors, command.literatureListId)
        }
        return state
    }
}
