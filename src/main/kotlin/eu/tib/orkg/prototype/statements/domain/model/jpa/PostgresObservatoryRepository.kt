package eu.tib.orkg.prototype.statements.domain.model.jpa
import java.util.Optional
import java.util.UUID
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query

// import org.springframework.data.neo4j.annotation.Query

interface PostgresObservatoryRepository : JpaRepository<ObservatoryEntity, UUID> {

    fun findByorganizationId(id: UUID): List<ObservatoryEntity>

    fun findByName(name: String): Optional<ObservatoryEntity>

    @Query(value = "SELECT * FROM userobservatories INNER JOIN observatories o on userobservatories.observatory_id = o.id where user_id=:id", nativeQuery = true)
    fun findByUsersId(id: UUID): Optional<ObservatoryEntity>
}
