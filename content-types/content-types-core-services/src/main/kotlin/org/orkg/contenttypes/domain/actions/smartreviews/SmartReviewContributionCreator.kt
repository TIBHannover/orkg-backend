package org.orkg.contenttypes.domain.actions.smartreviews

import org.orkg.contenttypes.domain.actions.CreateSmartReviewCommand
import org.orkg.contenttypes.domain.actions.smartreviews.CreateSmartReviewAction.State
import org.orkg.graph.domain.Classes
import org.orkg.graph.domain.Predicates
import org.orkg.graph.input.CreateResourceUseCase
import org.orkg.graph.input.ResourceUseCases
import org.orkg.graph.input.StatementUseCases

class SmartReviewContributionCreator(
    private val resourceService: ResourceUseCases,
    private val statementService: StatementUseCases
) : CreateSmartReviewAction {
    override fun invoke(command: CreateSmartReviewCommand, state: State): State {
        val contributionId = resourceService.createUnsafe(
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
