package org.orkg.contenttypes.domain.actions.smartreviews

import org.orkg.contenttypes.domain.PublishedContentType
import org.orkg.contenttypes.domain.actions.PublishSmartReviewCommand
import org.orkg.contenttypes.domain.actions.smartreviews.PublishSmartReviewAction.State
import org.orkg.contenttypes.output.SmartReviewPublishedRepository
import org.orkg.graph.domain.BundleConfiguration
import org.orkg.graph.domain.Classes
import org.orkg.graph.input.StatementUseCases
import org.springframework.data.domain.Sort

private val bundleConfiguration = BundleConfiguration(
    minLevel = null,
    maxLevel = 10,
    blacklist = listOf(Classes.researchField),
    whitelist = emptyList()
)

class SmartReviewVersionArchiver(
    private val statementService: StatementUseCases,
    private val smartReviewPublishedRepository: SmartReviewPublishedRepository
) : PublishSmartReviewAction {
    override fun invoke(command: PublishSmartReviewCommand, state: State): State {
        val statementsToPersist = statementService.fetchAsBundle(
            thingId = command.smartReviewId,
            configuration = bundleConfiguration,
            includeFirst = true,
            sort = Sort.unsorted()
        ).bundle
        smartReviewPublishedRepository.save(
            PublishedContentType(
                id = state.smartReviewVersionId!!,
                rootId = command.smartReviewId,
                subgraph = statementsToPersist
            )
        )
        return state
    }
}
