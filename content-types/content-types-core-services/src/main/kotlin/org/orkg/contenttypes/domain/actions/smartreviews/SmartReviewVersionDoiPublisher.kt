package org.orkg.contenttypes.domain.actions.smartreviews

import org.orkg.contenttypes.domain.actions.PublishSmartReviewCommand
import org.orkg.contenttypes.domain.actions.SingleStatementPropertyCreator
import org.orkg.contenttypes.domain.actions.smartreviews.PublishSmartReviewAction.State
import org.orkg.contenttypes.output.DoiService
import org.orkg.graph.domain.Predicates
import org.orkg.graph.input.LiteralUseCases
import org.orkg.graph.input.UnsafeStatementUseCases
import java.net.URI

class SmartReviewVersionDoiPublisher(
    private val singleStatementPropertyCreator: SingleStatementPropertyCreator,
    private val doiService: DoiService,
    private val smartReviewPublishBaseUri: String,
) : PublishSmartReviewAction {
    constructor(
        unsafeStatementUseCases: UnsafeStatementUseCases,
        literalService: LiteralUseCases,
        doiService: DoiService,
        smartReviewPublishBaseUri: String,
    ) : this(SingleStatementPropertyCreator(literalService, unsafeStatementUseCases), doiService, smartReviewPublishBaseUri)

    override fun invoke(command: PublishSmartReviewCommand, state: State): State {
        if (!command.assignDOI) {
            return state
        }
        val smartReview = state.smartReview!!
        val smartReviewVersionId = state.smartReviewVersionId!!
        val doi = doiService.register(
            DoiService.RegisterCommand(
                suffix = smartReviewVersionId.value,
                title = smartReview.title,
                subject = smartReview.researchFields.firstOrNull()?.label.orEmpty(),
                description = command.description!!,
                url = URI.create("$smartReviewPublishBaseUri/").resolve(smartReviewVersionId.value),
                creators = smartReview.authors,
                resourceType = "Review",
                resourceTypeGeneral = "Preprint",
                relatedIdentifiers = emptyList()
            )
        )
        singleStatementPropertyCreator.create(
            contributorId = command.contributorId,
            subjectId = smartReviewVersionId,
            predicateId = Predicates.hasDOI,
            label = doi.value
        )
        return state
    }
}
