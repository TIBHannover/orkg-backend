package org.orkg.contenttypes.domain.actions

import org.orkg.common.ObservatoryId
import org.orkg.community.domain.ObservatoryNotFound
import org.orkg.community.output.ObservatoryRepository
import org.orkg.contenttypes.domain.OnlyOneObservatoryAllowed

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
