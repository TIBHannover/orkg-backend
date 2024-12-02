package org.orkg.graph.domain

import java.util.*
import org.orkg.common.ThingId
import org.orkg.graph.input.RetrieveThingUseCase
import org.orkg.graph.output.ThingRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
@Transactional
class ThingService(
    private val repository: ThingRepository,
) : RetrieveThingUseCase {
    override fun exists(id: ThingId): Boolean = repository.findByThingId(id).isPresent

    override fun findById(id: ThingId): Optional<Thing> = repository.findByThingId(id)
}
