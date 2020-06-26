package eu.tib.orkg.prototype.statements.domain.model
import eu.tib.orkg.prototype.statements.domain.model.jpa.ObservatoryEntity
import eu.tib.orkg.prototype.statements.domain.model.jpa.OrganizationEntity
import java.util.Optional
import java.util.UUID
interface ObservatoryService {
    /**
     * Create a new company with a given name.
     *
     */
    fun create(name: String, description: String, organization: OrganizationEntity): ObservatoryEntity

    fun listObservatories(): List<Observatory>

    fun findObservatoriesByOrganizationId(id: UUID): List<ObservatoryEntity>

    fun findByName(name: String): Optional<ObservatoryEntity>

    fun findById(id: UUID): Optional<Observatory>
}
