package org.orkg.contenttypes.domain.actions.literaturelists

import org.orkg.contenttypes.domain.actions.CreateLiteratureListCommand
import org.orkg.contenttypes.domain.actions.CreateLiteratureListState
import org.orkg.contenttypes.domain.actions.StatementCollectionPropertyCreator
import org.orkg.graph.domain.Predicates
import org.orkg.graph.input.UnsafeLiteralUseCases
import org.orkg.graph.input.UnsafeStatementUseCases

class LiteratureListResearchFieldCreator(
    private val statementCollectionPropertyCreator: StatementCollectionPropertyCreator,
) : CreateLiteratureListAction {
    constructor(
        unsafeLiteralUseCases: UnsafeLiteralUseCases,
        unsafeStatementUseCases: UnsafeStatementUseCases,
    ) : this(StatementCollectionPropertyCreator(unsafeLiteralUseCases, unsafeStatementUseCases))

    override operator fun invoke(
        command: CreateLiteratureListCommand,
        state: CreateLiteratureListState,
    ): CreateLiteratureListState {
        statementCollectionPropertyCreator.create(
            contributorId = command.contributorId,
            subjectId = state.literatureListId!!,
            predicateId = Predicates.hasResearchField,
            objects = command.researchFields
        )
        return state
    }
}
