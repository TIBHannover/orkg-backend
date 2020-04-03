package eu.tib.orkg.prototype.statements.domain.model
import eu.tib.orkg.prototype.statements.domain.model.jpa.ObservatoryEntity
import java.util.UUID
interface ObservatoryService {
    /**
     * Create a new company with a given name.
     *
     */
    fun create(observatoryName: String, organizationId: UUID): ObservatoryEntity

    fun listObservatories(): List<ObservatoryEntity>
}
