package org.orkg.contenttypes.domain.actions.comparisons

import org.orkg.contenttypes.domain.actions.CreateComparisonCommand
import org.orkg.contenttypes.domain.actions.CreateComparisonState
import org.orkg.contenttypes.domain.actions.SDGCreator
import org.orkg.graph.input.StatementUseCases

class ComparisonSDGCreator(
    private val sdgCreator: SDGCreator
) : CreateComparisonAction {
    constructor(statementService: StatementUseCases) : this(SDGCreator(statementService))

    override operator fun invoke(command: CreateComparisonCommand, state: CreateComparisonState): CreateComparisonState =
        state.also { sdgCreator.create(command.contributorId, command.sustainableDevelopmentGoals, state.comparisonId!!) }
}
