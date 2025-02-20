package org.orkg.contenttypes.domain.actions.smartreviews

import org.orkg.contenttypes.domain.actions.AuthorValidator
import org.orkg.contenttypes.domain.actions.CreateSmartReviewCommand
import org.orkg.contenttypes.domain.actions.smartreviews.CreateSmartReviewAction.State
import org.orkg.graph.output.ResourceRepository
import org.orkg.graph.output.StatementRepository

class SmartReviewAuthorCreateValidator(
    private val authorValidator: AuthorValidator,
) : CreateSmartReviewAction {
    constructor(
        resourceRepository: ResourceRepository,
        statementRepository: StatementRepository,
    ) : this(AuthorValidator(resourceRepository, statementRepository))

    override fun invoke(command: CreateSmartReviewCommand, state: State): State =
        state.copy(authors = authorValidator.validate(command.authors))
}
