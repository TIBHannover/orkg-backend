package eu.tib.orkg.prototype.community.adapter.output.jpa.internal
import java.util.Optional
import java.util.UUID
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query

interface PostgresObservatoryRepository : JpaRepository<ObservatoryEntity, UUID> {

    fun findByOrganizationsId(id: UUID, pageable: Pageable): Page<ObservatoryEntity>

    fun findByName(name: String): Optional<ObservatoryEntity>

    @Query("""FROM ObservatoryEntity WHERE (displayId=:name or displayId=LOWER(:name))""")
    fun findByDisplayId(name: String): Optional<ObservatoryEntity>

    fun findByResearchField(researchField: String, pageable: Pageable): Page<ObservatoryEntity>
}
