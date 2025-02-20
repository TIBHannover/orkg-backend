package org.orkg.community.adapter.output.jpa.internal
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import java.util.Optional
import java.util.UUID

interface PostgresObservatoryRepository : JpaRepository<ObservatoryEntity, UUID> {
    fun findByName(name: String): Optional<ObservatoryEntity>

    fun findAllByNameContainsIgnoreCase(name: String, pageable: Pageable): Page<ObservatoryEntity>

    @Query("""SELECT DISTINCT researchField FROM ObservatoryEntity WHERE researchField IS NOT NULL""")
    fun findAllResearchFields(pageable: Pageable): Page<String>

    @Query("""FROM ObservatoryEntity WHERE (displayId=:name or displayId=LOWER(:name))""")
    fun findByDisplayId(name: String): Optional<ObservatoryEntity>

    fun findAllByOrganizationsId(id: UUID, pageable: Pageable): Page<ObservatoryEntity>

    fun findAllByResearchField(researchField: String, pageable: Pageable): Page<ObservatoryEntity>
}
