package eu.tib.orkg.prototype.contenttypes.services.actions

import eu.tib.orkg.prototype.contenttypes.application.OnlyOneResearchFieldAllowed
import eu.tib.orkg.prototype.statements.api.Classes
import eu.tib.orkg.prototype.statements.application.ResearchFieldNotFound
import eu.tib.orkg.prototype.statements.domain.model.ThingId
import eu.tib.orkg.prototype.statements.spi.ResourceRepository

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
