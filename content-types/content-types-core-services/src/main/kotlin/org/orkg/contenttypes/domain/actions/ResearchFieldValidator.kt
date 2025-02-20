package org.orkg.contenttypes.domain.actions

import org.orkg.common.ThingId
import org.orkg.contenttypes.domain.OnlyOneResearchFieldAllowed
import org.orkg.graph.domain.Classes
import org.orkg.graph.domain.ResearchFieldNotFound
import org.orkg.graph.output.ResourceRepository

class ResearchFieldValidator<T, S>(
    private val resourceRepository: ResourceRepository,
    private val newValueSelector: (T) -> List<ThingId>?,
    private val oldValueSelector: (S) -> List<ThingId> = { emptyList() },
) : Action<T, S> {
    override fun invoke(command: T, state: S): S {
        val newResearchFields = newValueSelector(command)
        val oldResearchFields = oldValueSelector(state)
        if (newResearchFields != null && newResearchFields != oldResearchFields) {
            if (newResearchFields.size > 1) {
                throw OnlyOneResearchFieldAllowed()
            }
            (newResearchFields.distinct() - oldResearchFields.toSet()).forEach { id ->
                resourceRepository.findById(id)
                    .filter { Classes.researchField in it.classes }
                    .orElseThrow { ResearchFieldNotFound(id) }
            }
        }
        return state
    }
}
