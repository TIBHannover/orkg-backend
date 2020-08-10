package eu.tib.orkg.prototype.statements.domain.model
import java.util.Optional
import java.util.UUID

interface ObservatoryService {
    /**
     * Create a new company with a given name.
     *
     */
    fun create(name: String, description: String, organization: Organization): Observatory

    fun listObservatories(): List<Observatory>

    fun findObservatoriesByOrganizationId(id: UUID): List<Observatory>

    fun findByName(name: String): Optional<Observatory>

    fun findById(id: UUID): Optional<Observatory>
}
