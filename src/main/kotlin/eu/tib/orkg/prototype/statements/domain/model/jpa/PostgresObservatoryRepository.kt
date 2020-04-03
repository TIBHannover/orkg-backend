package eu.tib.orkg.prototype.statements.domain.model.jpa
import java.util.UUID
import org.springframework.data.jpa.repository.JpaRepository
interface PostgresObservatoryRepository : JpaRepository<ObservatoryEntity, UUID>
