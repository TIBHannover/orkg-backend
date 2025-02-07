package org.orkg.contenttypes.domain.actions.smartreviews

import org.orkg.contenttypes.domain.actions.CreateSmartReviewCommand
import org.orkg.contenttypes.domain.actions.smartreviews.CreateSmartReviewAction.State
import org.orkg.graph.domain.Predicates
import org.orkg.graph.input.CreateStatementUseCase
import org.orkg.graph.input.LiteralUseCases
import org.orkg.graph.input.UnsafeResourceUseCases
import org.orkg.graph.input.UnsafeStatementUseCases

class SmartReviewSectionsCreator(
    private val unsafeStatementUseCases: UnsafeStatementUseCases,
    private val abstractSmartReviewSectionCreator: AbstractSmartReviewSectionCreator
) : CreateSmartReviewAction {
    constructor(
        literalService: LiteralUseCases,
        unsafeResourceUseCases: UnsafeResourceUseCases,
        unsafeStatementUseCases: UnsafeStatementUseCases
    ) : this(unsafeStatementUseCases, AbstractSmartReviewSectionCreator(unsafeStatementUseCases, unsafeResourceUseCases, literalService))

    override fun invoke(command: CreateSmartReviewCommand, state: State): State {
        command.sections.forEach { section ->
            unsafeStatementUseCases.create(
                CreateStatementUseCase.CreateCommand(
                    contributorId = command.contributorId,
                    subjectId = state.contributionId!!,
                    predicateId = Predicates.hasSection,
                    objectId = abstractSmartReviewSectionCreator.create(command.contributorId, section)
                )
            )
        }
        return state
    }
}
