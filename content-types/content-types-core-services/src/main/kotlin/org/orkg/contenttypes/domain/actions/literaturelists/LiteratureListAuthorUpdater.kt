package org.orkg.contenttypes.domain.actions.literaturelists

import org.orkg.contenttypes.domain.actions.AuthorUpdater
import org.orkg.contenttypes.domain.actions.UpdateLiteratureListCommand
import org.orkg.contenttypes.domain.actions.UpdateLiteratureListState
import org.orkg.graph.input.ListUseCases
import org.orkg.graph.input.LiteralUseCases
import org.orkg.graph.input.StatementUseCases
import org.orkg.graph.input.UnsafeResourceUseCases
import org.orkg.graph.input.UnsafeStatementUseCases
import org.orkg.graph.output.ListRepository

class LiteratureListAuthorUpdater(
    private val authorUpdater: AuthorUpdater,
) : UpdateLiteratureListAction {
    constructor(
        unsafeResourceUseCases: UnsafeResourceUseCases,
        statementService: StatementUseCases,
        unsafeStatementUseCases: UnsafeStatementUseCases,
        literalService: LiteralUseCases,
        listService: ListUseCases,
        listRepository: ListRepository,
    ) : this(
        AuthorUpdater(
            unsafeResourceUseCases,
            statementService,
            unsafeStatementUseCases,
            literalService,
            listService,
            listRepository
        )
    )

    override operator fun invoke(command: UpdateLiteratureListCommand, state: UpdateLiteratureListState): UpdateLiteratureListState {
        if (command.authors != null && command.authors != state.literatureList!!.authors) {
            authorUpdater.update(command.contributorId, state.authors, command.literatureListId)
        }
        return state
    }
}
