package org.orkg.contenttypes.adapter.output.simcomp

import org.orkg.contenttypes.adapter.output.simcomp.internal.SimCompThingRepository
import org.orkg.contenttypes.adapter.output.simcomp.internal.ThingType
import org.orkg.contenttypes.domain.PublishedContentType
import org.orkg.contenttypes.output.PaperPublishedRepository
import org.springframework.stereotype.Component

@Component
class SimCompPaperPublishedAdapter(
    private val repository: SimCompThingRepository
) : PaperPublishedRepository {
    override fun save(paper: PublishedContentType) {
        repository.save(paper.rootId, ThingType.PAPER_VERSION, mapOf("statements" to paper.subgraph))
    }
}
