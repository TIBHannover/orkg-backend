package org.orkg.contenttypes.domain.actions.comparisons

import org.orkg.common.PageRequests
import org.orkg.contenttypes.domain.ComparisonNotModifiable
import org.orkg.contenttypes.domain.actions.UpdateComparisonCommand
import org.orkg.contenttypes.domain.actions.comparisons.UpdateComparisonAction.State
import org.orkg.graph.domain.Classes
import org.orkg.graph.domain.Predicates
import org.orkg.graph.input.StatementUseCases

class ComparisonModifiableValidator(private val statementService: StatementUseCases) : UpdateComparisonAction {
    override operator fun invoke(command: UpdateComparisonCommand, state: State): State {
        val statements = statementService.findAll(
            subjectClasses = setOf(Classes.comparison),
            predicateId = Predicates.hasPreviousVersion,
            objectId = command.comparisonId,
            pageable = PageRequests.SINGLE
        )
        if (!statements.isEmpty) {
            throw ComparisonNotModifiable(command.comparisonId)
        }
        return state
    }
}
