package eu.tib.orkg.prototype.contenttypes.services.actions

import eu.tib.orkg.prototype.community.application.ObservatoryNotFound
import eu.tib.orkg.prototype.community.domain.model.ObservatoryId
import eu.tib.orkg.prototype.community.spi.ObservatoryRepository
import eu.tib.orkg.prototype.contenttypes.application.OnlyOneObservatoryAllowed

abstract class ObservatoryValidator(
    private val observatoryRepository: ObservatoryRepository
) {
    internal fun validate(observatories: List<ObservatoryId>){
        if (observatories.size > 1) throw OnlyOneObservatoryAllowed()
        observatories.distinct().forEach {
            observatoryRepository.findById(it).orElseThrow { ObservatoryNotFound(it) }
        }
    }
}
