package org.orkg.contenttypes.domain.actions.smartreviews.sections

import org.orkg.contenttypes.domain.actions.DeleteSmartReviewSectionCommand
import org.orkg.contenttypes.domain.actions.smartreviews.AbstractSmartReviewSectionDeleter
import org.orkg.contenttypes.domain.actions.smartreviews.findContributionId
import org.orkg.contenttypes.domain.actions.smartreviews.sections.DeleteSmartReviewSectionAction.State
import org.orkg.graph.input.ResourceUseCases
import org.orkg.graph.input.StatementUseCases

class SmartReviewSectionDeleter(
    private val abstractSmartReviewSectionDeleter: AbstractSmartReviewSectionDeleter
) : DeleteSmartReviewSectionAction {
    constructor(
        statementService: StatementUseCases,
        resourceService: ResourceUseCases
    ) : this(AbstractSmartReviewSectionDeleter(statementService, resourceService))

    override fun invoke(command: DeleteSmartReviewSectionCommand, state: State): State {
        val section = state.smartReview!!.sections.find { it.id == command.sectionId }
        if (section != null) {
            abstractSmartReviewSectionDeleter.delete(
                contributorId = command.contributorId,
                contributionId = state.statements.findContributionId(command.smartReviewId)!!,
                section = section,
                statements = state.statements
            )
        }
        return state
    }
}
