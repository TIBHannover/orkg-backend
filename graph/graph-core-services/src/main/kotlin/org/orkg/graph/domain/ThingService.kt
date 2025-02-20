package org.orkg.graph.domain

import org.orkg.common.ThingId
import org.orkg.graph.input.ThingUseCases
import org.orkg.graph.output.ThingRepository
import org.orkg.spring.data.annotations.TransactionalOnNeo4j
import org.springframework.stereotype.Service
import java.util.Optional

@Service
@TransactionalOnNeo4j(readOnly = true)
class ThingService(
    private val repository: ThingRepository,
) : ThingUseCases {
    override fun existsById(id: ThingId): Boolean = repository.findById(id).isPresent

    override fun findById(id: ThingId): Optional<Thing> = repository.findById(id)
}
