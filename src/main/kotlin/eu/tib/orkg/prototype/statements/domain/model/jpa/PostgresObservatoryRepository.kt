package eu.tib.orkg.prototype.statements.domain.model.jpa
import java.util.Optional
import java.util.UUID
import org.springframework.data.jpa.repository.JpaRepository

interface PostgresObservatoryRepository : JpaRepository<ObservatoryEntity, UUID> {

    fun findByorganizationId(id: UUID): List<ObservatoryEntity>

    fun findByName(name: String): Optional<ObservatoryEntity>

    fun findByUsersId(id: UUID): Optional<ObservatoryEntity>
}
