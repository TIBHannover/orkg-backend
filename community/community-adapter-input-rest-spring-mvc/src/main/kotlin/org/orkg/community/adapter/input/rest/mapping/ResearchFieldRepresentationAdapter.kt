package org.orkg.community.adapter.input.rest.mapping

import org.orkg.common.ThingId
import org.orkg.community.adapter.input.rest.ResearchFieldRepresentation
import org.orkg.graph.domain.Classes
import org.orkg.graph.domain.Resource
import org.orkg.graph.input.ResourceUseCases
import org.springframework.data.domain.Page

interface ResearchFieldRepresentationAdapter {
    val resourceRepository: ResourceUseCases

    fun Page<ThingId>.mapToResearchFieldRepresentation(): Page<ResearchFieldRepresentation> =
        map {
            resourceRepository.findById(it)
                .filter { resource -> Classes.researchField in resource.classes }
                .orElse(null)
                .toResearchFieldRepresentation()
        }

    fun Resource?.toResearchFieldRepresentation(): ResearchFieldRepresentation =
        ResearchFieldRepresentation(this?.id, this?.label)
}
