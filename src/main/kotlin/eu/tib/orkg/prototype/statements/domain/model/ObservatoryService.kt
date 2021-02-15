package eu.tib.orkg.prototype.statements.domain.model
import java.util.Optional

interface ObservatoryService {
    /**
     * Create a new observatory with a given name.
     */
    fun create(name: String, description: String, organization: Organization, researchField: String, uriName: String): Observatory

    fun listObservatories(): List<Observatory>

    fun findObservatoriesByOrganizationId(id: OrganizationId): List<Observatory>

    fun findByName(name: String): Optional<Observatory>

    fun findById(id: ObservatoryId): Optional<Observatory>

    fun findByUriName(id: String): Optional<Observatory>

    fun changeName(id: ObservatoryId, to: String): Observatory

    fun changeDescription(id: ObservatoryId, to: String): Observatory

    fun changeResearchField(id: ObservatoryId, to: String): Observatory

    fun findObservatoriesByResearchField(researchField: String): List<Observatory>
}
