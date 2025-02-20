package org.orkg.contenttypes.domain.actions.smartreviews

import org.orkg.contenttypes.domain.actions.AuthorUpdater
import org.orkg.contenttypes.domain.actions.UpdateSmartReviewCommand
import org.orkg.contenttypes.domain.actions.smartreviews.UpdateSmartReviewAction.State
import org.orkg.graph.input.ListUseCases
import org.orkg.graph.input.LiteralUseCases
import org.orkg.graph.input.StatementUseCases
import org.orkg.graph.input.UnsafeResourceUseCases
import org.orkg.graph.input.UnsafeStatementUseCases
import org.orkg.graph.output.ListRepository

class SmartReviewAuthorUpdater(
    private val authorUpdater: AuthorUpdater,
) : UpdateSmartReviewAction {
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

    override fun invoke(command: UpdateSmartReviewCommand, state: State): State {
        if (command.authors != null && command.authors != state.smartReview!!.authors) {
            authorUpdater.update(command.contributorId, state.authors, command.smartReviewId)
        }
        return state
    }
}
