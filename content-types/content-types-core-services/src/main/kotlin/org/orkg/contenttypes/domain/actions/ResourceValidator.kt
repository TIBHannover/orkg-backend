package org.orkg.contenttypes.domain.actions

import org.orkg.common.ThingId
import org.orkg.graph.domain.ResourceNotFound
import org.orkg.graph.output.ResourceRepository

class ResourceValidator<T, S>(
    private val resourceRepository: ResourceRepository,
    private val newValueSelector: (T) -> Set<ThingId>?,
    private val oldValueSelector: (S) -> Set<ThingId> = { emptySet() }
) : Action<T, S> {
    override fun invoke(command: T, state: S): S {
        val newResources = newValueSelector(command)
        val oldResources = oldValueSelector(state)
        if (newResources != null && newResources != oldResources) {
            (newResources - oldResources).forEach { id ->
                resourceRepository.findById(id)
                    .orElseThrow { ResourceNotFound.withId(id) }
            }
        }
        return state
    }
}
