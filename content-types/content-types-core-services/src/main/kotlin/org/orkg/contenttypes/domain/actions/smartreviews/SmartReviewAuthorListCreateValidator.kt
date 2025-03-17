package org.orkg.contenttypes.domain.actions.smartreviews

import org.orkg.contenttypes.domain.actions.AbstractAuthorListValidator
import org.orkg.contenttypes.domain.actions.CreateSmartReviewCommand
import org.orkg.contenttypes.domain.actions.smartreviews.CreateSmartReviewAction.State
import org.orkg.graph.output.ResourceRepository
import org.orkg.graph.output.StatementRepository

class SmartReviewAuthorListCreateValidator(
    private val authorValidator: AbstractAuthorListValidator,
) : CreateSmartReviewAction {
    constructor(
        resourceRepository: ResourceRepository,
        statementRepository: StatementRepository,
    ) : this(AbstractAuthorListValidator(resourceRepository, statementRepository))

    override fun invoke(command: CreateSmartReviewCommand, state: State): State =
        state.copy(authors = authorValidator.validate(command.authors))
}
