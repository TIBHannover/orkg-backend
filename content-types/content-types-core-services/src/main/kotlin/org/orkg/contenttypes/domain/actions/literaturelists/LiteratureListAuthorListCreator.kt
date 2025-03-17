package org.orkg.contenttypes.domain.actions.literaturelists

import org.orkg.contenttypes.domain.actions.AbstractAuthorListCreator
import org.orkg.contenttypes.domain.actions.CreateLiteratureListCommand
import org.orkg.contenttypes.domain.actions.literaturelists.CreateLiteratureListAction.State
import org.orkg.graph.input.ListUseCases
import org.orkg.graph.input.UnsafeLiteralUseCases
import org.orkg.graph.input.UnsafeResourceUseCases
import org.orkg.graph.input.UnsafeStatementUseCases

class LiteratureListAuthorListCreator(
    private val authorCreator: AbstractAuthorListCreator,
) : CreateLiteratureListAction {
    constructor(
        unsafeResourceUseCases: UnsafeResourceUseCases,
        unsafeStatementUseCases: UnsafeStatementUseCases,
        unsafeLiteralUseCases: UnsafeLiteralUseCases,
        listService: ListUseCases,
    ) : this(
        AbstractAuthorListCreator(unsafeResourceUseCases, unsafeStatementUseCases, unsafeLiteralUseCases, listService),
    )

    override fun invoke(command: CreateLiteratureListCommand, state: State): State =
        state.apply { authorCreator.create(command.contributorId, authors, literatureListId!!) }
}
