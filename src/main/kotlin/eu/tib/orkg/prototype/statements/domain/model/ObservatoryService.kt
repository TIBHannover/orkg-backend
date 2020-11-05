package eu.tib.orkg.prototype.statements.domain.model
import java.util.Optional
import java.util.UUID

interface ObservatoryService {
    /**
     * Create a new observatory with a given name.
     */
    fun create(name: String, description: String, organization: Organization, researchField: String): Observatory

    fun listObservatories(): List<Observatory>

    fun findObservatoriesByOrganizationId(id: UUID): List<Observatory>

    fun findByName(name: String): Optional<Observatory>

    fun findById(id: ObservatoryId): Optional<Observatory>

    fun changeName(id: ObservatoryId, to: String): Observatory

    fun changeDescription(id: ObservatoryId, to: String): Observatory

    fun changeResearchField(id: ObservatoryId, to: String): Observatory

    fun findObservatoriesByResearchField(researchField: String): List<Observatory>
}
