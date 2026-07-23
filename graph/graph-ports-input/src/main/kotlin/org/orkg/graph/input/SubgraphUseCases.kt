package org.orkg.graph.input

import org.orkg.common.ThingId
import org.orkg.graph.domain.GeneralStatement
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable

interface SubgraphUseCases : RetrieveSubgraphUseCase

interface RetrieveSubgraphUseCase {
    fun findByRootId(
        id: ThingId,
        pageable: Pageable,
        minHops: Int? = null,
        maxHops: Int? = null,
        denyClasses: Set<ThingId> = emptySet(),
        allowClasses: Set<ThingId> = emptySet(),
        terminationClasses: Set<ThingId> = emptySet(),
    ): Page<GeneralStatement>
}
