package org.orkg.contenttypes.domain.actions.comparisons

import org.orkg.contenttypes.domain.actions.CreateComparisonCommand
import org.orkg.contenttypes.domain.actions.CreateComparisonState
import org.orkg.contenttypes.domain.actions.StatementCollectionPropertyCreator
import org.orkg.graph.domain.Predicates
import org.orkg.graph.input.LiteralUseCases
import org.orkg.graph.input.StatementUseCases

class ComparisonSDGCreator(
    private val statementCollectionPropertyCreator: StatementCollectionPropertyCreator
) : CreateComparisonAction {
    constructor(
        literalUseCases: LiteralUseCases,
        statementService: StatementUseCases
    ) : this(StatementCollectionPropertyCreator(literalUseCases, statementService))

    override operator fun invoke(command: CreateComparisonCommand, state: CreateComparisonState): CreateComparisonState =
        state.also {
            statementCollectionPropertyCreator.create(
                contributorId = command.contributorId,
                subjectId = state.comparisonId!!,
                predicateId = Predicates.sustainableDevelopmentGoal,
                objects = command.sustainableDevelopmentGoals.toList()
            )
        }
}
