package org.orkg.contenttypes.output

import org.orkg.common.ThingId
import org.orkg.graph.domain.Resource
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import java.util.Optional

interface LegacyResearchFieldRepository {
    fun findById(id: ThingId): Optional<Resource>

    fun findAllWithBenchmarks(pageable: Pageable): Page<Resource>
}
