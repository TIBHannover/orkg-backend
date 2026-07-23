package org.orkg.graph.domain

import org.orkg.common.ThingId
import org.orkg.graph.input.PathUseCases
import org.orkg.graph.output.PathRepository
import org.orkg.graph.output.ThingRepository
import org.orkg.spring.data.annotations.TransactionalOnNeo4j
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.stereotype.Service

@Service
@TransactionalOnNeo4j
class PathService(
    private val pathRepository: PathRepository,
    private val thingRepository: ThingRepository,
) : PathUseCases {
    override fun findAllByRootId(
        id: ThingId,
        pageable: Pageable,
        minHops: Int?,
        maxHops: Int?,
        denyClasses: Set<ThingId>,
        allowClasses: Set<ThingId>,
        terminationClasses: Set<ThingId>,
        pathDirection: PathDirection,
        includeRoot: Boolean,
    ): Page<Path> {
        if (minHops != null && maxHops != null && minHops > maxHops) {
            throw InvalidHopBounds(minHops, maxHops)
        }
        if (thingRepository.findById(id).isEmpty) {
            throw ThingNotFound(id)
        }
        return pathRepository.findAllByRootId(
            id = id,
            pageable = pageable,
            minHops = minHops,
            maxHops = maxHops,
            denyClasses = denyClasses,
            allowClasses = allowClasses,
            terminationClasses = terminationClasses,
            pathDirection = pathDirection,
            includeRoot = includeRoot,
        )
    }
}
