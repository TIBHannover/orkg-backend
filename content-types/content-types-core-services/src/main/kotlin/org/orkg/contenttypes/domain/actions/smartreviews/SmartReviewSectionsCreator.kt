package org.orkg.contenttypes.domain.actions.smartreviews

import org.orkg.contenttypes.domain.actions.CreateSmartReviewCommand
import org.orkg.contenttypes.domain.actions.smartreviews.CreateSmartReviewAction.State
import org.orkg.graph.domain.Predicates
import org.orkg.graph.input.LiteralUseCases
import org.orkg.graph.input.StatementUseCases
import org.orkg.graph.input.UnsafeResourceUseCases

class SmartReviewSectionsCreator(
    private val statementService: StatementUseCases,
    private val abstractSmartReviewSectionCreator: AbstractSmartReviewSectionCreator
) : CreateSmartReviewAction {
    constructor(
        literalService: LiteralUseCases,
        unsafeResourceUseCases: UnsafeResourceUseCases,
        statementService: StatementUseCases
    ) : this(statementService, AbstractSmartReviewSectionCreator(statementService, unsafeResourceUseCases, literalService))

    override fun invoke(command: CreateSmartReviewCommand, state: State): State {
        command.sections.forEach { section ->
            statementService.add(
                userId = command.contributorId,
                subject = state.contributionId!!,
                predicate = Predicates.hasSection,
                `object` = abstractSmartReviewSectionCreator.create(command.contributorId, section)
            )
        }
        return state
    }
}
