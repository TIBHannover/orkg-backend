package eu.tib.orkg.prototype.statements.services

import eu.tib.orkg.prototype.statements.api.RetrieveThingUseCase
import eu.tib.orkg.prototype.statements.domain.model.Thing
import eu.tib.orkg.prototype.statements.domain.model.ThingId
import eu.tib.orkg.prototype.statements.spi.ThingRepository
import java.util.*
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
@Transactional
class ThingService(
    private val repository: ThingRepository,
) : RetrieveThingUseCase {
    override fun exists(id: ThingId): Boolean =  repository.findByThingId(id).isPresent

    override fun findByThingId(id: ThingId): Optional<Thing> = repository.findByThingId(id)

}
