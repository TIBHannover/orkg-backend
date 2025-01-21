package org.orkg.contenttypes.domain.actions.literaturelists

import org.orkg.contenttypes.domain.actions.AuthorCreator
import org.orkg.contenttypes.domain.actions.CreateLiteratureListCommand
import org.orkg.contenttypes.domain.actions.CreateLiteratureListState
import org.orkg.graph.input.ListUseCases
import org.orkg.graph.input.LiteralUseCases
import org.orkg.graph.input.StatementUseCases
import org.orkg.graph.input.UnsafeResourceUseCases

class LiteratureListAuthorCreator(
    private val authorCreator: AuthorCreator
) : CreateLiteratureListAction {
    constructor(
        unsafeResourceUseCases: UnsafeResourceUseCases,
        statementService: StatementUseCases,
        literalService: LiteralUseCases,
        listService: ListUseCases
    ) : this(object : AuthorCreator(unsafeResourceUseCases, statementService, literalService, listService) {})

    override operator fun invoke(
        command: CreateLiteratureListCommand,
        state: CreateLiteratureListState
    ): CreateLiteratureListState = state.apply {
        authorCreator.create(command.contributorId, state.authors, state.literatureListId!!)
    }
}
