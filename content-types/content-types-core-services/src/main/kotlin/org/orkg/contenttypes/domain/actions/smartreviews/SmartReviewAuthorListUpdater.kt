package org.orkg.contenttypes.domain.actions.smartreviews

import org.orkg.contenttypes.domain.actions.AbstractAuthorListUpdater
import org.orkg.contenttypes.domain.actions.UpdateSmartReviewCommand
import org.orkg.contenttypes.domain.actions.smartreviews.UpdateSmartReviewAction.State
import org.orkg.graph.input.ListUseCases
import org.orkg.graph.input.UnsafeLiteralUseCases
import org.orkg.graph.input.UnsafeResourceUseCases
import org.orkg.graph.input.UnsafeStatementUseCases
import org.orkg.graph.output.ListRepository

class SmartReviewAuthorListUpdater(
    private val authorUpdater: AbstractAuthorListUpdater,
) : UpdateSmartReviewAction {
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

    override fun invoke(command: UpdateSmartReviewCommand, state: State): State {
        if (command.authors != null && command.authors != state.smartReview!!.authors) {
            authorUpdater.update(state.statements, command.contributorId, state.authors, command.smartReviewId)
        }
        return state
    }
}
