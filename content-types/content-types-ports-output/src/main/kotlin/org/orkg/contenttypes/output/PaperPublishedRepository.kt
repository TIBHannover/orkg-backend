package org.orkg.contenttypes.output

import org.orkg.common.ThingId
import org.orkg.contenttypes.domain.PublishedContentType
import org.orkg.graph.domain.GeneralStatement
import java.util.Optional

interface PaperPublishedRepository {
    fun findById(id: ThingId): Optional<List<GeneralStatement>>

    fun save(paper: PublishedContentType)
}
