package org.orkg.contenttypes.domain.actions.papers

import org.orkg.contenttypes.domain.actions.CreatePaperCommand
import org.orkg.contenttypes.domain.actions.StatementCollectionPropertyCreator
import org.orkg.contenttypes.domain.actions.papers.CreatePaperAction.State
import org.orkg.graph.domain.Predicates
import org.orkg.graph.input.UnsafeLiteralUseCases
import org.orkg.graph.input.UnsafeStatementUseCases

class PaperResearchFieldCreator(
    private val statementCollectionPropertyCreator: StatementCollectionPropertyCreator,
) : CreatePaperAction {
    constructor(
        unsafeLiteralUseCases: UnsafeLiteralUseCases,
        unsafeStatementUseCases: UnsafeStatementUseCases,
    ) : this(
        StatementCollectionPropertyCreator(unsafeLiteralUseCases, unsafeStatementUseCases)
    )

    override fun invoke(command: CreatePaperCommand, state: State): State {
        statementCollectionPropertyCreator.create(
            contributorId = command.contributorId,
            subjectId = state.paperId!!,
            predicateId = Predicates.hasResearchField,
            objects = command.researchFields
        )
        return state
    }
}
