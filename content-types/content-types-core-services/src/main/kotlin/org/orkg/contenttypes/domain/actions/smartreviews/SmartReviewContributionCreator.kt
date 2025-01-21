package org.orkg.contenttypes.domain.actions.smartreviews

import org.orkg.contenttypes.domain.actions.CreateSmartReviewCommand
import org.orkg.contenttypes.domain.actions.smartreviews.CreateSmartReviewAction.State
import org.orkg.graph.domain.Classes
import org.orkg.graph.domain.Predicates
import org.orkg.graph.input.CreateResourceUseCase
import org.orkg.graph.input.StatementUseCases
import org.orkg.graph.input.UnsafeResourceUseCases

class SmartReviewContributionCreator(
    private val unsafeResourceUseCases: UnsafeResourceUseCases,
    private val statementService: StatementUseCases
) : CreateSmartReviewAction {
    override fun invoke(command: CreateSmartReviewCommand, state: State): State {
        val contributionId = unsafeResourceUseCases.create(
            CreateResourceUseCase.CreateCommand(
                label = command.title,
                classes = setOf(Classes.contribution, Classes.contributionSmartReview),
                contributorId = command.contributorId,
                observatoryId = command.observatories.singleOrNull(),
                organizationId = command.organizations.singleOrNull(),
                extractionMethod = command.extractionMethod
            )
        )
        statementService.add(
            userId = command.contributorId,
            subject = state.smartReviewId!!,
            predicate = Predicates.hasContribution,
            `object` = contributionId
        )
        return state.copy(contributionId = contributionId)
    }
}
