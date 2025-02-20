package org.orkg.contenttypes.domain.actions.papers

import org.orkg.contenttypes.domain.actions.AuthorUpdater
import org.orkg.contenttypes.domain.actions.UpdatePaperCommand
import org.orkg.contenttypes.domain.actions.UpdatePaperState
import org.orkg.graph.input.ListUseCases
import org.orkg.graph.input.LiteralUseCases
import org.orkg.graph.input.StatementUseCases
import org.orkg.graph.input.UnsafeResourceUseCases
import org.orkg.graph.input.UnsafeStatementUseCases
import org.orkg.graph.output.ListRepository

class PaperAuthorUpdater(
    private val authorUpdater: AuthorUpdater,
) : UpdatePaperAction {
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

    override operator fun invoke(command: UpdatePaperCommand, state: UpdatePaperState): UpdatePaperState {
        if (command.authors != null && command.authors != state.paper!!.authors) {
            authorUpdater.update(command.contributorId, state.authors, command.paperId)
        }
        return state
    }
}
