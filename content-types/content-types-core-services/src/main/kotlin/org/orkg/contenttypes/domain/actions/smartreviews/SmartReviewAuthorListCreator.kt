package org.orkg.contenttypes.domain.actions.smartreviews

import org.orkg.contenttypes.domain.actions.AbstractAuthorListCreator
import org.orkg.contenttypes.domain.actions.CreateSmartReviewCommand
import org.orkg.contenttypes.domain.actions.smartreviews.CreateSmartReviewAction.State
import org.orkg.graph.input.ListUseCases
import org.orkg.graph.input.UnsafeLiteralUseCases
import org.orkg.graph.input.UnsafeResourceUseCases
import org.orkg.graph.input.UnsafeStatementUseCases

class SmartReviewAuthorListCreator(
    private val authorCreator: AbstractAuthorListCreator,
) : CreateSmartReviewAction {
    constructor(
        unsafeResourceUseCases: UnsafeResourceUseCases,
        unsafeStatementUseCases: UnsafeStatementUseCases,
        unsafeLiteralUseCases: UnsafeLiteralUseCases,
        listService: ListUseCases,
    ) : this(
        AbstractAuthorListCreator(unsafeResourceUseCases, unsafeStatementUseCases, unsafeLiteralUseCases, listService),
    )

    override fun invoke(command: CreateSmartReviewCommand, state: State): State =
        state.apply { authorCreator.create(command.contributorId, authors, smartReviewId!!) }
}
