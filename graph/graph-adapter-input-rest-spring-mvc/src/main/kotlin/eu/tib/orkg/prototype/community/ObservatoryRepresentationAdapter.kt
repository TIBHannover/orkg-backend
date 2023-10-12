package eu.tib.orkg.prototype.community

import eu.tib.orkg.prototype.community.api.ObservatoryRepresentation
import eu.tib.orkg.prototype.community.domain.model.Observatory
import eu.tib.orkg.prototype.statements.domain.model.Resource
import java.util.*
import org.springframework.data.domain.Page

interface ObservatoryRepresentationAdapter : ResearchFieldRepresentationAdapter {
    fun Optional<Observatory>.mapToObservatoryRepresentation(): Optional<ObservatoryRepresentation> =
        map { it.toObservatoryRepresentation() }

    fun Page<Observatory>.mapToObservatoryRepresentation(): Page<ObservatoryRepresentation> =
        map { it.toObservatoryRepresentation() }

    private fun Observatory.toObservatoryRepresentation() =
        toObservatoryRepresentation(
            researchField?.let {
                resourceRepository.findById(it)
                    .filter { resource -> researchFieldClassId in resource.classes }
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
            displayId = displayId
        )
}
