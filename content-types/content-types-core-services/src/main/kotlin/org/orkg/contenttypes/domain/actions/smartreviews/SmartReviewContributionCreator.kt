package org.orkg.contenttypes.domain.actions.smartreviews

import org.orkg.contenttypes.domain.actions.CreateSmartReviewCommand
import org.orkg.contenttypes.domain.actions.smartreviews.CreateSmartReviewAction.State
import org.orkg.graph.domain.Classes
import org.orkg.graph.domain.Predicates
import org.orkg.graph.input.CreateResourceUseCase
import org.orkg.graph.input.CreateStatementUseCase
import org.orkg.graph.input.UnsafeResourceUseCases
import org.orkg.graph.input.UnsafeStatementUseCases

class SmartReviewContributionCreator(
    private val unsafeResourceUseCases: UnsafeResourceUseCases,
    private val unsafeStatementUseCases: UnsafeStatementUseCases,
) : CreateSmartReviewAction {
    override fun invoke(command: CreateSmartReviewCommand, state: State): State {
        val contributionId = unsafeResourceUseCases.create(
            CreateResourceUseCase.CreateCommand(
                contributorId = command.contributorId,
                label = command.title,
                classes = setOf(Classes.contribution, Classes.contributionSmartReview),
                extractionMethod = command.extractionMethod,
                observatoryId = command.observatories.singleOrNull(),
                organizationId = command.organizations.singleOrNull()
            )
        )
        unsafeStatementUseCases.create(
            CreateStatementUseCase.CreateCommand(
                contributorId = command.contributorId,
                subjectId = state.smartReviewId!!,
                predicateId = Predicates.hasContribution,
                objectId = contributionId
            )
        )
        return state.copy(contributionId = contributionId)
    }
}
