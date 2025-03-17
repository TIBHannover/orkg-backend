package org.orkg.contenttypes.domain.actions.papers

import org.orkg.contenttypes.domain.actions.AbstractAuthorListCreator
import org.orkg.contenttypes.domain.actions.CreatePaperCommand
import org.orkg.contenttypes.domain.actions.papers.CreatePaperAction.State
import org.orkg.graph.input.ListUseCases
import org.orkg.graph.input.UnsafeLiteralUseCases
import org.orkg.graph.input.UnsafeResourceUseCases
import org.orkg.graph.input.UnsafeStatementUseCases

class PaperAuthorListCreator(
    private val authorCreator: AbstractAuthorListCreator,
) : CreatePaperAction {
    constructor(
        unsafeResourceUseCases: UnsafeResourceUseCases,
        unsafeStatementUseCases: UnsafeStatementUseCases,
        unsafeLiteralUseCases: UnsafeLiteralUseCases,
        listService: ListUseCases,
    ) : this(
        AbstractAuthorListCreator(unsafeResourceUseCases, unsafeStatementUseCases, unsafeLiteralUseCases, listService),
    )

    override fun invoke(command: CreatePaperCommand, state: State): State =
        state.apply { authorCreator.create(command.contributorId, authors, paperId!!) }
}
