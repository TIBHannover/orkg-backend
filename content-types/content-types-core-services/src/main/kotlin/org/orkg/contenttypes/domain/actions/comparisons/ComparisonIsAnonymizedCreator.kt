package org.orkg.contenttypes.domain.actions.comparisons

import org.orkg.contenttypes.domain.actions.CreateComparisonCommand
import org.orkg.contenttypes.domain.actions.SingleStatementPropertyCreator
import org.orkg.contenttypes.domain.actions.comparisons.CreateComparisonAction.State
import org.orkg.graph.domain.Literals
import org.orkg.graph.domain.Predicates
import org.orkg.graph.input.LiteralUseCases
import org.orkg.graph.input.UnsafeStatementUseCases

class ComparisonIsAnonymizedCreator(
    private val singleStatementPropertyCreator: SingleStatementPropertyCreator,
) : CreateComparisonAction {
    constructor(
        literalService: LiteralUseCases,
        unsafeStatementUseCases: UnsafeStatementUseCases,
    ) : this(SingleStatementPropertyCreator(literalService, unsafeStatementUseCases))

    override fun invoke(command: CreateComparisonCommand, state: State): State {
        if (command.isAnonymized) {
            singleStatementPropertyCreator.create(
                contributorId = command.contributorId,
                subjectId = state.comparisonId!!,
                predicateId = Predicates.description,
                label = "true",
                datatype = Literals.XSD.BOOLEAN.prefixedUri
            )
        }
        return state
    }
}
