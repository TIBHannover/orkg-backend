package org.orkg.community.input

import java.util.*
import org.orkg.common.OrganizationId
import org.orkg.community.domain.ConferenceSeries
import org.orkg.community.domain.ConferenceSeriesId
import org.orkg.community.domain.Metadata
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable

interface ConferenceSeriesUseCases {

    fun create(id: ConferenceSeriesId?, organizationId: OrganizationId, name: String, url: String, displayId: String, metadata: Metadata): ConferenceSeries

    fun listConferenceSeries(pageable: Pageable): Page<ConferenceSeries>

    fun findSeriesByConference(id: OrganizationId, pageable: Pageable): Page<ConferenceSeries>

    fun findById(id: ConferenceSeriesId): Optional<ConferenceSeries>

    fun findByName(name: String): Optional<ConferenceSeries>

    fun findByDisplayId(name: String): Optional<ConferenceSeries>

    fun removeAll()
}
