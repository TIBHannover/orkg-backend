package eu.tib.orkg.prototype.community.api
import eu.tib.orkg.prototype.community.domain.model.Observatory
import eu.tib.orkg.prototype.community.domain.model.ObservatoryId
import eu.tib.orkg.prototype.community.domain.model.OrganizationId
import eu.tib.orkg.prototype.statements.domain.model.ResourceId
import java.util.Optional

interface ObservatoryUseCases {
    /**
     * Create a new observatory with a given name.
     */
    fun create(name: String, description: String, organizationId: OrganizationId, researchField: ResourceId, displayId: String): Observatory

    fun listObservatories(): List<Observatory>

    fun findObservatoriesByOrganizationId(id: OrganizationId): List<Observatory>

    fun findByName(name: String): Optional<Observatory>

    fun findById(id: ObservatoryId): Optional<Observatory>

    fun findByDisplayId(id: String): Optional<Observatory>

    fun changeName(id: ObservatoryId, to: String): Observatory

    fun changeDescription(id: ObservatoryId, to: String): Observatory

    fun changeResearchField(id: ObservatoryId, to: String): Observatory

    fun updateOrganization(id: ObservatoryId, organizationId: OrganizationId): Observatory

    fun deleteOrganization(id: ObservatoryId, organizationId: OrganizationId): Observatory

    fun findObservatoriesByResearchField(researchField: String): List<Observatory>

    /**
     * Remove all observatories
     */
    fun removeAll()
}
