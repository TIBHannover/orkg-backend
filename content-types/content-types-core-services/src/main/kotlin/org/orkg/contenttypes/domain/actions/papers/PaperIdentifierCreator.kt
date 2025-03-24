package org.orkg.contenttypes.domain.actions.papers

import org.orkg.contenttypes.domain.actions.CreatePaperCommand
import org.orkg.contenttypes.domain.actions.IdentifierCreator
import org.orkg.contenttypes.domain.actions.papers.CreatePaperAction.State
import org.orkg.contenttypes.domain.identifiers.Identifiers
import org.orkg.graph.input.UnsafeLiteralUseCases
import org.orkg.graph.input.UnsafeStatementUseCases

class PaperIdentifierCreator(
    private val identifierCreator: IdentifierCreator,
) : CreatePaperAction {
    constructor(
        unsafeStatementUseCases: UnsafeStatementUseCases,
        unsafeLiteralUseCases: UnsafeLiteralUseCases,
    ) : this(
        IdentifierCreator(unsafeStatementUseCases, unsafeLiteralUseCases)
    )

    override fun invoke(command: CreatePaperCommand, state: State): State {
        identifierCreator.create(
            contributorId = command.contributorId,
            identifiers = command.identifiers,
            identifierDefinitions = Identifiers.paper,
            subjectId = state.paperId!!
        )
        return state
    }
}
