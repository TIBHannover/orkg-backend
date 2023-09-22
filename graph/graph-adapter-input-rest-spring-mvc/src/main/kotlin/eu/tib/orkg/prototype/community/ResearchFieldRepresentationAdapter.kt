package eu.tib.orkg.prototype.community

import eu.tib.orkg.prototype.statements.api.ResourceUseCases
import eu.tib.orkg.prototype.statements.domain.model.Resource
import eu.tib.orkg.prototype.statements.domain.model.ThingId
import org.springframework.data.domain.Page

internal val researchFieldClassId = ThingId("ResearchField")

interface ResearchFieldRepresentationAdapter {
    val resourceRepository: ResourceUseCases

    fun Page<ThingId>.mapToResearchFieldRepresentation(): Page<ResearchFieldRepresentation> =
        map {
            resourceRepository.findById(it)
                .filter { resource -> researchFieldClassId in resource.classes }
                .orElse(null)
                .toResearchFieldRepresentation()
        }

    fun Resource?.toResearchFieldRepresentation(): ResearchFieldRepresentation =
        object : ResearchFieldRepresentation {
            override val id: ThingId? = this@toResearchFieldRepresentation?.id
            override val label: String? = this@toResearchFieldRepresentation?.label
        }
}
