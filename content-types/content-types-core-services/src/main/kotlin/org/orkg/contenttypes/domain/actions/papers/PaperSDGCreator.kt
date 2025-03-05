package org.orkg.contenttypes.domain.actions.papers

import org.orkg.contenttypes.domain.actions.CreatePaperCommand
import org.orkg.contenttypes.domain.actions.CreatePaperState
import org.orkg.contenttypes.domain.actions.StatementCollectionPropertyCreator
import org.orkg.graph.domain.Predicates
import org.orkg.graph.input.UnsafeLiteralUseCases
import org.orkg.graph.input.UnsafeStatementUseCases

class PaperSDGCreator(
    private val statementCollectionPropertyCreator: StatementCollectionPropertyCreator,
) : CreatePaperAction {
    constructor(
        unsafeLiteralUseCases: UnsafeLiteralUseCases,
        unsafeStatementUseCases: UnsafeStatementUseCases,
    ) : this(StatementCollectionPropertyCreator(unsafeLiteralUseCases, unsafeStatementUseCases))

    override operator fun invoke(command: CreatePaperCommand, state: CreatePaperState): CreatePaperState =
        state.also {
            statementCollectionPropertyCreator.create(
                contributorId = command.contributorId,
                subjectId = state.paperId!!,
                predicateId = Predicates.sustainableDevelopmentGoal,
                objects = command.sustainableDevelopmentGoals.toList()
            )
        }
}
