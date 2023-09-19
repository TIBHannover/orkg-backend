package eu.tib.orkg.prototype.community

import eu.tib.orkg.prototype.community.domain.model.Observatory
import eu.tib.orkg.prototype.community.domain.model.ObservatoryId
import eu.tib.orkg.prototype.community.domain.model.OrganizationId
import eu.tib.orkg.prototype.community.domain.model.ContributorId
import eu.tib.orkg.prototype.statements.api.ResourceUseCases
import eu.tib.orkg.prototype.statements.domain.model.Resource
import eu.tib.orkg.prototype.statements.domain.model.ThingId
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
        object : ObservatoryRepresentation {
            override val id: ObservatoryId = this@toObservatoryRepresentation.id
            override val name: String = this@toObservatoryRepresentation.name
            override val description: String? = this@toObservatoryRepresentation.description
            override val researchField: ResearchFieldRepresentation = researchField.toResearchFieldRepresentation()
            override val members: Set<ContributorId> = this@toObservatoryRepresentation.members
            override val organizationIds: Set<OrganizationId> = this@toObservatoryRepresentation.organizationIds
            override val displayId: String = this@toObservatoryRepresentation.displayId
        }
}
