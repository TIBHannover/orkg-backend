package org.orkg.contenttypes.domain.actions.literaturelists.sections

import org.orkg.common.PageRequests
import org.orkg.contenttypes.domain.actions.CreateLiteratureListSectionCommand
import org.orkg.contenttypes.domain.actions.CreateLiteratureListSectionState
import org.orkg.graph.domain.Predicates
import org.orkg.graph.output.StatementRepository

class LiteratureListSectionIndexValidator(
    private val statementRepository: StatementRepository
) : CreateLiteratureListSectionAction {
    override fun invoke(
        command: CreateLiteratureListSectionCommand,
        state: CreateLiteratureListSectionState
    ): CreateLiteratureListSectionState {
        if (command.index != null && command.index!! >= 0) {
            val statements = statementRepository.findAll(
                pageable = PageRequests.ALL,
                subjectId = command.literatureListId,
                predicateId = Predicates.hasSection
            )
            return state.copy(statements = statements.content.groupBy { it.subject.id })
        }
        return state
    }
}
