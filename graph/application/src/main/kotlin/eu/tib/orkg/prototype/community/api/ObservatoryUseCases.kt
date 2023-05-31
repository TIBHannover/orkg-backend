package eu.tib.orkg.prototype.community.api
import eu.tib.orkg.prototype.community.domain.model.Observatory
import eu.tib.orkg.prototype.community.domain.model.ObservatoryId
import eu.tib.orkg.prototype.community.domain.model.OrganizationId
import eu.tib.orkg.prototype.community.domain.model.ResearchField
import eu.tib.orkg.prototype.statements.domain.model.ThingId
import java.util.*
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable

interface ObservatoryUseCases {
    /**
     * Create a new observatory with a given name.
     */
    fun create(
        id: ObservatoryId?,
        name: String,
        description: String,
        organizationId: OrganizationId,
        researchField: ThingId?,
        displayId: String
    ): ObservatoryId

    fun listObservatories(pageable: Pageable): Page<Observatory>

    fun findObservatoriesByOrganizationId(id: OrganizationId, pageable: Pageable): Page<Observatory>

    fun findByName(name: String): Optional<Observatory>

    fun findById(id: ObservatoryId): Optional<Observatory>

    fun findByDisplayId(id: String): Optional<Observatory>

    fun changeName(id: ObservatoryId, name: String): Observatory

    fun changeDescription(id: ObservatoryId, description: String): Observatory

    fun changeResearchField(id: ObservatoryId, researchField: ResearchField): Observatory

    fun addOrganization(id: ObservatoryId, organizationId: OrganizationId): Observatory

    fun deleteOrganization(id: ObservatoryId, organizationId: OrganizationId): Observatory

    fun findObservatoriesByResearchField(researchField: ThingId, pageable: Pageable): Page<Observatory>

    /**
     * Remove all observatories
     */
    fun removeAll()
}
