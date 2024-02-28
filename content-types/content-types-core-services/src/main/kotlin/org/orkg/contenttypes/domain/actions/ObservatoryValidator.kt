package org.orkg.contenttypes.domain.actions

import org.orkg.common.ObservatoryId
import org.orkg.community.domain.ObservatoryNotFound
import org.orkg.community.output.ObservatoryRepository
import org.orkg.contenttypes.domain.OnlyOneObservatoryAllowed

class ObservatoryValidator<T, S>(
    private val observatoryRepository: ObservatoryRepository,
    private val newValueSelector: (T) -> List<ObservatoryId>?,
    private val oldValueSelector: (S) -> List<ObservatoryId> = { emptyList() }
) : Action<T, S> {
    override fun invoke(command: T, state: S): S {
        val newObservatories = newValueSelector(command)
        val oldObservatories = oldValueSelector(state)
        if (newObservatories != null && newObservatories.toSet() != oldObservatories.toSet()) {
            if (newObservatories.size > 1) {
                throw OnlyOneObservatoryAllowed()
            }
            (newObservatories.distinct() - oldObservatories.toSet()).forEach {
                observatoryRepository.findById(it).orElseThrow { ObservatoryNotFound(it) }
            }
        }
        return state
    }
}
