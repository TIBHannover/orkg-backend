package org.orkg.contenttypes.domain.actions.literaturelists

import org.orkg.contenttypes.domain.actions.PublishLiteratureListCommand
import org.orkg.contenttypes.domain.actions.SingleStatementPropertyCreator
import org.orkg.contenttypes.domain.actions.literaturelists.PublishLiteratureListAction.State
import org.orkg.graph.domain.Predicates
import org.orkg.graph.input.LiteralUseCases
import org.orkg.graph.input.UnsafeStatementUseCases

class LiteratureListChangelogCreator(
    private val singleStatementPropertyCreator: SingleStatementPropertyCreator,
) : PublishLiteratureListAction {
    constructor(
        literalService: LiteralUseCases,
        unsafeStatementUseCases: UnsafeStatementUseCases,
    ) : this(SingleStatementPropertyCreator(literalService, unsafeStatementUseCases))

    override fun invoke(command: PublishLiteratureListCommand, state: State): State {
        singleStatementPropertyCreator.create(
            contributorId = command.contributorId,
            subjectId = state.literatureListVersionId!!,
            predicateId = Predicates.description,
            label = command.changelog
        )
        return state
    }
}
