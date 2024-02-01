package org.orkg.contenttypes.domain.actions

import org.orkg.common.ThingId
import org.orkg.contenttypes.domain.OnlyOneResearchFieldAllowed
import org.orkg.graph.domain.Classes
import org.orkg.graph.domain.ResearchFieldNotFound
import org.orkg.graph.output.ResourceRepository

class ResearchFieldValidator<T, S>(
    private val resourceRepository: ResourceRepository,
    private val valueSelector: (T) -> List<ThingId>?
) : Action<T, S> {
    override fun invoke(command: T, state: S): S {
        val researchFields = valueSelector(command)
        if (researchFields != null) {
            if (researchFields.size > 1) {
                throw OnlyOneResearchFieldAllowed()
            }
            researchFields.distinct().forEach { id ->
                resourceRepository.findById(id)
                    .filter { Classes.researchField in it.classes }
                    .orElseThrow { ResearchFieldNotFound(id) }
            }
        }
        return state
    }
}
