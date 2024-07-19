package org.orkg.contenttypes.domain.actions.smartreviews.sections

import org.orkg.common.PageRequests
import org.orkg.contenttypes.domain.actions.CreateSmartReviewSectionCommand
import org.orkg.contenttypes.domain.actions.smartreviews.sections.CreateSmartReviewSectionAction.State
import org.orkg.graph.domain.Predicates
import org.orkg.graph.output.StatementRepository

class SmartReviewSectionIndexValidator(
    private val statementRepository: StatementRepository
) : CreateSmartReviewSectionAction {
    override fun invoke(command: CreateSmartReviewSectionCommand, state: State): State {
        if (command.index != null && command.index!! >= 0) {
            val statements = statementRepository.findAll(
                subjectId = state.contributionId,
                predicateId = Predicates.hasSection,
                pageable = PageRequests.ALL
            )
            return state.copy(statements = statements.content.groupBy { it.subject.id })
        }
        return state
    }
}
