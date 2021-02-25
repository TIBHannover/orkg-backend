package eu.tib.orkg.prototype.statements.domain.model
import java.util.Optional
import java.util.UUID

interface ObservatoryServiceNeo {
    /**
     * Create a new observatory with a given name.
     */

    fun create(id: UUID, name: String, description: String, researchField: String): Observatory

    fun create(name: String, description: String, organization: OrganizationId, researchField: String): Observatory

    fun createRelationInObservatoryOrganization(
        organization: UUID,
        observatoryId: UUID
    )

    fun listObservatories(): List<Observatory>

    fun findByName(name: String): Optional<Observatory>

    fun findById(id: ObservatoryId): Optional<Observatory>
}
