package org.orkg.contenttypes.domain.actions

import org.orkg.common.ThingId
import org.orkg.graph.domain.ResourceNotFound
import org.orkg.graph.output.ResourceRepository

open class ResourceValidator<T, S>(
    private val resourceRepository: ResourceRepository,
    private val newValueSelector: (T) -> Set<ThingId>?,
    private val oldValueSelector: (S) -> Set<ThingId> = { emptySet() },
    private val includeClasses: Set<ThingId> = emptySet(),
    private val exceptionFactory: (ThingId) -> Throwable = { ResourceNotFound.withId(it) },
) : Action<T, S> {
    override fun invoke(command: T, state: S): S {
        val newResources = newValueSelector(command)
        val oldResources = oldValueSelector(state)
        if (newResources != null && newResources != oldResources) {
            (newResources - oldResources).forEach { id ->
                resourceRepository.findById(id)
                    .filter { resource -> includeClasses.all { it in resource.classes } }
                    .orElseThrow { exceptionFactory(id) }
            }
        }
        return state
    }
}
