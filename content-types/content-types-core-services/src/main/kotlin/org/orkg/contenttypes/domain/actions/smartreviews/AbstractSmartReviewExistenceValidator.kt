package org.orkg.contenttypes.domain.actions.smartreviews

import org.orkg.common.ThingId
import org.orkg.contenttypes.domain.SmartReview
import org.orkg.contenttypes.domain.SmartReviewNotFound
import org.orkg.contenttypes.domain.SmartReviewNotModifiable
import org.orkg.contenttypes.domain.SmartReviewService
import org.orkg.graph.domain.Classes
import org.orkg.graph.domain.GeneralStatement
import org.orkg.graph.output.ResourceRepository

class AbstractSmartReviewExistenceValidator(
    private val smartReviewService: SmartReviewService,
    private val resourceRepository: ResourceRepository,
) {
    internal fun findUnpublishedSmartReviewById(id: ThingId): Pair<SmartReview, Map<ThingId, List<GeneralStatement>>> {
        val resource = resourceRepository.findById(id)
            .filter {
                if (Classes.smartReviewPublished in it.classes) {
                    throw SmartReviewNotModifiable(id)
                }
                Classes.smartReview in it.classes
            }
            .orElseThrow { SmartReviewNotFound(id) }
        val subgraph = smartReviewService.findSubgraph(resource)
        val smartReview = SmartReview.from(resource, subgraph.root, subgraph.statements)
        return smartReview to subgraph.statements
    }
}
