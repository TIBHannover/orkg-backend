package org.orkg.contenttypes.domain.actions.smartreviews

import org.orkg.contenttypes.domain.actions.AuthorValidator
import org.orkg.contenttypes.domain.actions.UpdateSmartReviewCommand
import org.orkg.contenttypes.domain.actions.smartreviews.UpdateSmartReviewAction.State
import org.orkg.graph.output.ResourceRepository
import org.orkg.graph.output.StatementRepository

class SmartReviewAuthorUpdateValidator(
    private val authorValidator: AuthorValidator,
) : UpdateSmartReviewAction {
    constructor(
        resourceRepository: ResourceRepository,
        statementRepository: StatementRepository,
    ) : this(AuthorValidator(resourceRepository, statementRepository))

    override fun invoke(command: UpdateSmartReviewCommand, state: State): State {
        if (command.authors != null && command.authors != state.smartReview!!.authors) {
            return state.copy(authors = authorValidator.validate(command.authors!!))
        }
        return state
    }
}
