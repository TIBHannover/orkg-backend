package org.orkg.community.adapter.input.rest.mapping

import org.orkg.common.ThingId
import org.orkg.community.adapter.input.rest.ResearchFieldRepresentation
import org.orkg.graph.domain.Resource
import org.orkg.graph.input.ResourceUseCases
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
        ResearchFieldRepresentation(this?.id, this?.label)
}
