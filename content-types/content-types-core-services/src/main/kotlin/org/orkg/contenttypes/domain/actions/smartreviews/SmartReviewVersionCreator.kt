package org.orkg.contenttypes.domain.actions.smartreviews

import org.orkg.contenttypes.domain.actions.CreateSmartReviewCommand
import org.orkg.contenttypes.domain.actions.CreateSmartReviewState
import org.orkg.contenttypes.domain.actions.PublishSmartReviewCommand
import org.orkg.contenttypes.domain.actions.execute
import org.orkg.contenttypes.domain.actions.smartreviews.PublishSmartReviewAction.State
import org.orkg.contenttypes.domain.ids
import org.orkg.graph.input.ListUseCases
import org.orkg.graph.input.LiteralUseCases
import org.orkg.graph.input.UnsafeResourceUseCases
import org.orkg.graph.input.UnsafeStatementUseCases
import org.orkg.graph.output.ResourceRepository
import org.orkg.graph.output.StatementRepository

class SmartReviewVersionCreator(
    private val resourceRepository: ResourceRepository,
    private val statementRepository: StatementRepository,
    private val unsafeResourceUseCases: UnsafeResourceUseCases,
    private val unsafeStatementUseCases: UnsafeStatementUseCases,
    private val literalService: LiteralUseCases,
    private val listService: ListUseCases,
) : PublishSmartReviewAction {
    override fun invoke(command: PublishSmartReviewCommand, state: State): State {
        val smartReview = state.smartReview!!
        val createSmartReviewCommand = CreateSmartReviewCommand(
            contributorId = command.contributorId,
            title = smartReview.title,
            researchFields = smartReview.researchFields.ids,
            authors = smartReview.authors,
            sustainableDevelopmentGoals = smartReview.sustainableDevelopmentGoals.ids,
            observatories = smartReview.observatories,
            organizations = smartReview.organizations,
            extractionMethod = smartReview.extractionMethod,
            sections = emptyList(),
            references = smartReview.references
        )
        val steps = listOf(
            SmartReviewAuthorCreateValidator(resourceRepository, statementRepository),
            SmartReviewVersionResourceCreator(unsafeResourceUseCases),
            SmartReviewResearchFieldCreator(literalService, unsafeStatementUseCases),
            SmartReviewAuthorCreator(unsafeResourceUseCases, unsafeStatementUseCases, literalService, listService),
            SmartReviewSDGCreator(literalService, unsafeStatementUseCases)
        )
        return state.copy(
            smartReviewVersionId = steps.execute(
                createSmartReviewCommand,
                CreateSmartReviewState()
            ).smartReviewId!!
        )
    }
}
