package eu.tib.orkg.prototype.statements.domain.model.jpa

import java.util.Optional
import java.util.UUID
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.domain.Page

interface PostgresConferenceSeriesRepository : JpaRepository<ConferenceSeriesEntity, UUID> {

    fun findByOrganizationId(id: UUID, pageable: Pageable): Page<ConferenceSeriesEntity>

    @Query("""FROM ConferenceSeriesEntity WHERE (displayId=:name or displayId=LOWER(:name))""")
    fun findByDisplayId(name: String): Optional<ConferenceSeriesEntity>

    fun findByName(name: String): Optional<ConferenceSeriesEntity>
}
