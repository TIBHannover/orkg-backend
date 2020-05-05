package eu.tib.orkg.prototype.statements.domain.model
import eu.tib.orkg.prototype.statements.domain.model.jpa.ObservatoryEntity
import java.util.Optional
import java.util.UUID
interface ObservatoryService {
    /**
     * Create a new company with a given name.
     *
     */
    fun create(observatoryName: String, organizationId: UUID): ObservatoryEntity

    fun listObservatories(): List<ObservatoryEntity>

    fun listObservatoriesByOrganizationId(id: UUID): List<ObservatoryEntity>

    fun findByName(name: String): Optional<ObservatoryEntity>

    fun findById(id: UUID): Optional<Observatory>

    fun findByUserId(id: UUID): Optional<ObservatoryEntity>
}
