package org.orkg.contenttypes.domain.actions.literaturelists

import org.orkg.contenttypes.domain.actions.AuthorCreator
import org.orkg.contenttypes.domain.actions.CreateLiteratureListCommand
import org.orkg.contenttypes.domain.actions.CreateLiteratureListState
import org.orkg.graph.input.ListUseCases
import org.orkg.graph.input.LiteralUseCases
import org.orkg.graph.input.UnsafeResourceUseCases
import org.orkg.graph.input.UnsafeStatementUseCases

class LiteratureListAuthorCreator(
    private val authorCreator: AuthorCreator,
) : CreateLiteratureListAction {
    constructor(
        unsafeResourceUseCases: UnsafeResourceUseCases,
        unsafeStatementUseCases: UnsafeStatementUseCases,
        literalService: LiteralUseCases,
        listService: ListUseCases,
    ) : this(object : AuthorCreator(unsafeResourceUseCases, unsafeStatementUseCases, literalService, listService) {})

    override operator fun invoke(
        command: CreateLiteratureListCommand,
        state: CreateLiteratureListState,
    ): CreateLiteratureListState = state.apply {
        authorCreator.create(command.contributorId, state.authors, state.literatureListId!!)
    }
}
