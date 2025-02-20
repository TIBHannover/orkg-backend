package org.orkg.community.adapter.input.rest.mapping

import org.orkg.community.adapter.input.rest.ObservatoryRepresentation
import org.orkg.community.domain.Observatory
import org.orkg.graph.domain.Classes
import org.orkg.graph.domain.Resource
import org.springframework.data.domain.Page
import java.util.Optional

interface ObservatoryRepresentationAdapter : ResearchFieldRepresentationAdapter {
    fun Optional<Observatory>.mapToObservatoryRepresentation(): Optional<ObservatoryRepresentation> =
        map { it.toObservatoryRepresentation() }

    fun Page<Observatory>.mapToObservatoryRepresentation(): Page<ObservatoryRepresentation> =
        map { it.toObservatoryRepresentation() }

    private fun Observatory.toObservatoryRepresentation() =
        toObservatoryRepresentation(
            researchField?.let {
                resourceRepository.findById(it)
                    .filter { resource -> Classes.researchField in resource.classes }
                    .orElse(null)
            }
        )

    fun Observatory.toObservatoryRepresentation(researchField: Resource? = null) =
        ObservatoryRepresentation(
            id = id,
            name = name,
            description = description,
            researchField = researchField.toResearchFieldRepresentation(),
            members = members,
            organizationIds = organizationIds,
            displayId = displayId,
            sustainableDevelopmentGoals = sustainableDevelopmentGoals
        )
}
