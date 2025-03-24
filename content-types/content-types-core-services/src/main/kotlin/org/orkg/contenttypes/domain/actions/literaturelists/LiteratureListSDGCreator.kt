package org.orkg.contenttypes.domain.actions.literaturelists

import org.orkg.contenttypes.domain.actions.CreateLiteratureListCommand
import org.orkg.contenttypes.domain.actions.StatementCollectionPropertyCreator
import org.orkg.contenttypes.domain.actions.literaturelists.CreateLiteratureListAction.State
import org.orkg.graph.domain.Predicates
import org.orkg.graph.input.UnsafeLiteralUseCases
import org.orkg.graph.input.UnsafeStatementUseCases

class LiteratureListSDGCreator(
    private val statementCollectionPropertyCreator: StatementCollectionPropertyCreator,
) : CreateLiteratureListAction {
    constructor(
        unsafeLiteralUseCases: UnsafeLiteralUseCases,
        unsafeStatementUseCases: UnsafeStatementUseCases,
    ) : this(
        StatementCollectionPropertyCreator(unsafeLiteralUseCases, unsafeStatementUseCases)
    )

    override fun invoke(command: CreateLiteratureListCommand, state: State): State {
        statementCollectionPropertyCreator.create(
            contributorId = command.contributorId,
            subjectId = state.literatureListId!!,
            predicateId = Predicates.sustainableDevelopmentGoal,
            objects = command.sustainableDevelopmentGoals.toList()
        )
        return state
    }
}
