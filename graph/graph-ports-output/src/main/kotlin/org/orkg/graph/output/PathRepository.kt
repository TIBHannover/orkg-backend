package org.orkg.graph.output

import org.orkg.common.ThingId
import org.orkg.graph.domain.Path
import org.orkg.graph.domain.PathDirection
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable

interface PathRepository {
    fun findAllByRootId(
        id: ThingId,
        pageable: Pageable,
        minHops: Int? = null,
        maxHops: Int? = null,
        denyClasses: Set<ThingId> = emptySet(),
        allowClasses: Set<ThingId> = emptySet(),
        terminationClasses: Set<ThingId> = emptySet(),
        pathDirection: PathDirection = PathDirection.OUTGOING,
        includeRoot: Boolean = true,
    ): Page<Path>
}
