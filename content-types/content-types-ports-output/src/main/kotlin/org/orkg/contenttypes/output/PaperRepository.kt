package org.orkg.contenttypes.output

import org.orkg.common.ThingId
import org.orkg.graph.domain.PaperResourceWithPath
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable

interface PaperRepository {
    fun findAllPapersRelatedToResource(id: ThingId, pageable: Pageable): Page<PaperResourceWithPath>
}
