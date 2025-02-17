package org.orkg.community.output

import java.util.*
import org.orkg.common.OrganizationId
import org.orkg.community.domain.ConferenceSeries
import org.orkg.community.domain.ConferenceSeriesId
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable

interface ConferenceSeriesRepository {
    fun save(conferenceSeries: ConferenceSeries)

    fun deleteAll()

    fun findAll(pageable: Pageable): Page<ConferenceSeries>

    fun findById(id: ConferenceSeriesId): Optional<ConferenceSeries>

    fun findAllByOrganizationId(id: OrganizationId, pageable: Pageable): Page<ConferenceSeries>

    fun findByDisplayId(displayId: String): Optional<ConferenceSeries>

    fun findByName(name: String): Optional<ConferenceSeries>
}
