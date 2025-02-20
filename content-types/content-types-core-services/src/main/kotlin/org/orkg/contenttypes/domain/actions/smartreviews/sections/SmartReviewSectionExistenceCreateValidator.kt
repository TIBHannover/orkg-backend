package org.orkg.contenttypes.domain.actions.smartreviews.sections

import org.orkg.common.PageRequests
import org.orkg.contenttypes.domain.SmartReviewNotFound
import org.orkg.contenttypes.domain.SmartReviewNotModifiable
import org.orkg.contenttypes.domain.actions.CreateSmartReviewSectionCommand
import org.orkg.contenttypes.domain.actions.smartreviews.sections.CreateSmartReviewSectionAction.State
import org.orkg.graph.domain.Classes
import org.orkg.graph.domain.Predicates
import org.orkg.graph.domain.Resource
import org.orkg.graph.output.StatementRepository

class SmartReviewSectionExistenceCreateValidator(
    private val statementRepository: StatementRepository,
) : CreateSmartReviewSectionAction {
    override fun invoke(command: CreateSmartReviewSectionCommand, state: State): State {
        val statement = statementRepository.findAll(
            subjectId = command.smartReviewId,
            predicateId = Predicates.hasContribution,
            objectClasses = setOf(Classes.contribution, Classes.contributionSmartReview),
            pageable = PageRequests.SINGLE
        ).singleOrNull() ?: throw SmartReviewNotFound(command.smartReviewId)
        val smartReview = statement.subject as Resource
        if (Classes.smartReviewPublished in smartReview.classes) {
            throw SmartReviewNotModifiable(command.smartReviewId)
        }
        if (Classes.smartReview !in smartReview.classes) {
            throw SmartReviewNotFound(command.smartReviewId)
        }
        return state.copy(contributionId = statement.`object`.id)
    }
}
