package org.orkg.contenttypes.domain.actions

import org.orkg.common.ThingId
import org.orkg.contenttypes.domain.OnlyOneResearchFieldAllowed
import org.orkg.graph.domain.Classes
import org.orkg.graph.domain.ResearchFieldNotFound
import org.orkg.graph.output.ResourceRepository

abstract class ResearchFieldValidator(
    private val resourceRepository: ResourceRepository
) {
    internal fun validate(researchFields: List<ThingId>) {
        if (researchFields.size > 1) throw OnlyOneResearchFieldAllowed()
        researchFields.distinct().forEach { id ->
            resourceRepository.findById(id)
                .filter { Classes.researchField in it.classes }
                .orElseThrow { ResearchFieldNotFound(id) }
        }
    }
}
