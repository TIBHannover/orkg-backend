package org.orkg.contenttypes.domain.actions.smartreviews

import org.orkg.contenttypes.domain.actions.AuthorCreator
import org.orkg.contenttypes.domain.actions.CreateSmartReviewCommand
import org.orkg.contenttypes.domain.actions.smartreviews.CreateSmartReviewAction.State
import org.orkg.graph.input.ListUseCases
import org.orkg.graph.input.LiteralUseCases
import org.orkg.graph.input.StatementUseCases
import org.orkg.graph.input.UnsafeResourceUseCases

class SmartReviewAuthorCreator(
    private val authorCreator: AuthorCreator
) : CreateSmartReviewAction {
    constructor(
        unsafeResourceUseCases: UnsafeResourceUseCases,
        statementService: StatementUseCases,
        literalService: LiteralUseCases,
        listService: ListUseCases
    ) : this(object : AuthorCreator(unsafeResourceUseCases, statementService, literalService, listService) {})

    override fun invoke(command: CreateSmartReviewCommand, state: State): State =
        state.apply {
            authorCreator.create(command.contributorId, state.authors, smartReviewId!!)
        }
}
