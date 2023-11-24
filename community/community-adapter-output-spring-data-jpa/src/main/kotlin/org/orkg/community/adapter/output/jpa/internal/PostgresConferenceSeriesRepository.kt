package org.orkg.community.adapter.output.jpa.internal

import java.util.*
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query

interface PostgresConferenceSeriesRepository : JpaRepository<ConferenceSeriesEntity, UUID> {

    fun findByOrganizationId(id: UUID, pageable: Pageable): Page<ConferenceSeriesEntity>

    @Query("""FROM ConferenceSeriesEntity WHERE (displayId=:name or displayId=LOWER(:name))""")
    fun findByDisplayId(name: String): Optional<ConferenceSeriesEntity>

    fun findByName(name: String): Optional<ConferenceSeriesEntity>
}
