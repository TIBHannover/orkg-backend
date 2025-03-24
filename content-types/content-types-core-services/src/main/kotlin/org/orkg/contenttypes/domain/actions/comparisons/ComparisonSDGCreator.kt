package org.orkg.contenttypes.domain.actions.comparisons

import org.orkg.contenttypes.domain.actions.CreateComparisonCommand
import org.orkg.contenttypes.domain.actions.StatementCollectionPropertyCreator
import org.orkg.contenttypes.domain.actions.comparisons.CreateComparisonAction.State
import org.orkg.graph.domain.Predicates
import org.orkg.graph.input.UnsafeLiteralUseCases
import org.orkg.graph.input.UnsafeStatementUseCases

class ComparisonSDGCreator(
    private val statementCollectionPropertyCreator: StatementCollectionPropertyCreator,
) : CreateComparisonAction {
    constructor(
        unsafeLiteralUseCases: UnsafeLiteralUseCases,
        unsafeStatementUseCases: UnsafeStatementUseCases,
    ) : this(StatementCollectionPropertyCreator(unsafeLiteralUseCases, unsafeStatementUseCases))

    override fun invoke(command: CreateComparisonCommand, state: State): State {
        statementCollectionPropertyCreator.create(
            contributorId = command.contributorId,
            subjectId = state.comparisonId!!,
            predicateId = Predicates.sustainableDevelopmentGoal,
            objects = command.sustainableDevelopmentGoals.toList()
        )
        return state
    }
}
