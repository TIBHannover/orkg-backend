package org.orkg.graph.domain

import org.orkg.common.ThingId
import org.orkg.graph.input.SubgraphUseCases
import org.orkg.graph.output.SubgraphRepository
import org.orkg.graph.output.ThingRepository
import org.orkg.spring.data.annotations.TransactionalOnNeo4j
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.stereotype.Service

@Service
@TransactionalOnNeo4j
class SubgraphService(
    private val subgraphRepository: SubgraphRepository,
    private val thingRepository: ThingRepository,
) : SubgraphUseCases {
    override fun findByRootId(
        id: ThingId,
        pageable: Pageable,
        minHops: Int?,
        maxHops: Int?,
        denyClasses: Set<ThingId>,
        allowClasses: Set<ThingId>,
        terminationClasses: Set<ThingId>,
    ): Page<GeneralStatement> {
        if (minHops != null && maxHops != null && minHops > maxHops) {
            throw InvalidHopBounds(minHops, maxHops)
        }
        if (thingRepository.findById(id).isEmpty) {
            throw ThingNotFound(id)
        }
        return subgraphRepository.findByRootId(
            id = id,
            pageable = pageable,
            minHops = minHops,
            maxHops = maxHops,
            denyClasses = denyClasses,
            allowClasses = allowClasses,
            terminationClasses = terminationClasses,
        )
    }
}
