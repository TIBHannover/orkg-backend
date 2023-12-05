package org.orkg.contenttypes.domain.actions

import org.orkg.common.ObservatoryId
import org.orkg.community.domain.ObservatoryNotFound
import org.orkg.community.output.ObservatoryRepository
import org.orkg.contenttypes.domain.OnlyOneObservatoryAllowed

class ObservatoryValidator<T, S>(
    private val observatoryRepository: ObservatoryRepository,
    private val valueSelector: (T) -> List<ObservatoryId>?
) : Action<T, S> {
    override fun invoke(command: T, state: S): S {
        val observatories = valueSelector(command)
        if (observatories != null) {
            if (observatories.size > 1) {
                throw OnlyOneObservatoryAllowed()
            }
            observatories.distinct().forEach {
                observatoryRepository.findById(it).orElseThrow { ObservatoryNotFound(it) }
            }
        }
        return state
    }
}
